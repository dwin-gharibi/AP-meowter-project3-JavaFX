package ir.ac.kntu.Meowter.controller;

import ir.ac.kntu.Meowter.model.Message;
import ir.ac.kntu.Meowter.model.User;
import ir.ac.kntu.Meowter.repository.MessageRepository;
import ir.ac.kntu.Meowter.repository.UserRepository;
import ir.ac.kntu.Meowter.service.MessageService;
import ir.ac.kntu.Meowter.service.UserService;
import ir.ac.kntu.Meowter.util.CliFormatter;
import ir.ac.kntu.Meowter.util.RabbitMQListener;

import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class MessageController {

    private final MessageService messageService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final Scanner scanner;
    private final MessageRepository messageRepository;

    public MessageController() {
        this.userService = new UserService();
        this.userRepository = new UserRepository();
        this.scanner = new Scanner(System.in);
        this.messageRepository = new MessageRepository();
        this.messageService = new MessageService(this.messageRepository);
        new Thread(RabbitMQListener::startListening).start();
    }

    public void start(User loggedInUser) {
        while (true) {
            System.out.println("\nMessage Menu:");
            System.out.println(CliFormatter.boldBlue("1. View existing chats"));
            System.out.println(CliFormatter.boldGreen("2. Select a chat and send a message"));
            System.out.println(CliFormatter.boldPurple("3. Start a new chat"));
            System.out.println(CliFormatter.boldRed("4. Exit Messages"));
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> listChats(loggedInUser);
                case 2 -> selectChatAndSendMessage(loggedInUser);
                case 3 -> startNewChat(loggedInUser);
                case 4 -> {
                    System.out.println(CliFormatter.boldRed("Exiting messages..."));
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void listChats(User loggedInUser) {
        Set<User> chatUsers = messageService.getChatUsers(loggedInUser);

        if (chatUsers.isEmpty()) {
            System.out.println(CliFormatter.boldRed("No active chats found."));
            return;
        }

        System.out.println(CliFormatter.boldYellow("Your active chats:"));
        int index = 1;
        for (User user : chatUsers) {
            List<Message> messages = messageService.getConversation(loggedInUser, user);
            Message latestMessage = messages.isEmpty() ? null : messages.get(messages.size() - 1);

            if (latestMessage != null) {
                System.out.printf("%d. %s - Last Message: %s (Sent: %s)%n", index, user.getUsername(),
                        latestMessage.getContent(), latestMessage.getTimestamp());
            } else {
                System.out.printf("%d. %s - No messages yet.%n", index, user.getUsername());
            }
            index++;
        }
    }

    private void selectChatAndSendMessage(User loggedInUser) {
        Set<User> chatUsers = messageService.getChatUsers(loggedInUser);

        if (chatUsers.isEmpty()) {
            System.out.println(CliFormatter.boldRed("No active chats found. Start a new chat first."));
            return;
        }

        System.out.println(CliFormatter.boldYellow("Select a chat:"));
        List<User> userList = chatUsers.stream().toList();
        for (int i = 0; i < userList.size(); i++) {
            System.out.println((i + 1) + ". " + userList.get(i).getUsername());
        }

        System.out.print("Enter number: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice < 1 || choice > userList.size()) {
            System.out.println(CliFormatter.boldRed("Invalid choice."));
            return;
        }

        User selectedUser = userList.get(choice - 1);
        viewConversation(loggedInUser, selectedUser);

        System.out.println("Enter your message (or type 'exit' to go back): ");
        String content = scanner.nextLine();

        if (!content.equalsIgnoreCase("exit")) {
            messageService.sendMessage(loggedInUser, selectedUser, content);
            System.out.println(CliFormatter.boldGreen("Message sent successfully."));
        }
    }

    private void startNewChat(User loggedInUser) {
        System.out.print("Enter username to start a chat with: ");
        String username = scanner.nextLine();
        User recipient = userRepository.findByUsername(username);

        CliFormatter.loadingSpinner("Getting user");
        if (recipient == null) {
            System.out.println(CliFormatter.boldRed("User not found."));
            return;
        }

//        if (!(recipient.getFollowers().contains(loggedInUser) && !recipient.getFollowing().contains(loggedInUser))) {
//            System.out.println(CliFormatter.boldRed("User not found or not follows you, so you cant send message!"));
//            return;
//        }

        if (loggedInUser.equals(recipient)) {
            System.out.println(CliFormatter.boldRed("You cannot start a chat with yourself."));
            return;
        }

        System.out.print("Enter your message: ");
        String content = scanner.nextLine();

        messageService.sendMessage(loggedInUser, recipient, content);
        System.out.println(CliFormatter.boldGreen("Chat started and message sent successfully."));
    }

    public void viewConversation(User loggedInUser, User otherUser) {
        List<Message> messages = messageService.getConversation(loggedInUser, otherUser);

        for (Message message : messages) {
            if (message.getRecipient().getId() == loggedInUser.getId()) {
                messageService.markMessageAsRead(message.getId());
            }
        }

        if (messages.isEmpty()) {
            System.out.println(CliFormatter.boldRed("No messages found with " + otherUser.getUsername()));
            return;
        }

        System.out.println(CliFormatter.boldYellow("Conversation with " + otherUser.getUsername() + ":"));
        for (Message message : messages) {
            System.out.printf("[%s] %s: %s (Read: %s)%n",
                    message.getTimestamp(),
                    CliFormatter.boldBlue("@" + message.getSender().getUsername()),
                    CliFormatter.boldPurple(message.getContent()),
                    CliFormatter.boldYellow(message.isRead() ? "Yes" : "No"));
        }
    }
}

