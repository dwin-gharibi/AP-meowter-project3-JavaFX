package ir.ac.kntu.Meowter.service;

import ir.ac.kntu.Meowter.model.User;
import ir.ac.kntu.Meowter.model.Post;
import ir.ac.kntu.Meowter.repository.PostRepository;
import ir.ac.kntu.Meowter.repository.UserRepository;

import java.util.List;

public class ReportService {

    private UserRepository userRepository;
    private PostRepository postRepository;

    public ReportService() {
        this.userRepository = new UserRepository();
        this.postRepository = new PostRepository();
    }

    public void generateUserReports() {
        List<User> users = userRepository.findAll();
        System.out.println("Generating User Report...");

        for (User user : users) {
            System.out.println("User: " + user.getUsername());
            System.out.println("Email: " + user.getEmail());
            System.out.println("Role: " + user.getRole());
            System.out.println("Active: " + user.isActive());
            System.out.println("-----------");
        }
    }

    public void generatePostReports() {
        List<Post> posts = postRepository.findAll();
        System.out.println("Generating Post Report...");

        for (Post post : posts) {
            System.out.println("Post: " + post.getContent());
            System.out.println("User: " + post.getUser().getUsername());
            System.out.println("Created At: " + post.getCreatedAt());
            System.out.println("-----------");
        }
    }

    public void generateSystemReports() {
        long totalUsers = userRepository.count();
        long totalPosts = postRepository.count();
        System.out.println("Generating System Report...");

        System.out.println("Total Users: " + totalUsers);
        System.out.println("Total Posts: " + totalPosts);
        System.out.println("-----------");
    }
}

