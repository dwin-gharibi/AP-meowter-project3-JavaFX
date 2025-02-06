package ir.ac.kntu.Meowter.controller;

import ir.ac.kntu.Meowter.app.AdminMenuHandler;
import ir.ac.kntu.Meowter.app.MenuHandler;
import ir.ac.kntu.Meowter.app.SupportMenuHandler;
import ir.ac.kntu.Meowter.model.*;
import ir.ac.kntu.Meowter.service.PostService;
import ir.ac.kntu.Meowter.service.SessionManager;
import ir.ac.kntu.Meowter.service.UserService;
import ir.ac.kntu.Meowter.util.CliFormatter;
import ir.ac.kntu.Meowter.util.PaginationUtil;
import ir.ac.kntu.Meowter.util.DateConverter;
import ir.ac.kntu.Meowter.util.SearchUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class UserController {

    private PostService postService;
    private UserService userService;

    public UserController() {
        this.postService = new PostService();
        this.userService = new UserService();
    }

    public void displayProfile(User LoggedInUser) {
        CliFormatter.loadingSpinner(CliFormatter.boldGreen("Getting user information and profile..."));
        StringBuilder profileDetails = new StringBuilder();
        profileDetails.append(CliFormatter.bold("👤 Username: ")).append(LoggedInUser.getUsername()).append("\n");
        profileDetails.append(CliFormatter.bold("📧 Email: ")).append(LoggedInUser.getEmail()).append("\n");
        profileDetails.append(CliFormatter.bold("📝 Bio: ")).append(LoggedInUser.getBio() == null ? CliFormatter.boldRed("No bio provided.") : LoggedInUser.getBio()).append("\n");
        profileDetails.append(CliFormatter.bold("🎂 Date of Birth: ")).append(LoggedInUser.getDateofbirth() != null ? LoggedInUser.getDateofbirth().toLocalDate().toString() : CliFormatter.boldRed("Not provided")).append("\n");
        profileDetails.append(CliFormatter.bold("🔒 Private Profile: ")).append(LoggedInUser.getIsPrivate() ? CliFormatter.boldGreen("Yes") : CliFormatter.boldRed("No")).append("\n");
        profileDetails.append(CliFormatter.bold("👥 Followers: ")).append(LoggedInUser.getFollowers().size()).append("\n");
        profileDetails.append(CliFormatter.bold("👣 Following: ")).append(LoggedInUser.getFollowing().size()).append("\n");
        profileDetails.append(CliFormatter.bold("🛠️ Role: ")).append(LoggedInUser.getRole()).append("\n");
        profileDetails.append(CliFormatter.bold("✅ Active: ")).append(LoggedInUser.isActive() ? CliFormatter.boldGreen("Yes") : CliFormatter.boldRed("No")).append("\n");

        System.out.println(profileDetails.toString());
        List<Post> posts = postService.getUserPosts(LoggedInUser);

        if (!posts.isEmpty()) {
            profileDetails.append(CliFormatter.bold("\n📸 Posts:\n"));
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
        displaySettings(LoggedInUser);
    }

    private void displaySettings(User LoggedInUser) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            CliFormatter.printTypingEffect(CliFormatter.boldYellow("Loading user settings..."));
            System.out.println(CliFormatter.green("Profile edit for: " + LoggedInUser.getUsername()));
            System.out.println(CliFormatter.red("1. Edit or add Biography"));
            System.out.println(CliFormatter.yellow("2. Edit or add BirthDate"));
            System.out.println(CliFormatter.cyan("3. Go Back"));

            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            try{
                switch (choice) {
                    case 1:
                        System.out.print("Enter new Biography: ");
                        String newBio = scanner.nextLine();
                        LoggedInUser = userService.updateBio(LoggedInUser, newBio);
                        CliFormatter.loadingSpinner("Biography updated successfully.");
                        break;
                    case 2:
                        System.out.print("Enter new BirthDate(YYYY-MM-DD) : ");
                        String newDate = scanner.nextLine();
                        LoggedInUser = userService.updateDateOfBirth(LoggedInUser, DateConverter.convertStringToDate(newDate));
                        CliFormatter.loadingSpinner("Birthdate updated successfully.");
                        break;
                    case 3:
                        return;
                    default:
                        System.out.println(CliFormatter.boldRed("Invalid option. Please try again."));
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public void displayUsersSection(User loggedInUser) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            CliFormatter.printTypingEffect(CliFormatter.boldYellow("Welcome to users section:"));
            displayMenuOptions(loggedInUser);
            String input = scanner.nextLine();

            if (input.startsWith("#")) {
                handleFollowRequest(loggedInUser, input.substring(1));
                continue;
            }

            int choice = getUserChoice(input);
            if (choice == -1) {
                continue;
            }

            switch (choice) {
                case 1:
                    displayFollowers(loggedInUser);
                    break;
                case 2:
                    displayFollowings(loggedInUser);
                    break;
                case 3:
                    searchUsers(loggedInUser);
                    break;
                case 4:
                    viewFollowRequests(loggedInUser);
                    break;
                case 5:
                    return;
                default:
                    System.out.println(CliFormatter.boldRed("Invalid option. Try again."));
                    break;
            }
        }
    }

    private void displayMenuOptions(User loggedInUser) {
        if (loggedInUser.getRole() != Role.SUPPORT) {
            System.out.println(CliFormatter.boldGreen("    - You can also type #username to immediately send follow request."));
            System.out.println(CliFormatter.boldPurple("1. View Followers") + "\n" + CliFormatter.boldGreen("2. View Followings"));
        }
        System.out.println(CliFormatter.boldBlue("3. Search Users"));
        if (loggedInUser.getRole() != Role.SUPPORT) {
            System.out.println(CliFormatter.boldYellow("4. View Follow Requests (Received & Sent)"));
        }
        System.out.println(CliFormatter.boldPurple("5. Back to Main Menu"));
        System.out.print(CliFormatter.cyan("Choose an option: "));
    }

    private int getUserChoice(String input) {
        int choice = -1;
        try {
            choice = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println(CliFormatter.boldRed("Invalid option. Please try again."));
        }
        return choice;
    }

    private void handleFollowRequest(User loggedInUser, String targetUsername) {
        User recipientUser = userService.searchUserByUsername(targetUsername);
        if (recipientUser != null) {
            CliFormatter.progressBar(CliFormatter.boldBlue("Sending follow request..."), 5);
            userService.sendFollowRequest(loggedInUser, recipientUser);
        } else {
            System.out.println(CliFormatter.boldRed("User not found."));
        }
    }

    private void displayFollowers(User loggedInUser) {
        Set<User> followers = loggedInUser.getFollowers();
        if (!followers.isEmpty()) {
            List<String> follower_details = new ArrayList<>();
            followers.forEach(follower -> {
                if (follower.isActive()) {
                    String UserDetail = "Username: @" + CliFormatter.blue(follower.getUsername()) + "\n";
                    follower_details.add(UserDetail);
                }
            });
            PaginationUtil.paginate(follower_details);
            selectUserFromList(loggedInUser);
        } else {
            System.out.println(CliFormatter.red("\nNo followers found.\n"));
        }
    }

    private void displayFollowings(User loggedInUser) {
        Set<User> followings = loggedInUser.getFollowing();
        if (!followings.isEmpty()) {
            List<String> following_details = new ArrayList<>();
            followings.forEach(following -> {
                if (following.isActive()) {
                    String UserDetail = "Username: @" + CliFormatter.blue(following.getUsername()) + "\n";
                    following_details.add(UserDetail);
                }
            });
            PaginationUtil.paginate(following_details);
            selectUserFromList(loggedInUser);
        } else {
            System.out.println(CliFormatter.red("\nNo followings found.\n"));
        }
    }

    private void selectUserFromList(User loggedInUser) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println(CliFormatter.green("You can select a user by typing @username or back:"));
            System.out.print(CliFormatter.cyan("Choose an option: "));
            String choice_user = scanner.nextLine();
            if (choice_user.startsWith("@")) {
                String targetUsername = choice_user.substring(1);
                User recipientUser = userService.searchUserByUsername(targetUsername);
                if (recipientUser != null) {
                    displayUserProfile(loggedInUser, recipientUser);
                } else {
                    System.out.println(CliFormatter.boldRed("User not found."));
                }
            } else {
                break;
            }
        }
    }

    private void searchUsers(User loggedInUser) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter username to search: ");
        String searchTerm = scanner.nextLine();
        List<String> usernames = new ArrayList<>();
        List<User> users = userService.getAllUsers();
        users.forEach(user -> usernames.add(user.getUsername()));
        List<String> users_list = SearchUtil.search(searchTerm, usernames);

        if (users_list.isEmpty()) {
            System.out.println(CliFormatter.red("No users found."));
        } else {
            for (String user : users_list) {
                User userInfo = userService.searchUserByUsername(user);
                displayUserProfile(loggedInUser, userInfo);
            }
        }
    }

    private void viewFollowRequests(User loggedInUser) {
        System.out.println("Your Follow Requests (Sent & Received):");
        userService.getFollowRequests(loggedInUser).forEach(request -> {
            String requesterUsername = request.getRequester().getUsername();
            String recipientUsername = request.getRecipient().getUsername();
            if (request.getRequester().equals(loggedInUser)) {
                System.out.println("Follow request sent to: @" + recipientUsername + " | Status: " + request.getStatus());
            } else if (request.getRecipient().equals(loggedInUser)) {
                System.out.println("Follow request received from: @" + requesterUsername + " | Status: " + request.getStatus());
            }
            if (request.getStatus() == FollowRequestStatus.PENDING && request.getRecipient().equals(loggedInUser)) {
                acceptOrRejectFollowRequest(loggedInUser, request);
            }
        });
    }

    private void acceptOrRejectFollowRequest(User loggedInUser, FollowRequest request) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Accept (y/n)? ");
        String response = scanner.nextLine();
        if ("y".equalsIgnoreCase(response)) {
            userService.acceptFollowRequest(loggedInUser, request);
            System.out.println("Follow request accepted.");
        } else {
            userService.rejectFollowRequest(loggedInUser, request);
            System.out.println("Follow request rejected.");
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
        if (loggedInUser.getRole() == Role.SUPPORT) {
            return;
        }
        displayUserSetting(loggedInUser, selectedUser);
    }

    private void displayUserSetting(User loggedInUser, User selectedUser) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            CliFormatter.printTypingEffect(CliFormatter.boldYellow("Loading user options..."));
            System.out.println(CliFormatter.green("Options for: " + CliFormatter.boldYellow(selectedUser.getUsername())));
            System.out.println(CliFormatter.bold("1. Send Follow Request"));
            System.out.println(CliFormatter.boldRed("2. Remove Follower"));
            System.out.println(CliFormatter.boldBlue("3. Unfollow User"));
            System.out.println(CliFormatter.cyan("4. Go Back"));

            System.out.print(CliFormatter.boldYellow("Choose an option: "));
            int choice = scanner.nextInt();
            scanner.nextLine();

            try{
                switch (choice) {
                    case 1:
                        if (selectedUser != null) {
                            userService.sendFollowRequest(loggedInUser, selectedUser);
                        } else {
                            System.out.println(CliFormatter.boldRed("User not found."));
                        }
                        break;

                    case 2:
                        if (selectedUser != null) {
                            userService.removeFollower(loggedInUser, selectedUser);
                        } else {
                            System.out.println(CliFormatter.boldRed("Follower not found."));
                        }
                        break;
                    case 3:
                        if (selectedUser != null) {
                            userService.unfollowUser(loggedInUser, selectedUser);
                        } else {
                            System.out.println(CliFormatter.boldRed("User not found."));
                        }
                        break;

                    case 4:
                        return;

                    default:
                        System.out.println(CliFormatter.boldRed("Invalid option. Please try again."));
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void createPost(User loggedInUser) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter post content: ");
        String content = scanner.nextLine();

        postService.createPost(content, loggedInUser);
        System.out.println("Post created successfully!");
    }

    private void viewPosts(User loggedInUser) {
        postService.viewPostsByUser(loggedInUser);
    }

}
