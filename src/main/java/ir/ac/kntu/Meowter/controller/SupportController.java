package ir.ac.kntu.Meowter.controller;

import ir.ac.kntu.Meowter.model.User;

import java.util.Scanner;

public class SupportController {

    public void displaySupportUserMenu(User loggedInUser) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Support User Menu");
        System.out.println("1. View User Information");
        System.out.println("2. Log out");

        System.out.print("Choose an option: ");
        int choice = scanner.nextInt();

        switch (choice) {
            case 1:
                viewUserInformation(loggedInUser);
                break;
            case 2:
                System.out.println("Logging out...");
                break;
            default:
                System.out.println("Invalid option! Try again.");
                displaySupportUserMenu(loggedInUser);
                break;
        }
    }

    private void viewUserInformation(User loggedInUser) {
        System.out.println("User Information:");
        System.out.println("Username: " + loggedInUser.getUsername());
        System.out.println("Email: " + loggedInUser.getEmail());
    }
}
