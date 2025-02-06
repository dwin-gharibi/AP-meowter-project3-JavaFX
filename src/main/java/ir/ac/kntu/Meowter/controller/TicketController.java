package ir.ac.kntu.Meowter.controller;

import com.fasterxml.jackson.databind.jsontype.impl.ClassNameIdResolver;
import ir.ac.kntu.Meowter.exceptions.NotExistingUserException;
import ir.ac.kntu.Meowter.model.Role;
import ir.ac.kntu.Meowter.repository.TicketRepository;
import ir.ac.kntu.Meowter.repository.UserRepository;
import ir.ac.kntu.Meowter.service.TicketService;
import ir.ac.kntu.Meowter.model.Ticket;
import ir.ac.kntu.Meowter.model.TicketSubject;
import ir.ac.kntu.Meowter.model.User;
import ir.ac.kntu.Meowter.service.UserService;
import ir.ac.kntu.Meowter.util.CliFormatter;
import ir.ac.kntu.Meowter.util.PaginationUtil;


import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TicketController {

    private TicketService ticketService;
    private final TicketRepository ticketRepository;
    private UserService userService;
    private UserRepository userRepository;

    public TicketController() {
        this.ticketService = new TicketService();
        this.ticketRepository = new TicketRepository();
        this.userService = new UserService();
        this.userRepository = new UserRepository();
    }

    public void displayTicketSection(User loggedInUser) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            displayTicketMenu(loggedInUser);
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 0:
                    System.out.print(CliFormatter.boldRed("Enter Ticket ID to ban ticket user: "));
                    long ticketIdBan = scanner.nextLong();
                    scanner.nextLine();
                    Ticket ticketToBan = ticketRepository.findById(ticketIdBan);
                    if (ticketToBan != null) {
                        User user = userService.searchUserByUsername(ticketToBan.getUsername());
                        System.out.println(user.getUsername());
                        user.setActive(false);
                        userRepository.update(user);
                        System.out.println(CliFormatter.boldRed("User of this ticket get InActivated!"));
                    } else {
                        System.out.println(CliFormatter.boldRed("Ticket not found or invalid."));
                    }
                    break;
                case 1:
                    createTicketSection(loggedInUser);
                    break;
                case 2:
                    viewTicketSection(loggedInUser);
                    break;
                case 3:
                    responseTicketSection(loggedInUser);
                    break;
                case 4:
                    System.out.print("Enter Ticket ID to close: ");
                    long ticketIdToClose = scanner.nextLong();
                    Ticket ticketToClose = ticketRepository.findById(ticketIdToClose);
                    if (ticketToClose != null) {
                        ticketService.closeTicket(ticketToClose);
                        System.out.println("Ticket closed successfully.");
                    } else {
                        System.out.println("Ticket not found.");
                    }
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid option. Try again.");
                    break;
            }
        }
    }

    void displayTicketMenu(User loggedInUser) {
        CliFormatter.printTypingEffect(CliFormatter.boldGreen("Welcome to ticket section:"));


        if (loggedInUser.getRole() == Role.SUPPORT){
            System.out.println("0. Ban a Ticket User");
            System.out.println("1. Set Warning on Ticket");
        } else{
            System.out.println(CliFormatter.boldGreen("1. Create Ticket"));
        }

        if (loggedInUser.getRole() == Role.SUPPORT){
            System.out.println("2. View All Tickets");
        } else{
            System.out.println(CliFormatter.boldBlue("2. View My Tickets"));
        }

        if (loggedInUser.getRole() == Role.SUPPORT){
            System.out.println("3. Respond to Ticket");
            System.out.println("4. Close Ticket");

        }
        System.out.println(CliFormatter.boldRed("5. Back to User Menu"));
        System.out.print(CliFormatter.magenta("Choose an option: "));
    }

    void viewTicketSection(User loggedInUser) {
        System.out.println("Your Tickets:");
        CliFormatter.loadingSpinner("Waiting for tickets ...");
        List<String> ticket_details = new ArrayList<>();

        if (loggedInUser.getRole() == Role.SUPPORT) {
            ticketService.getAllTickets().stream()
                    .filter(ticket -> ticket.getDepartments().stream().anyMatch(loggedInUser.getDepartments()::contains))
                    .forEach(t -> {
                        String message = CliFormatter.boldYellow(t.getDescription());
                        String ticketUserSend = CliFormatter.boldBlue("@" + t.getUsername());
                        String response = t.getResponse() == null ? CliFormatter.red("No Response Available") : CliFormatter.boldGreen(t.getResponse());
                        String ticketIdOut = CliFormatter.blue("#" + String.valueOf(t.getId()));
                        String status = CliFormatter.yellow("@" + String.valueOf(t.getStatus()));
                        String warning = t.getIsWarned() ? CliFormatter.boldRed("Yes") : CliFormatter.green("No");
                        String warningMessage = t.getReportWarning() == null ? "" : t.getReportWarning();
                        String ticketUser = t.getReportUsername() == null ? " " : t.getReportUsername();
                        ticket_details.add("Ticket ID: " + ticketIdOut + " " + ticketUserSend + " | Status: " + status + "\nMessage: " + message + CliFormatter.boldRed(ticketUser) + "\nResponse: " + response + "\nWarning: " + warning + " | " + CliFormatter.boldYellow(warningMessage));
                    });
            PaginationUtil.paginate(ticket_details);
        } else {
            ticketService.getUserTickets(loggedInUser.getUsername()).forEach(t -> {
                String message = CliFormatter.boldYellow(t.getDescription());
                String response = t.getResponse() == null ? CliFormatter.red("No Response Available") : CliFormatter.boldGreen(t.getResponse());
                String ticketIdOut = CliFormatter.blue("#" + String.valueOf(t.getId()));
                String status = CliFormatter.yellow("@" + String.valueOf(t.getStatus()));
                String warning = t.getIsWarned() ? CliFormatter.boldRed("Yes") : CliFormatter.green("No");
                String warningMessage = t.getReportWarning() == null ? "" : t.getReportWarning();
                ticket_details.add("Ticket ID: " + ticketIdOut + " | Status: " + status + "\nMessage: " + message + "\nResponse: " + response + "\nWarning: " + warning + " | " + CliFormatter.boldYellow(warningMessage));
            });
            PaginationUtil.paginate(ticket_details);
        }
    }

    void createTicketSection(User loggedInUser) {
        Scanner scanner = new Scanner(System.in);
        System.out.print(CliFormatter.boldGreen("Enter ticket description: "));
        String description = scanner.nextLine();
        System.out.println(CliFormatter.boldBlue("Choose ticket subject:"));
        System.out.println("1. Report Issue");
        System.out.println("2. User Settings (+Username)");
        System.out.println("3. Profile Issue");
        System.out.println("4. Other");
        System.out.print(CliFormatter.magenta("Choose an option: "));

        int subjectChoice = scanner.nextInt();
        scanner.nextLine();
        String username = null;

        if (subjectChoice == 2) {
            System.out.println(CliFormatter.boldBlue("Please provide username for report:"));

            try {
                username = scanner.nextLine();
                User user = userService.searchUserByUsername(username);
                if (user == null) {
                    throw new NotExistingUserException(CliFormatter.boldRed("User not found"));
                }
            } catch (Exception e) {
                System.out.println(CliFormatter.boldRed("User not found"));
            }

        }
        TicketSubject subject = TicketSubject.values()[subjectChoice - 1];
        Ticket ticket = ticketService.createTicket(description, subject, loggedInUser.getUsername());
        if (subjectChoice == 2){
            ticket.setReportUsername(username);
        }
        CliFormatter.printTypingEffect(CliFormatter.boldGreen("Creating Ticket..."));
        System.out.println("Ticket created successfully. Ticket ID: " + CliFormatter.boldBlue("#" + ticket.getId()));
    }

    void responseTicketSection(User loggedInUser) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Ticket ID to respond: ");
        long ticketId = scanner.nextLong();
        scanner.nextLine();
        Ticket ticketToRespond = ticketRepository.findById(ticketId);
        if (ticketToRespond != null) {
            if (ticketToRespond.getUsername().equals(loggedInUser.getUsername()) || loggedInUser.getRole() == Role.SUPPORT) {
                System.out.print("Enter response: ");
                String response = scanner.nextLine();
                ticketService.respondToTicket(ticketToRespond, response);
                System.out.println("Response added successfully.");
            }
        } else {
            System.out.println("Ticket not found or invalid.");
        }
    }

}
