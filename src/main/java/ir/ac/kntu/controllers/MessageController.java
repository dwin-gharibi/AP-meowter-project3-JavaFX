package ir.ac.kntu.controllers;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import ir.ac.kntu.Meowter.model.User;
import ir.ac.kntu.Meowter.model.Message;
import ir.ac.kntu.Meowter.repository.MessageRepository;
import ir.ac.kntu.Meowter.service.MessageService;
import ir.ac.kntu.Meowter.service.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class MessageController {

    @FXML
    private VBox chatListVBox, chatMessagesVBox;
    @FXML
    private MFXTextField searchField, messageInputField;
    @FXML
    private MFXButton sendButton, newChatButton, closeChatButton;
    @FXML
    private Label chatUserLabel, chatStatusLabel;
    @FXML
    private ImageView chatUserAvatar;
    @FXML
    private MFXScrollPane chatMessagesScrollPane;

    private MessageService messageService;
    private User currentUser;
    private User selectedChat;

    @FXML
    public void initialize() {
        MessageRepository messageRepository = new MessageRepository();
        messageService = new MessageService(messageRepository);

        currentUser = SessionManager.loadSession();
        if (currentUser == null) {
            return;
        }

        loadChatList();
        setupListeners();
    }

    private void loadChatList() {
        chatListVBox.getChildren().clear();
        Set<User> chats = messageService.getChatUsers(currentUser);

        for (User chat : chats) {
            HBox chatItem = createChatItem(chat);
            chatListVBox.getChildren().add(chatItem);
        }
    }

    private HBox createChatItem(User chat) {
        HBox chatItem = new HBox();
        chatItem.getStyleClass().add("chat-item");

        ImageView avatar = new ImageView(new Image("/images/avatar.png"));
        avatar.setFitWidth(40);
        avatar.setFitHeight(40);
        avatar.getStyleClass().add("chat-avatar");

        VBox details = new VBox();
        details.getStyleClass().add("chat-details");

        Label username = new Label(chat.getUsername());
        username.getStyleClass().add("chat-username");

        List<Message> messages = messageService.getConversation(currentUser, chat);
        String previewText = messages.isEmpty() ? "No messages yet" : messages.get(messages.size() - 1).getContent();

        Label preview = new Label(previewText);
        preview.getStyleClass().add("chat-preview");

        details.getChildren().addAll(username, preview);
        chatItem.getChildren().addAll(avatar, details);

        chatItem.setOnMouseClicked(event -> loadChat(chat));

        return chatItem;
    }

    private void loadChat(User chat) {
        this.selectedChat = chat;

        List<Message> messages = messageService.getConversation(currentUser, chat);

        chatUserLabel.setText(chat.getFullname());
        chatUserAvatar.setImage(new Image("/images/avatar.png"));
        chatStatusLabel.setText("Online");

        chatMessagesVBox.getChildren().clear();
        for (Message message : messages) {
            chatMessagesVBox.getChildren().add(createMessageItem(message));
        }

        chatMessagesScrollPane.setVvalue(1.0);
    }

    private HBox createMessageItem(Message message) {
        HBox messageBox = new HBox();
        boolean isSentByCurrentUser = message.getSender().equals(currentUser);

        messageBox.getStyleClass().add(isSentByCurrentUser ? "message sent" : "message received");

        Label messageLabel = new Label(message.getContent());
        messageLabel.getStyleClass().add("message-content");

        messageBox.getChildren().add(messageLabel);
        return messageBox;
    }

    @FXML
    private void sendMessage() {
        if (selectedChat == null || messageInputField.getText().trim().isEmpty()) {
            return;
        }

        String content = messageInputField.getText().trim();
        Message newMessage = messageService.sendMessage(currentUser, selectedChat, content);

        chatMessagesVBox.getChildren().add(createMessageItem(newMessage));

        messageInputField.clear();

        chatMessagesScrollPane.setVvalue(1.0);
    }

    @FXML
    private void startNewChat() {
        Set<User> followers = currentUser.getFollowing();

        for (User follower : followers) {
            System.out.println(follower.getUsername());
        }

        Optional<User> selectedUser = showUserSelectionDialog(followers);

        selectedUser.ifPresent(user -> {
            messageService.sendMessage(currentUser, user, "Hello " + user.getUsername());
            loadChatList();
            loadChat(user);
        });
    }

    private Optional<User> showUserSelectionDialog(Set<User> users) {
        TextInputDialog usernameDialog = new TextInputDialog();
        usernameDialog.setTitle("Select User");
        usernameDialog.setHeaderText("Enter the username of the user you want to chat with:");
        usernameDialog.setContentText("Username:");

        Optional<String> result = usernameDialog.showAndWait();

        if (result.isPresent() && !result.get().isEmpty()) {
            String username = result.get();
            User selectedUser = findUserByUsername(username, users);
            if (selectedUser != null) {
                return Optional.of(selectedUser);
            } else {
                showError("User not found", "No user with the username '" + username + "' was found.");
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private User findUserByUsername(String username, Set<User> users) {
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void setupListeners() {
        sendButton.setOnAction(event -> sendMessage());
        newChatButton.setOnAction(event -> startNewChat());
        closeChatButton.setOnAction(event -> {
            selectedChat = null;
            chatMessagesVBox.getChildren().clear();
            chatUserLabel.setText("");
            chatStatusLabel.setText("");
        });
    }
}