package ir.ac.kntu.Meowter.app;

import ir.ac.kntu.Meowter.controller.UserController;
import ir.ac.kntu.Meowter.model.Department;
import ir.ac.kntu.Meowter.model.Role;
import ir.ac.kntu.Meowter.model.User;
import ir.ac.kntu.Meowter.service.NotificationService;
import ir.ac.kntu.Meowter.service.PrometheusExporter;
import ir.ac.kntu.Meowter.service.UserService;
import ir.ac.kntu.Meowter.service.SessionManager;
import ir.ac.kntu.Meowter.exceptions.InvalidCommandException;
import ir.ac.kntu.Meowter.util.*;
import ir.ac.kntu.Meowter.exceptions.CaptchaVerificationException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        KafkaUtil kafkaUtil = new KafkaUtil(
                "localhost:9092",
                "localhost:9092",
                "notification-group",
                "notifications"
        );

        Neo4jUtil neo4jUtil = new Neo4jUtil("bolt://localhost:7687", "neo4j", "password");
        NotificationService notificationService = new NotificationService(kafkaUtil, "notifications");
        PrometheusExporter exporter = new PrometheusExporter();

        try {
            exporter.startExporter();
            System.out.println("Prometheus HTTP server is running on http://localhost:9090/metrics");
        } catch (Exception e) {
            System.err.println("Failed to start Prometheus HTTP server: " + e.getMessage());
            e.printStackTrace();
        }

        CliFormatter.printMeow();
        Scanner scanner = new Scanner(System.in);
        UserService userService = new UserService();
        UserController userController = new UserController();
        User loggedInUser = SessionManager.loadSession();
        Role role = null;
        if (loggedInUser != null) {
            System.out.println(CliFormatter.blueUnderlined("❇\uFE0F Welcome back, @" + loggedInUser.getUsername() + "! ❇\uFE0F"));
            role = loggedInUser.getRole();
            if (role.equals(Role.SUPPORT)) {
                System.out.print(CliFormatter.boldYellow("\nYour departments: \n"));
                for (Department department : loggedInUser.getDepartments()){
                    System.out.print(department.toString());
                }
            }
        }
        while (true) {
            if (loggedInUser == null) {
                CliFormatter.printTypingEffect(CliFormatter.boldGreen("\uD83D\uDC3E  Welcome to Meowter \uD83D\uDC3E"));
                System.out.println(CliFormatter.blue("1. 👤 User"));
                System.out.println(CliFormatter.green("2. ⚙️ Admin"));
                System.out.println(CliFormatter.yellow("3. 🛠️ Support User"));
                System.out.println(CliFormatter.boldRed("4. Exit"));
                System.out.print(CliFormatter.bold("Choose your role (1, 2, 3 or 4 to exit): "));
                int roleChoice = scanner.nextInt();
                scanner.nextLine();
                if (roleChoice == 4) {
                    System.out.println(CliFormatter.boldRed("Goodbye!"));
                    break;
                }
                if (roleChoice < 1 || roleChoice > 3) {
                    try {
                        throw new InvalidCommandException("Please enter a valid option.");
                    } catch (InvalidCommandException e) {
                        System.out.println(CliFormatter.red(e.getMessage()));
                        continue;
                    }
                }
                role = Role.USER;
                if (roleChoice == 2) {
                    role = Role.ADMIN;
                } else if (roleChoice == 3) {
                    role = Role.SUPPORT;
                }
                loggedInUser = null;
                try {
                    ArithmeticCaptchaUtil.askCaptchaOrThrow();
                } catch (CaptchaVerificationException e) {
                    System.out.println(CliFormatter.boldRed(e.getMessage()));
                    break;
                }
                if (role == Role.ADMIN || role == Role.SUPPORT) {
                    System.out.println(CliFormatter.boldBlue("1. 🔒 Login"));
                    System.out.print(CliFormatter.boldGreen("Choose an option (Type anything else for turning back to main menu): "));
                    int choice = scanner.nextInt();
                    scanner.nextLine();

                    if (choice == 1) {
                        System.out.print(CliFormatter.boldYellow("Enter your email or username: "));
                        String username = scanner.nextLine();
                        System.out.print(CliFormatter.boldPurple("Enter your password: "));
                        String password = scanner.nextLine();

                        loggedInUser = attemptLogin(userService, username, password, role);
                        if (loggedInUser == null) {
                            continue;
                        }
                    } else {
                        CliFormatter.printTypingEffect("Turning back to main menu...");
                        continue;
                    }
                } else {
                    System.out.println(CliFormatter.blue("1. 🔒 Login"));
                    System.out.println(CliFormatter.cyan("2. 📝 Register"));
                    System.out.print(CliFormatter.boldGreen("Choose an option (Type anything else for turning back to main menu): "));
                    int choice = scanner.nextInt();
                    scanner.nextLine();
                    if (choice == 1) {
                        System.out.print(CliFormatter.boldYellow("Enter your email or username: "));
                        String username = scanner.nextLine();
                        System.out.print(CliFormatter.boldPurple("Enter your password: "));
                        String password = scanner.nextLine();
                        loggedInUser = attemptLogin(userService, username, password, Role.USER);
                        if (loggedInUser == null) {
                            continue;
                        }
                    } else if (choice == 2) {
                        System.out.println(CliFormatter.boldBlue("⚠️ Registration Requirements:"));
                        System.out.println(CliFormatter.bold("Please ensure the following before registering:"));
                        System.out.println(CliFormatter.boldYellow("1. Username: ") +
                                CliFormatter.bold("Username should be unique."));
                        System.out.println(CliFormatter.boldYellow("2. Email: ") +
                                CliFormatter.bold("Must be a valid and unique email address."));
                        System.out.println(CliFormatter.boldYellow("3. Password: ") +
                                CliFormatter.bold("Must be at least 8 characters long and include:"));
                        System.out.println(CliFormatter.bold("   - ") + "One uppercase letter.");
                        System.out.println(CliFormatter.bold("   - ") + "One lowercase letter.");
                        System.out.println(CliFormatter.bold("   - ") + "One number.");
                        System.out.println(CliFormatter.bold("   - ") + "One special character (e.g., !@#$%).");
                        CliFormatter.printTypingEffect(CliFormatter.boldGreen("✔️ Ready to register? Follow the prompts below!"));



                        System.out.println("\n------------");


                        System.out.print(CliFormatter.boldPurple("Enter a username: "));
                        String username = scanner.nextLine();
                        System.out.print(CliFormatter.magenta("Enter your email: "));
                        String email = scanner.nextLine();
                        System.out.print(CliFormatter.red("Enter your password: "));
                        String password = scanner.nextLine();

                        try {
                            loggedInUser = userService.register(username, email, password);
                        } catch (Exception e) {
                            System.out.println(CliFormatter.boldRed(e.getMessage()));
                        }

                        if (loggedInUser != null) {
                            System.out.println(CliFormatter.boldGreen("Registration successful! You are now logged in."));
                            SessionManager.saveSession(loggedInUser);
                        }
                    } else {
                        CliFormatter.printTypingEffect("Turning back to main menu...");
                        continue;
                    }
                }
            } else {
                if(!loggedInUser.isActive()){
                    System.out.println(CliFormatter.boldRed("Your account is Inactive!"));
                    System.out.println(CliFormatter.yellow("For getting more information contact with admin."));
                    CliFormatter.printTypingEffect(CliFormatter.boldRed("Logging out..."));
                    SessionManager.clearSession();
                    break;
                }
                final User user = loggedInUser;
                new Thread(() -> notificationService.startListening(user)).start();

                if (role == Role.ADMIN) {
                    AdminMenuHandler adminMenuHandler = new AdminMenuHandler();
                    adminMenuHandler.displayAdminMenu(loggedInUser);
                } else if (role == Role.SUPPORT) {
                    SupportMenuHandler supportMenuHandler = new SupportMenuHandler();
                    supportMenuHandler.displaySupportMenu(loggedInUser);
                } else {
                    MenuHandler menuHandler = new MenuHandler();
                    menuHandler.displayMainMenu(loggedInUser, role);
                }

                CliFormatter.printTypingEffect("Do you want to log out? (y/n): ");
                String logoutChoice = scanner.nextLine();
                if ("y".equalsIgnoreCase(logoutChoice)) {
                    CliFormatter.printTypingEffect(CliFormatter.boldRed("Logging out..."));
                    SessionManager.clearSession();
                    loggedInUser = null;
                }
            }
        }
    }

    private static User attemptLogin(UserService userService, String username, String password, Role expectedRole) {
        User loggedInUser = null;
        if (username.contains("@")) {
            loggedInUser = userService.loginWithEmail(username, password);
        } else {
            loggedInUser = userService.loginWithUsername(username, password);
        }

        if (loggedInUser != null) {
            if (loggedInUser.getRole() != expectedRole) {
                System.out.println(CliFormatter.red("Access denied. You do not have the proper role to log in as " + expectedRole + "."));
                loggedInUser = null;
            } else {
                System.out.println(CliFormatter.boldGreen("Login successful!"));
                SessionManager.saveSession(loggedInUser);
            }
        } else {
            System.out.println(CliFormatter.boldRed("Invalid email/username or password."));
        }
        return loggedInUser;
    }
}
