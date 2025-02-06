package ir.ac.kntu.controllers;

import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import ir.ac.kntu.Meowter.model.User;
import ir.ac.kntu.Meowter.repository.UserRepository;
import ir.ac.kntu.Meowter.service.SessionManager;
import ir.ac.kntu.Meowter.service.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class SignUpController {
    @FXML
    private MFXTextField nameField;
    @FXML
    private MFXTextField emailField;
    @FXML
    private MFXTextField usernameField;
    @FXML
    private MFXPasswordField passwordField;
    @FXML
    private MFXPasswordField confirmPasswordField;


    @FXML
    private void handleSignUp() {
        String name = nameField.getText();
        String email = emailField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (password.equals(confirmPassword)) {
            UserService userService = new UserService();
            UserRepository userRepository = new UserRepository();
            User loggedInUser = userService.register(username, email, password);
            loggedInUser.setFullname(name);
            userRepository.save(loggedInUser);

            if (loggedInUser != null) {
                showAlert("Sign Up Successful", "You can now log in with your credentials.");
                navigateToLogin();
            } else {
                showError("Registration failed. Please check your inputs.");
            }
        } else {
            showError("Passwords do not match.");
        }
    }

    @FXML
    private void handleLogin() {
        navigateToLogin();
    }

    private void navigateToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ir/ac/kntu/views/login.fxml"));
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}