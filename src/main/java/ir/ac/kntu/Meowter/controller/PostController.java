package ir.ac.kntu.Meowter.controller;

import ir.ac.kntu.Meowter.model.Comment;
import ir.ac.kntu.Meowter.model.Like;
import ir.ac.kntu.Meowter.model.Post;
import ir.ac.kntu.Meowter.model.User;
import ir.ac.kntu.Meowter.repository.PostRepository;
import ir.ac.kntu.Meowter.service.PostService;
import ir.ac.kntu.Meowter.util.CliFormatter;
import ir.ac.kntu.Meowter.util.PaginationUtil;
import ir.ac.kntu.Meowter.util.RedisHashtagUtil;

import java.util.*;

public class PostController {

    private PostService postService;
    private PostRepository postRepository;

    public PostController() {
        this.postService = new PostService();
        this.postRepository = new PostRepository();
    }

    public void displayPostsSection(User loggedInUser) {
        Scanner scanner = new Scanner(System.in);

        System.out.println(CliFormatter.boldYellow("🔥 Top 5 Hashtags 🔥"));
        RedisHashtagUtil.displayTopHashtags(5);

        while (true) {
            CliFormatter.printTypingEffect(CliFormatter.boldYellow("Welcome to post section:"));
            System.out.println(CliFormatter.boldBlue("1. View My Posts"));
            System.out.println(CliFormatter.boldGreen("2. Add a New Post"));
            System.out.println(CliFormatter.boldBlue("3. Select a post"));
            System.out.println(CliFormatter.boldYellow("4. Handle Requests (L[id], C[id], R[id], #[hashtag])"));
            System.out.println(CliFormatter.boldPurple("5. Back to Main Menu"));
            System.out.print(CliFormatter.magenta("Choose an option: "));
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    displayUserPosts(loggedInUser);
                    break;
                case 2:
                    addNewPost(loggedInUser, scanner);
                    break;
                case 3:
                    selectPost(loggedInUser, scanner);
                    break;
                case 4:
                    handleRequests(loggedInUser, scanner);
                    break;
                case 5:
                    return;
                default:
                    System.out.println(CliFormatter.boldRed("Invalid option. Try again."));
                    break;
            }
        }
    }

    private void displayUserPosts(User loggedInUser) {
        List<Post> posts = postService.getUserPosts(loggedInUser);

        if (!posts.isEmpty()) {
            CliFormatter.printTypingEffect(CliFormatter.bold("\n📸 Posts:\n"));
            List<String> post_details = new ArrayList<>();

            posts.forEach(post -> {
                String postDetail = "Post ID: #" + CliFormatter.blue(String.valueOf(post.getId())) + "\n" +
                        "Content: " + CliFormatter.boldGreen(post.getContent()) + "\n" +
                        "Created At: " + CliFormatter.boldBlue(post.getCreatedAt().toString()) + "\n" +
                        "Likes: " + CliFormatter.yellow(String.valueOf(post.getLikes().size())) + "\n" +
                        "Hashtags: " + (post.getHashtags().isEmpty()
                        ? CliFormatter.red("No hashtags")
                        : CliFormatter.cyan(post.getHashtags().toString())) + "\n" +
                        "Comments:\n";

                if (!post.getComments().isEmpty()) {
                    StringBuilder commentsDetails = new StringBuilder();
                    processComments(post.getComments(), commentsDetails, 1);
                    postDetail += commentsDetails.toString();
                } else {
                    postDetail += CliFormatter.red("    No comments yet.\n");
                }

                post_details.add(postDetail);
            });

            PaginationUtil.paginate(post_details);
        } else {
            System.out.println(CliFormatter.red("\n📸 Posts: No posts yet.\n"));
        }
    }

    private void addNewPost(User loggedInUser, Scanner scanner) {
        System.out.print(CliFormatter.magenta("Enter post content: "));
        String content = scanner.nextLine();

        System.out.print(CliFormatter.magenta("Enter post labels (Multi Label Select using ,): "));
        System.out.print(CliFormatter.boldBlue("Available Labels: #SPORT, #ART, #TECHNOLOGY, #TRAVEL, #FOOD, #ENTERTAINMENT, #EDUCATION\n"));
        String labels = scanner.nextLine();

        System.out.print(CliFormatter.boldGreen("- Do you want @AI to correct your post grammar and punctuation? "));
        boolean flagChecker = scanner.nextBoolean();

        System.out.print(CliFormatter.boldBlue("- Do you want @AI to describe your post? "));
        boolean flagDescribe = scanner.nextBoolean();
        boolean[] flags = {flagChecker, flagDescribe};
        postService.addPost(loggedInUser, content, flags);
    }

    private void selectPost(User loggedInUser, Scanner scanner) {
        System.out.print("Enter Post ID to select: ");
        long postIdToSelect = scanner.nextLong();
        scanner.nextLine();
        Post selectedPost = null;

        while (true) {
            CliFormatter.progressBar(CliFormatter.boldGreen("Loading the post ..."), 10);

            try {
                selectedPost = postRepository.findById(postIdToSelect);
                if (selectedPost == null || !Objects.equals(selectedPost.getUser().getUsername(), loggedInUser.getUsername())) {
                    throw new Exception("Post does not exist");
                }
                String postDetail = "Post ID: #" + CliFormatter.blue(String.valueOf(selectedPost.getId())) + "\n" +
                        "Content: " + CliFormatter.boldGreen(selectedPost.getContent()) + "\n" +
                        "Created At: " + CliFormatter.boldBlue(selectedPost.getCreatedAt().toString()) + "\n" +
                        "Likes: " + CliFormatter.yellow(String.valueOf(selectedPost.getLikes().size())) + "\n" +
                        "Hashtags: " + (selectedPost.getHashtags().isEmpty()
                        ? CliFormatter.red("No hashtags")
                        : CliFormatter.cyan(selectedPost.getHashtags().toString())) + "\n" +
                        "Comments:\n";

                if (!selectedPost.getComments().isEmpty()) {
                    StringBuilder commentsDetails = new StringBuilder();
                    processComments(selectedPost.getComments(), commentsDetails, 1);
                    postDetail += commentsDetails.toString();
                } else {
                    postDetail += CliFormatter.red("    No comments yet.\n");
                }

                System.out.println(postDetail);
            } catch (Exception e){
                System.out.println(CliFormatter.boldRed("Something went wrong"));
                return;
            }
            if (!displayPostDetails(loggedInUser, selectedPost)) {
                break;
            }
        }
    }

    private boolean displayPostDetails(User loggedInUser, Post selectedPost) {
        System.out.println(CliFormatter.boldBlue("1. Edit post"));
        System.out.println(CliFormatter.boldGreen("2. Delete post"));
        System.out.println(CliFormatter.boldBlue("3. Likes Details"));
        System.out.println(CliFormatter.boldYellow("4. Comments Details"));
        System.out.println(CliFormatter.boldPurple("5. Back to Main Menu"));
        System.out.print(CliFormatter.magenta("Choose an option: "));
        Scanner scanner = new Scanner(System.in);
        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                editPost(loggedInUser, selectedPost);
                break;
            case 2:
                deletePost(loggedInUser, selectedPost);
                break;
            case 3:
                likeDetails(selectedPost);
                break;
            case 4:
                commentsDetails(loggedInUser, selectedPost);
                break;
            case 5:
                return false;
            default:
                System.out.println(CliFormatter.boldRed("Invalid option. Try again."));
                return false;
        }
        return true;
    }


    private void likeDetails(Post selectedPost) {
        CliFormatter.progressBar(CliFormatter.boldGreen("Getting like details..."), 10);

        CliFormatter.printTypingEffect(CliFormatter.boldYellow("Post liked by:"));

        Set<Like> likes = selectedPost.getLikes();

        if (likes.isEmpty()) {
            System.out.println(CliFormatter.boldPurple("No likes yet."));
            return;
        }

        for(Like like : likes) {
            System.out.print(CliFormatter.blue(like.getUser().getUsername()) + " ");
        }
    }

    private void commentsDetails(User loggedInUser, Post selectedPost) {
        CliFormatter.progressBar(CliFormatter.boldGreen("Getting comments details..."), 10);

        CliFormatter.printTypingEffect(CliFormatter.boldYellow("Post commented by:"));


        Set<Comment> comments = selectedPost.getComments();

        if (comments.isEmpty()) {
            System.out.println(CliFormatter.boldPurple("No comments yet."));
            return;
        }

        for(Comment comment : comments) {
            System.out.println(CliFormatter.blue(CliFormatter.boldBlue("#" + comment.getId()) + " @" + CliFormatter.magenta(comment.getUser().getUsername()) + " : " + CliFormatter.cyan(comment.getContent())));
        }

        while (true) {
            System.out.print(CliFormatter.magenta("Enter comment id to remove (or back): "));
            Scanner scanner = new Scanner(System.in);
            String commentId = scanner.nextLine();

            if (commentId.equalsIgnoreCase("back")) {
                return;
            }

            try {
                postRepository.removeComment(loggedInUser, selectedPost, Long.parseLong(commentId));
            } catch (Exception e) {
                System.out.println(CliFormatter.boldRed("Something went wrong"));
            }
        }

    }

    private void editPost(User loggedInUser, Post selectedPost) {
        System.out.print("Enter new content: ");
        Scanner scanner = new Scanner(System.in);
        String newContent = scanner.nextLine();
        boolean success = postService.editPost(loggedInUser, selectedPost.getId(), newContent);
        if (success) {
            System.out.println("Post updated successfully.");
        } else {
            System.out.println("Failed to update post. Make sure you are the owner of the post.");
        }
    }

    private void deletePost(User loggedInUser, Post selectedPost) {
        boolean success = postService.deletePost(loggedInUser, selectedPost.getId());
        if (success) {
            System.out.println("Post deleted successfully.");
        } else {
            System.out.println("Failed to delete post. Make sure you are the owner of the post.");
        }
    }

    public void handleRequests(User loggedInUser, Scanner scanner) {
        System.out.println("Handle requests like L[id], C[id], R[id], #[hashtag]. Type 'back' to return.");
        while (true) {
            System.out.print("Enter your request: ");
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("back")) {
                return;
            }

            if (input.startsWith("L")) {
                handleLikeRequest(loggedInUser, input);
            } else if (input.startsWith("C") || input.startsWith("R")) {
                handleCommentRequest(loggedInUser, scanner, input, input.startsWith("R"));
            } else if (input.startsWith("#")) {
                handleHashtagSearch(input);
            } else {
                System.out.println("Invalid request. Use L[id], C[id], R[id] or #[hashtag].");
            }
        }
    }

    private void handleLikeRequest(User loggedInUser, String input) {
        try {
            long postId = Long.parseLong(input.substring(1));
            boolean success = postService.addLike(loggedInUser, postId);
            if (success) {
                System.out.println("You liked the post with ID: " + postId);
            } else {
                System.out.println("Failed to like the post. It might not exist.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid format for like request. Use L[id].");
        }
    }

    private void handleCommentRequest(User loggedInUser, Scanner scanner, String input, boolean nestedComment) {
        try {
            if (nestedComment) {
                long CommentId = Long.parseLong(input.substring(1));
                System.out.print("Enter your reply comment: ");
                String commentContent = scanner.nextLine();
                boolean success = postService.addComment(loggedInUser, CommentId, commentContent, nestedComment);
                if (success) {
                    System.out.println("Your comment was replied to comment ID: " + CommentId);
                } else {
                    System.out.println("Failed to add reply comment. It might not exist.");
                }
            } else {
                long postId = Long.parseLong(input.substring(1));
                System.out.print("Enter your comment: ");
                String commentContent = scanner.nextLine();
                boolean success = postService.addComment(loggedInUser, postId, commentContent, nestedComment);
                if (success) {
                    System.out.println("Your comment was added to post ID: " + postId);
                } else {
                    System.out.println("Failed to comment on the post. It might not exist.");
                }
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid format for comment request. Use C[id].");
        }
    }

    private void processComments(Set<Comment> comments, StringBuilder commentsDetails, int level) {
        String indentation = "    ".repeat(level);
        comments.forEach(comment -> {
            commentsDetails.append(indentation)
                    .append("- Comment by ")
                    .append(CliFormatter.blue(comment.getUser().getUsername()))
                    .append(": ")
                    .append(CliFormatter.cyan(comment.getContent()))
                    .append(" #")
                    .append(CliFormatter.yellow(comment.getId().toString()))
                    .append("\n");

            if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
                processComments(comment.getReplies(), commentsDetails, level + 1);
            }
        });
    }

    private void handleHashtagSearch(String input) {
        List<Post> posts = postService.searchPostsByHashtag(input);
        if (!posts.isEmpty()) {
            CliFormatter.printTypingEffect(CliFormatter.bold("\n📸 Posts:\n"));
            List<String> post_details = new ArrayList<>();

            posts.forEach(post -> {
                String postDetail = "Post ID: #" + CliFormatter.blue(String.valueOf(post.getId())) + "\n" +
                        "Content: " + CliFormatter.boldGreen(post.getContent()) + "\n" +
                        "Created At: " + CliFormatter.boldBlue(post.getCreatedAt().toString()) + "\n" +
                        "Likes: " + CliFormatter.yellow(String.valueOf(post.getLikes().size())) + "\n" +
                        "Hashtags: " + (post.getHashtags().isEmpty()
                        ? CliFormatter.red("No hashtags")
                        : CliFormatter.cyan(post.getHashtags().toString())) + "\n" +
                        "Comments:\n";

                if (!post.getComments().isEmpty()) {
                    StringBuilder commentsDetails = new StringBuilder();
                    processComments(post.getComments(), commentsDetails, 1);
                    postDetail += commentsDetails.toString();
                } else {
                    postDetail += CliFormatter.red("    No comments yet.\n");
                }

                post_details.add(postDetail);
            });

            PaginationUtil.paginate(post_details);
        } else {
            System.out.println(CliFormatter.red("\n📸 Posts: No posts yet.\n"));
        }
    }
}
