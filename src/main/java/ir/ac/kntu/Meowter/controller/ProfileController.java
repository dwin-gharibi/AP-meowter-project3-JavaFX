package ir.ac.kntu.Meowter.controller;

import ir.ac.kntu.Meowter.model.User;
import ir.ac.kntu.Meowter.service.UserService;
import ir.ac.kntu.Meowter.util.DateConverter;

import java.time.LocalDateTime;
import java.util.Scanner;

public class ProfileController {

    private UserService userService;

    public ProfileController() {
        this.userService = new UserService();
    }

    public void displayProfileSection(User loggedInUser) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Profile Section");
            System.out.println("1. View Profile");
            System.out.println("2. Edit Profile");
            System.out.println("3. Back to Main Menu");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.println("Username: @" + loggedInUser.getUsername());
                    System.out.println("Email: " + loggedInUser.getEmail());
                    System.out.println("Followers: " + loggedInUser.getFollowers().size());
                    System.out.println("Following: " + loggedInUser.getFollowing().size());
                    System.out.println("Bio: " + (loggedInUser.getBio() != null ? loggedInUser.getBio() : "Not set"));
                    System.out.println("Date of Birth: " + (loggedInUser.getDateofbirth() != null ? loggedInUser.getDateofbirth() : "Not set"));
                    break;
                case 2:
                    System.out.print("Enter new username (leave blank to skip): ");
                    String newUsername = scanner.nextLine();
                    if (!newUsername.isBlank()) {
                        loggedInUser = userService.updateUsername(loggedInUser, newUsername);
                        System.out.println("Username updated successfully.");
                    }
                    System.out.print("Enter new bio (leave blank to skip): ");
                    String newBio = scanner.nextLine();
                    if (!newBio.isBlank()) {
                        loggedInUser = userService.updateBio(loggedInUser, newBio);
                        System.out.println("Bio updated successfully.");
                    }
                    System.out.print("Enter new date of birth (YYYY-MM-DD, leave blank to skip): ");
                    String newDob = scanner.nextLine();
                    LocalDateTime dateOfBirth = DateConverter.convertStringToDate(newDob);
                    if (!newDob.isBlank()) {
                        loggedInUser = userService.updateDateOfBirth(loggedInUser, dateOfBirth);
                        System.out.println("Date of Birth updated successfully.");
                    }
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid option. Try again.");
                    break;
            }
        }
    }
}
