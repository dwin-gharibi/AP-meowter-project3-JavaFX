package ir.ac.kntu.Meowter.app;

import ir.ac.kntu.Meowter.controller.*;
import ir.ac.kntu.Meowter.exceptions.InvalidCommandException;
import ir.ac.kntu.Meowter.model.*;
import ir.ac.kntu.Meowter.repository.PostRepository;
import ir.ac.kntu.Meowter.repository.UserRepository;
import ir.ac.kntu.Meowter.service.NotificationService;
import ir.ac.kntu.Meowter.service.PostService;
import ir.ac.kntu.Meowter.service.TicketService;
import ir.ac.kntu.Meowter.service.UserService;
import ir.ac.kntu.Meowter.util.CliFormatter;
import ir.ac.kntu.Meowter.util.DateConverter;
import ir.ac.kntu.Meowter.util.KafkaUtil;
import ir.ac.kntu.Meowter.util.PaginationUtil;
import net.sf.saxon.event.MessageWarner;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MenuHandler {

    private UserService userService;
    private TicketController ticketController;
    private UserController userController;
    private PostController postController;
    private PostService postService;
    private MessageController messageController;
    private NotificationService notificationService;
    private UserRepository userRepository;
    private PostRepository postRepository;

    public MenuHandler() {
        this.userService = new UserService();
        this.ticketController = new TicketController();
        this.userController = new UserController();
        this.postController = new PostController();
        this.postService = new PostService();
        this.userRepository = new UserRepository();
        this.messageController = new MessageController();
        this.postRepository = new PostRepository();
        KafkaUtil kafkaUtil = new KafkaUtil(
                "localhost:9092",
                "localhost:9092",
                "notification-group",
                "notifications"
        );

        this.notificationService = new NotificationService(kafkaUtil, "notifications");
    }

    public void displayMainMenu(User loggedInUser, Role role) {
        Scanner scanner = new Scanner(System.in);

        if (role == Role.USER) {
            displayUserMenu(loggedInUser);
        } else if (role == Role.ADMIN) {
            displayAdminMenu(loggedInUser);
        } else if (role == Role.SUPPORT) {
            displaySupportUserMenu(loggedInUser);
        }
    }

    private void displaySupportUserMenu(User loggedInUser) {
    }

    private void displayAdminMenu(User loggedInUser) {
    }

    public void displayUserMenu(User loggedInUser) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println(CliFormatter.bold("User Menu:") + "\n" + CliFormatter.boldYellow("0. Home"));
            System.out.println(CliFormatter.boldYellow("1. Settings") + "\n" + CliFormatter.boldRed("2. Support Section"));
            System.out.println(CliFormatter.green("3. Users Section") + "\n" + CliFormatter.magenta("4. Posts Section"));
            System.out.println(CliFormatter.cyan("5. User Profile") + "\n" + CliFormatter.red("6. Log out"));
            System.out.println(CliFormatter.blue("7. Notifications"));
            System.out.println(CliFormatter.blue("8. Messages"));
            System.out.println(CliFormatter.blue("9. Exit"));

            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            try{
                switch (choice) {
                    case 0:
                        displayHome(loggedInUser);
                        break;
                    case 1:
                        displaySettings(loggedInUser);
                        break;
                    case 2:
                        ticketController.displayTicketSection(loggedInUser);
                        break;
                    case 3:
                        userController.displayUsersSection(loggedInUser);
                        break;
                    case 4:
                        postController.displayPostsSection(loggedInUser);
                        break;
                    case 5:
                        userController.displayProfile(loggedInUser);
                        break;
                    case 6:
                        loggedInUser = null;
                        System.out.println("You have logged out.");
                        return;
                    case 7:
                        List<String> notificationDetails = new ArrayList<>();

                        List<Notification> notifications = notificationService.getNotifications(loggedInUser);

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
                            System.out.println(CliFormatter.boldPurple("2. Back to main menu"));
                            System.out.print(CliFormatter.green("Choose an option: "));
                            int choice2 = scanner.nextInt();
                            scanner.nextLine();

                            switch (choice2) {
                                case 1:
                                    System.out.print("Enter notification ID: ");
                                    int notificationid = scanner.nextInt();

                                    Notification notification = notificationService.getNotificationById(notificationid);
                                    if (notification == null || notification.getNotifiee().getId() != loggedInUser.getId()) {
                                        System.out.print(CliFormatter.boldRed("Notification not found or not related to you!"));
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
                                        displayUserProfile(loggedInUser, userRepository.findById((long) extractId(notification.getContent())));
                                    } else if (Objects.equals(notification.getType(), "MESSAGE")) {
                                        MessageController messageController = new MessageController();
                                        messageController.viewConversation(notification.getNotifiee(), notification.getNotifier());
                                    }
                                    break;
                                case 2:
                                    flag = false;
                                    break;
                                default:
                                    break;
                            }
                        }
                    case 8:
                        messageController.start(loggedInUser);
                        break;
                    case 9:
                        System.out.println("Goodbye!");
                        System.exit(0);
                        break;
                    default:
                        throw new InvalidCommandException("Invalid option! Please try again.");
                }
            } catch (Exception e) {
                System.out.println(CliFormatter.boldRed(e.getMessage()));
            }
        }
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

    public void displayHome(User loggedInUser) {
        Scanner scanner = new Scanner(System.in);
        LocalDateTime start_date = null;
        LocalDateTime end_date = null;
        List<String> sortBy = new ArrayList<>();
        boolean sortOrder = true;
        boolean showOnlyFollowersPosts = false;

        while (true) {
            showHomeMenu();

            System.out.print(CliFormatter.green("Choose an option: "));
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    subscribeToPublisher();
                    break;

                case 2:
                    viewFollowingsPosts(loggedInUser, scanner, start_date, end_date, sortBy, sortOrder, showOnlyFollowersPosts);
                    break;

                case 3:
                    LocalDateTime[] dates = setDateFilter(scanner);
                    start_date = dates[0];
                    end_date = dates[1];
                    break;

                case 4:
                    System.out.print("Enter new labels: ");
                    System.out.print(CliFormatter.boldBlue("Available Labels: #SPORT, #ART, #TECHNOLOGY, #TRAVEL, #FOOD, #ENTERTAINMENT, #EDUCATION\n"));
                    String newLabels = scanner.nextLine();
                    userService.setLabels(loggedInUser, newLabels);
                    CliFormatter.printTypingEffect(CliFormatter.boldGreen("Favorite labels updated successfully."));
                    break;

                case 5:
                    sortBy = setSortingFilter(scanner);
                    break;

                case 6:
                    sortOrder = setSortingOrder(scanner);
                    break;

                case 7:
                    showOnlyFollowersPosts = setFollowerFilter(scanner);
                    break;

                case 8:
                    return;

                default:
                    System.out.println(CliFormatter.boldRed("Invalid option. Please try again."));
            }
        }
    }

    private List<String> setSortingFilter(Scanner scanner) {
        System.out.print("Enter sorting criteria (comma-separated: date, likes, comments): ");
        String input = scanner.nextLine();
        List<String> criteria = Arrays.stream(input.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> s.equals("date") || s.equals("likes") || s.equals("comments"))
                .collect(Collectors.toList());

        if (criteria.isEmpty()) {
            System.out.println(CliFormatter.boldRed("Invalid criteria. Defaulting to date."));
            return Collections.singletonList("date");
        }

        return criteria;
    }

    private boolean setSortingOrder(Scanner scanner) {
        System.out.print("Enter sorting order (asc/desc): ");
        String input = scanner.nextLine().trim().toLowerCase();
        if (input.equals("desc")) {
            return false;
        }
        return true;
    }

    private boolean setFollowerFilter(Scanner scanner) {
        System.out.print("Show only posts from users who follow you? (yes/no): ");
        String input = scanner.nextLine().trim().toLowerCase();
        return input.equals("yes");
    }


    private void showHomeMenu() {
        System.out.println(CliFormatter.boldYellow("Home Menu:"));
        System.out.println(CliFormatter.boldGreen("1. Subscribe to publisher"));
        System.out.println(CliFormatter.boldBlue("2. View followings posts"));
        System.out.println(CliFormatter.boldRed("3. Change date filter"));
        System.out.println(CliFormatter.boldRed("4. Set favorite labels"));
        System.out.println(CliFormatter.cyan("5. Set sorting criteria (date, likes, comments)"));
        System.out.println(CliFormatter.magenta("6. Set sorting order (asc/desc)"));
        System.out.println(CliFormatter.boldPurple("7. Toggle showing only posts from followers"));
        System.out.println(CliFormatter.boldPurple("8. Go Back"));
    }


    private void subscribeToPublisher() {
        CliFormatter.progressBar(CliFormatter.boldYellow("Subscribing to posts ..."), 10);
        CliFormatter.printTypingEffect(CliFormatter.boldGreen("Listening for posts..."));
        postService.subscribeToPosts();
    }

    private void viewFollowingsPosts(User loggedInUser, Scanner scanner, LocalDateTime start_date, LocalDateTime end_date,
                                     List<String> sortBy, boolean sortOrder, boolean showOnlyFollowersPosts) {
        List<Post> posts = postService.getPostsFromFollowings(loggedInUser);
        CliFormatter.progressBar(CliFormatter.boldGreen("Loading the posts ..."), 10);

        if (posts.isEmpty()) {
            System.out.println(CliFormatter.boldRed("No posts found!"));
            return;
        }

        Date start_date_new = start_date != null ? Date.from(start_date.atZone(ZoneId.systemDefault()).toInstant()) : null;
        Date end_date_new = end_date != null ? Date.from(end_date.atZone(ZoneId.systemDefault()).toInstant()) : null;

        posts = posts.stream()
                .filter(post -> loggedInUser.getUser_labels().isEmpty() ||
                        post.getLabels().stream().anyMatch(loggedInUser.getUser_labels()::contains))
                .filter(post -> (start_date_new == null || !post.getCreatedAt().before(start_date_new)) &&
                        (end_date_new == null || !post.getCreatedAt().after(end_date_new)))
                .filter(post -> !showOnlyFollowersPosts || post.getUser().getFollowers().contains(loggedInUser))
                .sorted((p1, p2) -> {
                    for (String criteria : sortBy) {
                        int comparison = 0;
                        switch (criteria.toLowerCase()) {
                            case "likes":
                                comparison = Integer.compare(p1.getLikes().size(), p2.getLikes().size());
                                break;
                            case "comments":
                                comparison = Integer.compare(p1.getComments().size(), p2.getComments().size());
                                break;
                            case "date":
                                comparison = p1.getCreatedAt().compareTo(p2.getCreatedAt());
                                break;
                            default:
                                break;
                        }
                        if (comparison != 0) {
                            return sortOrder ? comparison : -comparison;
                        }
                    }
                    return 0;
                })
                .collect(Collectors.toList());

        posts.forEach(this::displayPost);
        handlePostRequests(loggedInUser, scanner);
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
            commentsDetails.append(indentation).append("- Comment by ").append(CliFormatter.blue(comment.getUser().getUsername())).append(": ").append(CliFormatter.cyan(comment.getContent())).append(" #").append(CliFormatter.yellow(comment.getId().toString())).append("\n");
            if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
                processComments(comment.getReplies(), commentsDetails, level + 1);
            }
        });
    }

    private void handlePostRequests(User loggedInUser, Scanner scanner) {
        while (true) {
            System.out.println(CliFormatter.boldYellow("1. Handle Requests (L[id], C[id], R[id] #[hashtag])"));
            System.out.println(CliFormatter.boldPurple("2. Back to Main Menu"));
            System.out.print(CliFormatter.magenta("Choose an option: "));
            int choice_request = scanner.nextInt();
            scanner.nextLine();
            switch (choice_request) {
                case 1:
                    postController.handleRequests(loggedInUser, scanner);
                    break;
                case 2:
                    return;
                default:
                    System.out.println(CliFormatter.boldRed("Invalid option. Try again."));
            }
        }
    }

    private LocalDateTime[] setDateFilter(Scanner scanner) {
        System.out.print("Enter date filters: (YYYY-mm-dd|YYYY-mm-dd) \nNote: They can also be empty for open ranges.\n");
        String dateStr = scanner.nextLine();
        if (!dateStr.contains("|")) {
            System.out.println(CliFormatter.boldRed("Invalid date format."));
            return new LocalDateTime[]{null, null};
        }
        String start = dateStr.split("\\|")[0].trim();
        String end = dateStr.split("\\|")[1].trim();
        LocalDateTime start_date = start.isEmpty() ? null : DateConverter.convertStringToDate(start);
        LocalDateTime end_date = end.isEmpty() ? null : DateConverter.convertStringToDate(end);
        CliFormatter.progressBar(CliFormatter.boldYellow("Setting date filters ..."), 5);
        return new LocalDateTime[]{start_date, end_date};
    }


    public void displaySettings(User loggedInUser) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Settings for: " + CliFormatter.boldGreen(loggedInUser.getUsername()));
            System.out.println("1. Change Username (" + CliFormatter.boldBlue(loggedInUser.getUsername()) + ")");
            System.out.println("2. Change Password (" + CliFormatter.boldYellow(loggedInUser.getPassword()) + ")");
            System.out.println("3. Change Privacy Setting (" + (loggedInUser.getIsPrivate() ? CliFormatter.boldGreen("Yes") : CliFormatter.boldRed("No") )+ ")");
            System.out.println(CliFormatter.boldRed("4. Go Back"));

            System.out.print(CliFormatter.green("Choose an option: "));
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Enter new username: ");
                    String newUsername = scanner.nextLine();
                    loggedInUser = userService.updateUsername(loggedInUser, newUsername);
                    CliFormatter.printTypingEffect(CliFormatter.boldGreen("Username updated successfully."));
                    break;

                case 2:
                    System.out.print("Enter new password: ");
                    String newPassword = scanner.nextLine();
                    loggedInUser = userService.updatePassword(loggedInUser, newPassword);
                    CliFormatter.printTypingEffect(CliFormatter.boldGreen("Password updated successfully."));
                    break;

                case 3:
                    System.out.print("Make your profile private? (true/false): ");
                    boolean isPrivate = scanner.nextBoolean();
                    scanner.nextLine();
                    loggedInUser = userService.updatePrivacySetting(loggedInUser, isPrivate);
                    CliFormatter.printTypingEffect(CliFormatter.boldGreen("Privacy setting updated successfully."));
                    break;
                case 4:
                    return;
                default:
                    System.out.println(CliFormatter.boldRed("Invalid option. Please try again."));
            }
        }
    }

}
