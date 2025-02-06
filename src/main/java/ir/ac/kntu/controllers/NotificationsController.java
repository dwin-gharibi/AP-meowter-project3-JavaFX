package ir.ac.kntu.controllers;

import io.github.palexdev.materialfx.controls.*;
import io.github.palexdev.materialfx.utils.ScrollUtils;
import io.github.palexdev.materialfx.utils.ToggleButtonsUtil;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import ir.ac.kntu.Meowter.model.Notification;
import ir.ac.kntu.Meowter.model.User;
import ir.ac.kntu.Meowter.service.NotificationService;
import ir.ac.kntu.Meowter.service.SessionManager;
import ir.ac.kntu.Meowter.util.KafkaUtil;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class NotificationsController {

	@FXML
	private MFXToggleButton generalButton;

	@FXML
	private MFXToggleButton likesButton;

	@FXML
	private MFXToggleButton commentsButton;

	@FXML
	private MFXToggleButton messagesButton;

	@FXML
	private VBox notificationsContainer;

	@FXML
	private Label footerLabel;

	private NotificationService notificationService;
	private List<Notification> allNotifications;

	public NotificationsController() {
		KafkaUtil kafkaUtil = new KafkaUtil(
				"localhost:9092",
				"localhost:9092",
				"notification-group",
				"notifications"
		);

		notificationService = new NotificationService(kafkaUtil, "notifications");
	}

	public void initialize() {
		User loggedInUser = SessionManager.loadSession();
		System.out.println(loggedInUser.getUsername());
		allNotifications = notificationService.getNotifications(loggedInUser);
		Collections.reverse(allNotifications);
		showNotifications(allNotifications);

		setupFilterButtons();
	}

	public static String removeAnsiCodes(String input) {
		String regex = "\u001B\\[[;\\d]*[A-Za-z]";
		return input.replaceAll(regex, "");
	}

	private void showNotifications(List<Notification> notifications) {
		Platform.runLater(() -> {
			notificationsContainer.getChildren().clear();

			for (Notification notification : notifications) {
				System.out.println(notification.getContent());
				HBox notificationBox = createNotificationBox(notification);
				notificationsContainer.getChildren().add(notificationBox);
			}
		});
	}

	private HBox createNotificationBox(Notification notification) {
		HBox notificationBox = new HBox();
		notificationBox.getStyleClass().add("notification");

		Label titleLabel = new Label(notification.getType());
		titleLabel.getStyleClass().add("notification-title");

		Label contentLabel = new Label(removeAnsiCodes(notification.getContent()));
		contentLabel.getStyleClass().add("notification-content");

		Label timeLabel = new Label(notification.getTimestamp().toString());
		timeLabel.getStyleClass().add("notification-time");

		notificationBox.getChildren().addAll(titleLabel, contentLabel, timeLabel);

		notificationBox.setOnMouseClicked(event -> showMaterialNotification(notification));

		return notificationBox;
	}

	private void showMaterialNotification(Notification notification) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Notification");
		alert.setHeaderText(notification.getType());
		alert.setContentText(removeAnsiCodes(notification.getContent()));
		alert.showAndWait();
	}

	private void setupFilterButtons() {
		generalButton.setOnAction(event -> filterAndShowNotifications("ADMIN"));
		likesButton.setOnAction(event -> filterAndShowNotifications("LIKE"));
		commentsButton.setOnAction(event -> filterAndShowNotifications("COMMENT"));
		messagesButton.setOnAction(event -> filterAndShowNotifications("MESSAGE"));
	}

	private void filterAndShowNotifications(String type) {
		List<Notification> filteredNotifications = allNotifications.stream()
				.filter(notification -> notification.getType().equalsIgnoreCase(type))
				.collect(Collectors.toList());
		showNotifications(filteredNotifications);
	}

	@FXML
	private void onFilterButtonClicked(javafx.event.ActionEvent event) {
		MFXToggleButton sourceButton = (MFXToggleButton) event.getSource();
		String filter = sourceButton.getText();

		List<Notification> filteredNotifications = allNotifications.stream()
				.filter(notification -> notification.getType().equalsIgnoreCase(filter) || filter.equalsIgnoreCase("All"))
				.collect(Collectors.toList());

		showNotifications(filteredNotifications);
	}

}

