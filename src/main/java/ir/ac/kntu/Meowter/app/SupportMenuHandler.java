package ir.ac.kntu.Meowter.app;

import ir.ac.kntu.Meowter.model.Post;
import ir.ac.kntu.Meowter.model.User;
import ir.ac.kntu.Meowter.service.PostService;
import ir.ac.kntu.Meowter.service.SupportService;
import ir.ac.kntu.Meowter.service.UserService;
import ir.ac.kntu.Meowter.util.CliFormatter;
import ir.ac.kntu.Meowter.controller.*;
import ir.ac.kntu.Meowter.util.HtmlReportGeneratorUtil;


import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SupportMenuHandler {

    private final SupportService supportService = new SupportService();
    private final Scanner scanner = new Scanner(System.in);

    private UserService userService = new UserService();
    private TicketController ticketController = new TicketController();
    private UserController userController = new UserController();
    private PostController postController = new PostController();
    private final PostService postService = new PostService();

    public void displaySupportMenu(User supportUser) {
        System.out.println(CliFormatter.boldGreen("Welcome to the Support Panel, " + supportUser.getUsername() + "!"));
        while (true) {
            CliFormatter.printTypingEffect(CliFormatter.boldYellow("Support Menu:"));
            System.out.println(CliFormatter.boldRed("1. View All Reports"));
            System.out.println(CliFormatter.boldPurple("2. Respond to a Report"));
            System.out.println(CliFormatter.boldGreen("3. Find a User"));
            System.out.println(CliFormatter.boldRed("4. Logout"));

            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    viewAllReports();
                    break;
                case 2:
                    ticketController.displayTicketSection(supportUser);
                    break;
                case 3:
                    userController.displayUsersSection(supportUser);
                    break;
                case 4:
                    System.out.println("Exiting Support Menu...");
                    return;
                default:
                    System.out.println(CliFormatter.red("Invalid option. Please try again."));
            }
        }
    }

    private void viewAllReports() {
        System.out.println(CliFormatter.boldYellow("Generate Reports Menu:"));
        System.out.println("1. Generate Users Report");
        System.out.println("2. Generate Posts Report");
        System.out.println("3. Generate Comments Report");
        System.out.println("4. Back");

        System.out.print("Choose an option: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                generateUsersReport();
                break;
            case 2:
                generatePostsReport();
                break;
            case 3:
                break;
            case 4:
                return;
            default:
                System.out.println(CliFormatter.red("Invalid option. Please try again."));
        }
    }

    private void respondToReport() {
        System.out.print("Enter the Report ID to respond to: ");
    }

    private void assistUser() {
        System.out.print("Enter the username of the user to assist: ");
    }

    private void generateUsersReport() {
        List<User> users = userService.getAllUsers();
        List<String> headers = List.of("User ID", "Username", "Email", "Status");
        List<List<String>> data = new ArrayList<>();

        for (User user : users) {
            data.add(List.of(
                    String.valueOf(user.getId()),
                    user.getUsername(),
                    user.getEmail(),
                    user.isActive() ? "Active" : "Deactive"
            ));
        }

        String reportPath = HtmlReportGeneratorUtil.generateReport("Users Report", headers, data);
        System.out.println(CliFormatter.green("Users Report generated: " + reportPath));
    }

    private void generatePostsReport() {
        List<Post> posts = postService.getAllPosts();
        List<String> headers = List.of("Post ID", "Author", "Content", "Created Date");
        List<List<String>> data = new ArrayList<>();

        posts.forEach(post -> {
            data.add(List.of(
                    String.valueOf(post.getId()),
                    post.getUser().getUsername(),
                    post.getContent(),
                    post.getCreatedAt().toString()
            ));
        });

        String reportPath = HtmlReportGeneratorUtil.generateReport("Posts Report", headers, data);
        System.out.println(CliFormatter.green("Posts Report generated: " + reportPath));
    }
}
