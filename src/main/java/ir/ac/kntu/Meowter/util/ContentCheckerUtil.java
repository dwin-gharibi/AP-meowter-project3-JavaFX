package ir.ac.kntu.Meowter.util;

import ir.ac.kntu.Meowter.model.Comment;
import ir.ac.kntu.Meowter.model.Message;
import ir.ac.kntu.Meowter.model.Post;
import ir.ac.kntu.Meowter.repository.UserRepository;
import ir.ac.kntu.Meowter.service.MessageService;
import ir.ac.kntu.Meowter.service.PostService;
import ir.ac.kntu.Meowter.service.UserService;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ContentCheckerUtil {
    private final PostService postService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ScheduledExecutorService scheduler;

    public ContentCheckerUtil(PostService postService) {
        this.postService = postService;
        this.userService = new UserService();
        this.userRepository = new UserRepository();
        this.scheduler = Executors.newScheduledThreadPool(5);
    }

    public void startChecking() {
        scheduler.scheduleAtFixedRate(this::checkAllContent, 0, 5, TimeUnit.SECONDS);
    }

    private void checkAllContent() {
        List<Post> posts = postService.getAllPosts();
        List<Comment> comments = postService.getAllComments();

        posts.parallelStream().forEach(this::checkAndCleanPost);
        comments.parallelStream().forEach(this::checkAndCleanComment);
    }

    private void checkAndCleanPost(Post post) {
        if (BlacklistUtil.containsBlacklistedWord(post.getContent()) || ContentModerationUtil.containsHarmfulContent(post.getContent())) {
            postService.deletePost(post.getUser(), post.getId());
            post.getUser().setActive(false);
            userRepository.update(post.getUser());
            System.out.println("⚠️ Post by " + post.getUser().getUsername() + " was removed due to violations.");
        }
    }

    private void checkAndCleanComment(Comment comment) {
        if (BlacklistUtil.containsBlacklistedWord(comment.getContent()) || ContentModerationUtil.containsHarmfulContent(comment.getContent())) {
            postService.removeComment(comment.getUser(), comment.getPost().getId(), comment.getId());
            comment.getUser().setActive(false);
            userRepository.update(comment.getUser());
            System.out.println("⚠️ Comment by " + comment.getUser().getUsername() + " was removed due to violations.");
        }
    }

}
