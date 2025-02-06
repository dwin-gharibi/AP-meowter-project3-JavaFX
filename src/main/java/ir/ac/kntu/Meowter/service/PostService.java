package ir.ac.kntu.Meowter.service;

import io.prometheus.metrics.model.registry.Collector;
import ir.ac.kntu.Meowter.model.Comment;
import ir.ac.kntu.Meowter.model.Post;
import ir.ac.kntu.Meowter.model.User;
import ir.ac.kntu.Meowter.repository.PostRepository;
import ir.ac.kntu.Meowter.repository.UserRepository;
import ir.ac.kntu.Meowter.util.*;
import org.hibernate.Session;
import redis.clients.jedis.JedisPubSub;

import java.util.List;
import java.util.Scanner;

import static ir.ac.kntu.Meowter.util.GrammarAndPunctuationUtil.checkGrammarAndPunctuation;

public class PostService {

    private PostRepository postRepository;
    private UserRepository userRepository;
    private NotificationService notificationService;
    private final RedisUtil redisUtil = new RedisUtil();

    public PostService() {
        this.postRepository = new PostRepository();
        this.userRepository = new UserRepository();
        KafkaUtil kafkaUtil = new KafkaUtil(
                "localhost:9092",
                "localhost:9092",
                "notification-group",
                "notifications"
        );
        this.notificationService = new NotificationService(kafkaUtil, "notifications");
    }

    public void createPost(String content, User user) {
        if (user.getId() == null) {
            userRepository.save(user);
        }

        CliFormatter.printTypingEffect("Checking your post for violations...");
        if (BlacklistUtil.containsBlacklistedWord(content) || ContentModerationUtil.containsHarmfulContent(content)) {
            CliFormatter.printTypingEffect("⚠️ Post rejected: Content Moderation rejected the post");
            return;
        }


        redisUtil.publish("post_channel", "New post from " + user.getUsername() + ": " + content);

        PrometheusUtil.POST_CREATED.labelValues(user.getUsername()).inc();
        trackActiveTime(user, 5, true);
        Post post = new Post(content, user);
        postRepository.save(post);
    }


    public void subscribeToPosts() {
        redisUtil.subscribe("post_channel", new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                System.out.println("New post received: " + message);
            }
        });
    }

    public List<Post> getPostsFromFollowings(User user) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Post> posts = null;

        try {
            String hql = "FROM Post p WHERE p.user IN :followings ORDER BY p.createdAt DESC";
            posts = session.createQuery(hql, Post.class)
                    .setParameter("followings", user.getFollowing())
                    .getResultList();
        } finally {
            session.close();
        }

        return posts;
    }

    public void viewAllPosts(int page, int size) {
        List<Post> posts = postRepository.findAllPaginated(page, size);
        if (posts.isEmpty()) {
            System.out.println("No posts available.");
            return;
        }

        for (Post post : posts) {
            System.out.println("Post ID: " + post.getId() + " | " + post.getContent() + " by @" + post.getUser().getUsername());
        }

        System.out.println("Page: " + page + " | Showing " + posts.size() + " of " + size + " posts per page.");
    }

    public void viewPostsByUser(User user) {
        List<Post> userPosts = postRepository.findByUser(user);
        if (userPosts.isEmpty()) {
            System.out.println(user.getUsername() + " has no posts.");
            return;
        }

        for (Post post : userPosts) {
            System.out.println("Post ID: " + post.getId() + " | " + post.getContent());
        }
    }

    public boolean editPost(User user, long postId, String newContent) {
        Post post = postRepository.findById(postId);
        if (post == null) {
            return false;
        }

        if (!post.getUser().equals(user)) {
            return false;
        }

        post.setContent(newContent);
        postRepository.update(post);
        return true;
    }

    public boolean deletePost(User user, long postId) {
        Post post = postRepository.findById(postId);
        if (post == null) {
            return false;
        }

        if (!post.getUser().equals(user)) {
            return false;
        }

        postRepository.delete(postId);
        return true;
    }

    public void viewPostDetails(long postId) {
        Post post = postRepository.findById(postId);
        if (post == null) {
            System.out.println("Post not found.");
            return;
        }

        System.out.println("Post: " + post.getContent() + " by @" + post.getUser().getUsername());
        System.out.println("Likes: " + post.getLikes().size());
        System.out.println("Comments:");
        post.getComments().forEach(comment -> {
            System.out.println("Comment: " + comment.getContent() + " by @" + comment.getUser().getUsername());
        });
    }

    public boolean addLike(User user, long postId) {
        Post post = postRepository.findById(postId);
        if (post == null) {
            return false;
        }

        if (post.getLikes().stream().anyMatch(like -> like.getUser().equals(user))) {
            return false;
        }

        postRepository.addLike(user, post);
        PrometheusUtil.LIKE_ADDED.labelValues(user.getUsername()).inc();

        notificationService.sendNotification(user, post.getUser(), "LIKE", "Post with id " + CliFormatter.boldBlue("#" + postId) + " liked by " + user.getUsername());

        trackActiveTime(user, 5, true);
        return true;
    }

    public List<Post> getUserPosts(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("Invalid user: User must not be null and must have a valid ID.");
        }
        return postRepository.findByUser(user);
    }

    public void addPost(User user, String content, boolean[] flags) {
        boolean flagChecker = flags[0];
        boolean flagDescribe = flags[1];

        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User must not be null and must have a valid ID.");
        }

        CliFormatter.printTypingEffect("Checking your post for violations...");
        if (BlacklistUtil.containsBlacklistedWord(content) || ContentModerationUtil.containsHarmfulContent(content)) {
            CliFormatter.printTypingEffect("⚠️ Post rejected: Content Moderation rejected the post");
            return;
        }

        if (flagChecker){
            content = checkGrammarAndPunctuation(content);
        }

        if (flagDescribe) {
            content = PostDescriptionUtil.describePost(content);
        }


        redisUtil.publish("post_channel", "New post from " + user.getUsername() + ": " + content);


        Post post = new Post(content, user);
        postRepository.save(post);
        System.out.println("Post added successfully.");
    }


    public void manageComments(User user, long postId) {
        Post post = postRepository.findById(postId);
        if (post == null) {
            System.out.println("Post not found.");
            return;
        }

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Manage Comments for Post: " + post.getContent());
            System.out.println("1. View Comments");
            System.out.println("2. Add Comment");
            System.out.println("3. Delete Comment");
            System.out.println("4. Back to Post Management");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.println("Comments:");
                    post.getComments().forEach(comment -> {
                        System.out.println("Comment ID: " + comment.getId() + " | Content: " + comment.getContent() + " | By @" + comment.getUser().getUsername());
                    });
                    break;

                case 2:
                    System.out.print("Enter comment content: ");
                    String content = scanner.nextLine();
                    postRepository.addComment(user, post, content);
                    System.out.println("Comment added successfully.");
                    break;

                case 3:
                    System.out.print("Enter Comment ID to delete: ");
                    long commentId = scanner.nextLong();
                    postRepository.removeComment(user, post, commentId);
                    System.out.println("Comment deleted successfully.");
                    break;

                case 4:
                    return;

                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public List<Comment> getAllComments() {
        return postRepository.findAllComments();
    }

    public List<Post> searchPostsByHashtag(String hashtag) {
        return postRepository.findByHashtag(hashtag);
    }


    public void removeLike(User user, long postId) {
        Post post = postRepository.findById(postId);
        if (post == null) {
            System.out.println("Post not found.");
            return;
        }

        if (post.getLikes().stream().noneMatch(like -> like.getUser().equals(user))) {
            System.out.println("You have not liked this post.");
            return;
        }

        postRepository.removeLike(user, post);
        PrometheusUtil.LIKE_REMOVED.labelValues(user.getUsername()).inc();
        trackActiveTime(user, 5, true);
        System.out.println("Like removed successfully.");
    }

    public boolean addComment(User user, long postId, String content, boolean nestedComment) {
        if (nestedComment) {
            Comment comment = postRepository.findByIdComment(postId);
            if (comment == null) {
                return false;
            }
            PrometheusUtil.COMMENT_ADDED.labelValues(user.getUsername()).inc();
            postRepository.addComment(user, comment, content);
            return true;
        } else {
            Post post = postRepository.findById(postId);
            if (post == null) {
                return false;
            }
            PrometheusUtil.COMMENT_ADDED.labelValues(user.getUsername()).inc();
            postRepository.addComment(user, post, content);
            return true;
        }
    }

    public void trackActiveTime(User user, long duration, boolean isActive) {
        PrometheusUtil.USER_ACTIVE_TIME.labelValues(user.getUsername(), isActive ? "active" : "inactive").inc(duration);
    }

    public void removeComment(User user, long postId, long commentId) {
        Post post = postRepository.findById(postId);
        if (post == null) {
            System.out.println("Post not found.");
            return;
        }

        postRepository.removeComment(user, post, commentId);
        System.out.println("Comment removed successfully.");
    }
}
