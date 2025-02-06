package ir.ac.kntu.Meowter.app;

import ir.ac.kntu.Meowter.model.*;
import ir.ac.kntu.Meowter.repository.PostRepository;
import ir.ac.kntu.Meowter.repository.UserRepository;
import ir.ac.kntu.Meowter.service.AdminService;
import ir.ac.kntu.Meowter.service.NotificationService;
import ir.ac.kntu.Meowter.service.PostService;
import ir.ac.kntu.Meowter.service.UserService;
import ir.ac.kntu.Meowter.util.CliFormatter;
import ir.ac.kntu.Meowter.util.HtmlReportGeneratorUtil;
import ir.ac.kntu.Meowter.util.KafkaUtil;
import ir.ac.kntu.Meowter.util.PaginationUtil;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdminMenuHandler {

    private final AdminService adminService = new AdminService();
    private final Scanner scanner = new Scanner(System.in);
    private final PostService postService = new PostService();
    private final UserService userService = new UserService();
    private NotificationService notificationService;
    private final UserRepository userRepository = new UserRepository();
    private final PostRepository postRepository = new PostRepository();

    public AdminMenuHandler() {
        KafkaUtil kafkaUtil = new KafkaUtil(
                "localhost:9092",
                "localhost:9092",
                "notification-group",
                "notifications"
        );

        this.notificationService = new NotificationService(kafkaUtil, "notifications");
    }
    public void displayAdminMenu(User adminUser) {
        System.out.println(CliFormatter.boldGreen("Welcome to the Admin Panel, " + adminUser.getUsername() + "!"));
        while (true) {
            System.out.println(CliFormatter.boldYellow("Admin Menu:"));
            System.out.println(CliFormatter.boldYellow("1. View Users"));
            System.out.println(CliFormatter.boldGreen("2. Add a User"));
            System.out.println(CliFormatter.boldBlue("3. Edit a User"));
            System.out.println(CliFormatter.boldRed("4. Ban a User"));
            System.out.println(CliFormatter.magenta("5. Send notifications"));
            System.out.println(CliFormatter.boldPurple("6. View notifications"));
            System.out.println("7. Generate Reports");
            System.out.println(CliFormatter.boldRed("8. Logout"));

            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    viewAllUsers();
                    break;
                case 2:
                    addUser();
                    break;
                case 3:
                    editUser();
                    break;
                case 4:
                    banUser();
                    break;
                case 5:
                    sendNotifications(adminUser);
                    break;
                case 6:
                    List<String> notificationDetails = new ArrayList<>();

                    List<Notification> notifications = notificationService.getAllNotifications();

                    notifications.forEach(notification -> {
                        String notificationDetail = "Notification ID: #" + CliFormatter.blue(String.valueOf(notification.getId())) + "\n" +
                                "Type: " + CliFormatter.boldGreen(notification.getType()) + "\n" +
                                "Content: " + CliFormatter.boldBlue(notification.getContent()) + "\n" +
                                "Timestamp: " + CliFormatter.yellow(notification.getTimestamp().toString()) + "\n" +
                                "Notifier: " + (notification.getNotifier().isActive() ? CliFormatter.cyan(notification.getNotifier().getUsername()) : CliFormatter.boldRed("Inactive user")) + "\n";

                        notificationDetails.add(notificationDetail);
                    });

                    if (notificationDetails.isEmpty()) {
                        System.out.println(CliFormatter.red("You have no notifications."));
                    } else {
                        PaginationUtil.paginate(notificationDetails);
                    }
                    boolean flag = true;

                    while(flag){
                        System.out.println(CliFormatter.boldBlue("1. Select a notification"));
                        System.out.println(CliFormatter.boldBlue("2. Disable a notification"));
                        System.out.println(CliFormatter.boldPurple("3. Back to main menu"));
                        System.out.print(CliFormatter.green("Choose an option: "));
                        int choice2 = scanner.nextInt();
                        scanner.nextLine();

                        switch (choice2) {
                            case 1:
                                System.out.print("Enter notification ID: ");
                                int notificationid = scanner.nextInt();

                                Notification notification = notificationService.getNotificationById(notificationid);
                                if (notification == null) {
                                    System.out.print(CliFormatter.boldRed("There is no notification!"));
                                    break;
                                }

                                notification.setChecked(true);
                                userRepository.saveNotification(notification);


                                String notificationDetail = "Notification ID: #" + CliFormatter.blue(String.valueOf(notification.getId())) + "\n" +
                                        "Type: " + CliFormatter.boldGreen(notification.getType()) + "\n" +
                                        "Content: " + CliFormatter.boldBlue(notification.getContent()) + "\n" +
                                        "Timestamp: " + CliFormatter.yellow(notification.getTimestamp().toString()) + "\n" +
                                        "Notifier: " + (notification.getNotifier().isActive() ? CliFormatter.cyan(notification.getNotifier().getUsername()) : CliFormatter.boldRed("Inactive user")) + "\n";

                                System.out.println(notificationDetail);
                                System.out.println("- - - - - - - - - -");
                                if (Objects.equals(notification.getType(), "LIKE") || Objects.equals(notification.getType(), "COMMENT")) {
                                    displayPost(postRepository.findById((long) extractId(notification.getContent())));
                                } else if (Objects.equals(notification.getType(), "FOLLOW")) {
                                    displayUserProfile(adminUser, userRepository.findById((long) extractId(notification.getContent())));
                                }
                                break;
                            case 2:
                                System.out.print("Enter notification ID: ");
                                int notificationid2 = scanner.nextInt();

                                Notification notification2 = notificationService.getNotificationById(notificationid2);
                                if (notification2 == null) {
                                    System.out.print(CliFormatter.boldRed("There is no notification!"));
                                    break;
                                }

                                notification2.setActive(false);
                                userRepository.saveNotification(notification2);
                                break;
                            case 3:
                                flag = false;
                                break;
                            default:
                                System.out.println(CliFormatter.red("Invalid option!"));
                                break;
                        }
                    }
                    break;
                case 7:
                    generateReports();
                    break;
                case 8:
                    System.out.println("Exiting Admin Menu...");
                    return;
                default:
                    System.out.println(CliFormatter.red("Invalid option. Please try again."));
            }
        }
    }

    private void displayPost(Post selectedPost) {
        try {
            String postDetail = "Post ID: #" + CliFormatter.blue(String.valueOf(selectedPost.getId())) + "\n" +
                    "Content: " + CliFormatter.boldGreen(selectedPost.getContent()) + "\n" +
                    "Created At: " + CliFormatter.boldBlue(selectedPost.getCreatedAt().toString()) + "\n" +
                    "Likes: " + CliFormatter.yellow(String.valueOf(selectedPost.getLikes().size())) + "\n" +
                    "Hashtags: " + (selectedPost.getHashtags().isEmpty() ? CliFormatter.red("No hashtags") : CliFormatter.cyan(selectedPost.getHashtags().toString())) + "\n" +
                    "Comments:\n";

            if (!selectedPost.getComments().isEmpty()) {
                StringBuilder commentsDetails = new StringBuilder();
                processComments(selectedPost.getComments(), commentsDetails, 1);
                postDetail += commentsDetails.toString();
            } else {
                postDetail += CliFormatter.red("    No comments yet.\n");
            }

            System.out.println(postDetail);
        } catch (Exception e) {
            System.out.println(CliFormatter.boldRed("No post founded."));
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

    public void displayUserProfile(User loggedInUser, User selectedUser) {
        CliFormatter.loadingSpinner(CliFormatter.boldGreen("Getting user information and profile..."));

        StringBuilder profileDetails = new StringBuilder();

        profileDetails.append(CliFormatter.bold("👤 Username: ")).append(selectedUser.getUsername()).append("\n");
        profileDetails.append(CliFormatter.bold("📧 Email: ")).append(selectedUser.getEmail()).append("\n");
        profileDetails.append(CliFormatter.bold("📝 Bio: ")).append(selectedUser.getBio() == null ? CliFormatter.boldRed("No bio provided.") : selectedUser.getBio()).append("\n");
        profileDetails.append(CliFormatter.bold("🎂 Date of Birth: ")).append(selectedUser.getDateofbirth() != null ? selectedUser.getDateofbirth().toLocalDate().toString() : CliFormatter.boldRed("Not provided")).append("\n");
        profileDetails.append(CliFormatter.bold("🔒 Private Profile: ")).append(selectedUser.getIsPrivate() ? CliFormatter.boldGreen("Yes") : CliFormatter.boldRed("No")).append("\n");
        profileDetails.append(CliFormatter.bold("👥 Followers: ")).append(selectedUser.getFollowers().size()).append("\n");
        profileDetails.append(CliFormatter.bold("👣 Following: ")).append(selectedUser.getFollowing().size()).append("\n");
        profileDetails.append(CliFormatter.bold("🛠️ Role: ")).append(selectedUser.getRole()).append("\n");
        profileDetails.append(CliFormatter.bold("✅ Active: ")).append(selectedUser.isActive() ? CliFormatter.boldGreen("Yes") : CliFormatter.boldRed("No")).append("\n");

        System.out.println(profileDetails.toString());

        List<Post> posts = postService.getUserPosts(selectedUser);

        if (selectedUser.getIsPrivate()) {
            System.out.println(CliFormatter.boldRed("This profile is private. Follow to see posts."));
        } else {
            if (!posts.isEmpty()) {
                profileDetails.append(CliFormatter.bold("\n📸 Posts:\n"));
                List<String> post_details = new ArrayList<>();

                posts.forEach(post -> {
                    String postDetail = "Post ID: #" + CliFormatter.blue(String.valueOf(post.getId())) + "\n" + "Content: " + CliFormatter.boldGreen(post.getContent()) + "\n" + "Created At: " + CliFormatter.boldBlue(post.getCreatedAt().toString()) + "\n" + "Likes: " + CliFormatter.yellow(String.valueOf(post.getLikes().size())) + "\n" + "Hashtags: " + (post.getHashtags().isEmpty() ? CliFormatter.red("No hashtags") : CliFormatter.cyan(post.getHashtags().toString())) + "\n" + "Comments:\n";

                    if (!post.getComments().isEmpty()) {
                        StringBuilder commentsDetails = new StringBuilder();
                        post.getComments().forEach(comment -> {
                            if (comment.getUser().isActive()) {
                                commentsDetails.append("    - Comment by ").append(CliFormatter.blue(comment.getUser().getUsername())).append(": ").append(CliFormatter.cyan(comment.getContent()))
                                        .append(" #")
                                        .append(CliFormatter.yellow(comment.getId().toString()))
                                        .append("\n");
                            } else {
                                commentsDetails.append(CliFormatter.boldRed("    - Comment hidden because user is inactive ")).append("\n");
                            }
                        });
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

    public static int extractId(String text) {
        Pattern pattern = Pattern.compile("#(\\d+)");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        } else {
            throw new IllegalArgumentException("No ID found in the text");
        }
    }

    private void viewAllUsers() {
        System.out.println(CliFormatter.boldYellow("List of all users:"));
        List<String> user_details = new ArrayList<>();
        userService.getAllUsers().forEach(user -> {
            if (user.isActive()) {
                StringBuilder profileDetails = new StringBuilder();
                profileDetails.append(CliFormatter.bold("👤 Username: ")).append(user.getUsername()).append("\n");
                profileDetails.append(CliFormatter.bold("📧 Email: ")).append(user.getEmail()).append("\n");
                profileDetails.append(CliFormatter.bold("📝 Bio: ")).append(user.getBio() == null ? CliFormatter.boldRed("No bio provided.") : user.getBio()).append("\n");
                profileDetails.append(CliFormatter.bold("🎂 Date of Birth: ")).append(user.getDateofbirth() != null ? user.getDateofbirth().toLocalDate().toString() : CliFormatter.boldRed("Not provided")).append("\n");
                profileDetails.append(CliFormatter.bold("🔒 Private Profile: ")).append(user.getIsPrivate() ? CliFormatter.boldGreen("Yes") : CliFormatter.boldRed("No")).append("\n");
                profileDetails.append(CliFormatter.bold("👥 Followers: ")).append(user.getFollowers().size()).append("\n");
                profileDetails.append(CliFormatter.bold("👣 Following: ")).append(user.getFollowing().size()).append("\n");
                profileDetails.append(CliFormatter.bold("🛠️ Role: ")).append(user.getRole()).append("\n");
                profileDetails.append(CliFormatter.bold("✅ Active: ")).append(user.isActive() ? CliFormatter.boldGreen("Yes") : CliFormatter.boldRed("No")).append("\n");
                user_details.add(profileDetails.toString());
            }
        });
        PaginationUtil.paginate(user_details);
    }

    private void addUser() {
        System.out.println(CliFormatter.boldYellow("Add a New User"));
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        System.out.print("Enter role (USER/ADMIN/SUPPORT): ");
        Role role = Role.valueOf(scanner.nextLine().toUpperCase());

        if (userService.createUser(username, email, password, role)){
            System.out.println(CliFormatter.boldGreen("User added successfully!"));
        } else {
            System.out.println(CliFormatter.boldYellow("Something went wrong!"));
        }

    }

    private void sendNotifications(User loggedInUser) {
        System.out.print("Enter notification message: ");
        String content = scanner.nextLine();
        for(User user : userService.getAllUsers()) {
            notificationService.sendNotification(loggedInUser, user, "ADMIN", content);
        }
        System.out.println(CliFormatter.boldGreen("Notifications sent to all users successfully!"));
    }

    private void editUser() {
        System.out.print("Enter the username of the user to edit: ");
        String username = scanner.nextLine();
        User user = userService.searchUserByUsername(username);
        if (user == null) {
            System.out.println(CliFormatter.red("User not found."));
            return;
        }
        System.out.println(CliFormatter.boldGreen("Editing user: " + user.getUsername()));
        System.out.println(CliFormatter.boldPurple("1. Update Username"));
        System.out.println(CliFormatter.boldRed("2. Update Password"));
        System.out.println(CliFormatter.cyan("3. Update Bio"));
        System.out.println(CliFormatter.magenta("4. Update Labels"));
        System.out.println(CliFormatter.yellow("5. Update Privacy Setting"));
        if (user.getRole() == Role.USER) {
            System.out.println(CliFormatter.yellow("6. Update Department Setting"));
        }
        System.out.println(CliFormatter.magenta("7. Return to main menu"));
        System.out.print("Choose an option: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                System.out.print("Enter new username: ");
                String newUsername = scanner.nextLine();
                userService.updateUsername(user, newUsername);
                break;
            case 2:
                System.out.print("Enter new password: ");
                String newPassword = scanner.nextLine();
                userService.updatePassword(user, newPassword);
                break;
            case 3:
                System.out.print("Enter new bio: ");
                String newBio = scanner.nextLine();
                userService.updateBio(user, newBio);
                break;
            case 4:
                System.out.print("Enter new labels (comma-separated): ");
                String labels = scanner.nextLine();
                userService.setLabels(user, labels);
                break;
            case 5:
                System.out.print("Set user privacy (true/false): ");
                boolean isPrivate = scanner.nextBoolean();
                userService.updatePrivacySetting(user, isPrivate);
                break;
            case 6:
                System.out.print("Enter new departments (comma-separated): ");
                String departments = scanner.nextLine();
                userService.setDepartments(user, departments);
                break;
            case 7:
                return;
            default:
                System.out.println(CliFormatter.red("Invalid option."));
                break;
        }
    }

    private void banUser() {
        System.out.print("Enter the username of the user to ban: ");
        String username = scanner.nextLine();
        User user = userService.searchUserByUsername(username);

        if (user == null) {
            System.out.println(CliFormatter.red("User not found."));
            return;
        }

        user.setActive(false);
        userRepository.update(user);
        System.out.println(CliFormatter.green("User " + username + " has been banned successfully."));
    }

    public void displayUserProfile(User selectedUser) {
        CliFormatter.loadingSpinner(CliFormatter.boldGreen("Getting user information and profile..."));
        StringBuilder profileDetails = new StringBuilder();
        profileDetails.append(CliFormatter.bold("👤 Username: ")).append(selectedUser.getUsername()).append("\n");
        profileDetails.append(CliFormatter.bold("📧 Email: ")).append(selectedUser.getEmail()).append("\n");
        profileDetails.append(CliFormatter.bold("📝 Bio: ")).append(selectedUser.getBio() == null ? CliFormatter.boldRed("No bio provided.") : selectedUser.getBio()).append("\n");
        profileDetails.append(CliFormatter.bold("🎂 Date of Birth: ")).append(selectedUser.getDateofbirth() != null ? selectedUser.getDateofbirth().toLocalDate().toString() : CliFormatter.boldRed("Not provided")).append("\n");
        profileDetails.append(CliFormatter.bold("🔒 Private Profile: ")).append(selectedUser.getIsPrivate() ? CliFormatter.boldGreen("Yes") : CliFormatter.boldRed("No")).append("\n");
        profileDetails.append(CliFormatter.bold("👥 Followers: ")).append(selectedUser.getFollowers().size()).append("\n");
        profileDetails.append(CliFormatter.bold("👣 Following: ")).append(selectedUser.getFollowing().size()).append("\n");
        profileDetails.append(CliFormatter.bold("🛠️ Role: ")).append(selectedUser.getRole()).append("\n");
        profileDetails.append(CliFormatter.bold("✅ Active: ")).append(selectedUser.isActive() ? CliFormatter.boldGreen("Yes") : CliFormatter.boldRed("No")).append("\n");

        System.out.println(profileDetails.toString());

        List<Post> posts = postService.getUserPosts(selectedUser);

        if (selectedUser.getIsPrivate()) {
            System.out.println(CliFormatter.boldRed("This profile is private. Follow to see posts."));
        } else {
            if (!posts.isEmpty()) {
                profileDetails.append(CliFormatter.bold("\n📸 Posts:\n"));
                List<String> post_details = new ArrayList<>();

                posts.forEach(post -> {
                    String postDetail = "Post ID: #" + CliFormatter.blue(String.valueOf(post.getId())) + "\n" + "Content: " + CliFormatter.boldGreen(post.getContent()) + "\n" + "Created At: " + CliFormatter.boldBlue(post.getCreatedAt().toString()) + "\n" + "Likes: " + CliFormatter.yellow(String.valueOf(post.getLikes().size())) + "\n" + "Hashtags: " + (post.getHashtags().isEmpty() ? CliFormatter.red("No hashtags") : CliFormatter.cyan(post.getHashtags().toString())) + "\n" + "Comments:\n";
                    if (!post.getComments().isEmpty()) {
                        StringBuilder commentsDetails = new StringBuilder();
                        post.getComments().forEach(comment -> {
                            if (comment.getUser().isActive()) {
                                commentsDetails.append("    - Comment by ").append(CliFormatter.blue(comment.getUser().getUsername())).append(": ").append(CliFormatter.cyan(comment.getContent())).append(" #").append(CliFormatter.yellow(comment.getId().toString())).append("\n");
                            } else {
                                commentsDetails.append(CliFormatter.boldRed("    - Comment hidden because user is inactive ")).append("\n");
                            }
                        });
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

    private void generateReports() {
        System.out.println(CliFormatter.boldYellow("Generate Reports Menu:"));
        System.out.println("1. Generate Users Report");
        System.out.println("2. Generate Posts Report");
        System.out.println("3. Back");
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
                return;
            default:
                System.out.println(CliFormatter.red("Invalid option. Please try again."));
        }
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
