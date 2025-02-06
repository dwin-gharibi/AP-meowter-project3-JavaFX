package ir.ac.kntu.controllers;

import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.controls.MFXToggleButton;
import io.github.palexdev.materialfx.utils.ScrollUtils;
import io.github.palexdev.materialfx.utils.ToggleButtonsUtil;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import ir.ac.kntu.Meowter.model.User;
import ir.ac.kntu.Meowter.repository.UserRepository;
import ir.ac.kntu.Meowter.service.SessionManager;
import ir.ac.kntu.Meowter.service.UserService;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController {

    @FXML
    private MFXPasswordField newPasswordField;

    @FXML
    private MFXPasswordField confirmPasswordField;

    @FXML
    private MFXTextField newUsernameField;

    @FXML
    private MFXToggleButton profileVisibilityToggle;

    private UserService userService;
    private User loggedInUser;

    public SettingsController() {
        userService = new UserService();
    }

    public void initialize() {
        this.loggedInUser = SessionManager.loadSession();
    }

    @FXML
    public void onSaveSettings() {
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String newUsername = newUsernameField.getText();
        boolean isProfilePublic = profileVisibilityToggle.isSelected();

        boolean isValid = true;

        UserRepository userRepository = new UserRepository();
        UserService userService1 = new UserService();

        if (!newPassword.isEmpty() && !newPassword.equals(confirmPassword)) {
            showError("Password Error", "Passwords do not match.");
            isValid = false;
        }

        if (isValid && !newPassword.isEmpty()) {
            userService.updatePassword(loggedInUser, newPassword);
        }

        if (isValid && !newUsername.isEmpty()) {
            userService.updateUsername(loggedInUser, newUsername);
        }

        if (isValid) {
            userService.updatePrivacySetting(loggedInUser, isProfilePublic);
        }

        if (isValid) {
            showSuccess("Settings Updated", "Your settings have been updated successfully.");
            refreshView();
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void refreshView() {
        Platform.runLater(() -> {
            newPasswordField.clear();
            confirmPasswordField.clear();

            newUsernameField.clear();
            profileVisibilityToggle.setSelected(loggedInUser.getIsPrivate());
        });
    }
}



