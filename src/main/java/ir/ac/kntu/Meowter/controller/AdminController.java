package ir.ac.kntu.Meowter.controller;

import ir.ac.kntu.Meowter.service.ReportService;
import ir.ac.kntu.Meowter.model.User;

import java.util.Scanner;

public class AdminController {

    private ReportService reportService;

    public AdminController() {
        this.reportService = new ReportService();
    }

    public void displayAdminMenu(User loggedInUser) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Admin Menu");
        System.out.println("1. View User Reports");
        System.out.println("2. View Post Reports");
        System.out.println("3. View System Reports");
        System.out.println("4. Log out");

        System.out.print("Choose an option: ");
        int choice = scanner.nextInt();

        switch (choice) {
            case 1:
                reportService.generateUserReports();
                break;
            case 2:
                reportService.generatePostReports();
                break;
            case 3:
                reportService.generateSystemReports();
                break;
            case 4:
                System.out.println("Logging out...");
                break;
            default:
                System.out.println("Invalid option! Try again.");
                displayAdminMenu(loggedInUser);
                break;
        }
    }
}
