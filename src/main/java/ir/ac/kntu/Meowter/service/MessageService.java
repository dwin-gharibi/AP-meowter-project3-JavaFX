package ir.ac.kntu.Meowter.service;

import ir.ac.kntu.Meowter.model.Comment;
import ir.ac.kntu.Meowter.model.Message;
import ir.ac.kntu.Meowter.model.User;
import ir.ac.kntu.Meowter.repository.MessageRepository;
import ir.ac.kntu.Meowter.util.CliFormatter;
import ir.ac.kntu.Meowter.util.KafkaUtil;
import ir.ac.kntu.Meowter.util.RabbitMQUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MessageService {

    private final MessageRepository messageRepository;
    private final NotificationService notificationService;


    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
        RabbitMQUtil.initializeQueue();
        KafkaUtil kafkaUtil = new KafkaUtil(
                "localhost:9092",
                "localhost:9092",
                "notification-group",
                "notifications"
        );
        this.notificationService = new NotificationService(kafkaUtil, "notifications");
    }

    public Message sendMessage(User sender, User recipient, String content) {
        if (sender == null || recipient == null) {
            throw new IllegalArgumentException("Sender and recipient must not be null.");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content must not be empty.");
        }

        Message message = new Message(sender, recipient, content);
        messageRepository.save(message);

        String messagePayload = String.format(
                "{\"senderId\":%d,\"recipientId\":%d,\"content\":\"%s\",\"timestamp\":\"%s\"}",
                sender.getId(), recipient.getId(), content, message.getTimestamp());
        RabbitMQUtil.sendMessage(messagePayload);

        notificationService.sendNotification(sender, recipient, "MESSAGE", "New message from " + CliFormatter.boldBlue("@" + sender.getUsername()) + " for you " + recipient.getUsername());
        return message;
    }

    public List<Message> getMessagesBySender(User sender) {
        if (sender == null) {
            throw new IllegalArgumentException("Sender must not be null.");
        }
        return messageRepository.findBySender(sender);
    }

    public List<Message> getMessagesByRecipient(User recipient) {
        if (recipient == null) {
            throw new IllegalArgumentException("Recipient must not be null.");
        }
        return messageRepository.findByRecipient(recipient);
    }

    public List<Message> getConversation(User user1, User user2) {
        if (user1 == null || user2 == null) {
            throw new IllegalArgumentException("Both users must not be null.");
        }
        return messageRepository.findConversation(user1, user2);
    }

    public void markMessageAsRead(Long messageId) {
        if (messageId == null) {
            throw new IllegalArgumentException("Message ID must not be null.");
        }
        messageRepository.markAsRead(messageId);
    }

    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    public Set<User> getChatUsers(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null.");
        }

        List<User> senders = messageRepository.findSendersByRecipient(user);

        List<User> recipients = messageRepository.findRecipientsBySender(user);

        Set<User> chatUsers = new HashSet<>();
        chatUsers.addAll(senders);
        chatUsers.addAll(recipients);

        return chatUsers;
    }
}
