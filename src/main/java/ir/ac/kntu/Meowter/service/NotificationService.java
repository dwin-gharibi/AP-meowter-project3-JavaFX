package ir.ac.kntu.Meowter.service;

import ir.ac.kntu.Meowter.model.Notification;
import ir.ac.kntu.Meowter.model.User;
import ir.ac.kntu.Meowter.repository.UserRepository;
import ir.ac.kntu.Meowter.util.CliFormatter;
import ir.ac.kntu.Meowter.util.KafkaUtil;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NotificationService {

    private final KafkaUtil kafkaUtil;
    private final String topic;
    private UserRepository userRepository;

    public NotificationService(KafkaUtil kafkaUtil, String topic) {
        this.kafkaUtil = kafkaUtil;
        this.topic = topic;
        this.userRepository = new UserRepository();
    }

    public void sendNotification(User notifier, User notifiable, String type, String content) {
        String message = String.format("%d,%d,%s,%s", notifier.getId(), notifiable.getId(), type, content);

        kafkaUtil.sendNotification(topic, message);
        Notification newNotification = new Notification(type, content, notifier, notifiable);
        userRepository.saveNotification(newNotification);
    }

    public List<Notification> getNotifications(User user) {
        return userRepository.getNotifications(user).stream().filter(notification -> notification.getActive() == true).collect(Collectors.toList());
    }

    public List<Notification> getAllNotifications() {
        return userRepository.getAllNotifications();
    }

    public Notification getNotificationById(int id) {
        return userRepository.getNotificationById(id);
    }

    public void startListening(User loggedinUser) {
        kafkaUtil.listenForNotifications(message -> {

            String[] parts = message.split(",");
            Long notifierId = Long.parseLong(parts[0]);
            Long notifieeId = Long.parseLong(parts[1]);
            String type = parts[2];
            String content = parts[3];

            if (loggedinUser.getId().equals(notifieeId)) {
                System.out.print(CliFormatter.cyan("NEW NOTIFICATION:"));
                System.out.println(message);
            }
        });
    }

    public void sendNotificationToAllUsers(User admin, String type, String content, List<User> allUsers) {
        for (User user : allUsers) {
            if (!user.getId().equals(admin.getId())) {
                sendNotification(admin, user, type, content);
            }
        }
    }

}

