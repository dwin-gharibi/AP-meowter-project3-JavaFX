package ir.ac.kntu.controllers;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import ir.ac.kntu.Meowter.model.Role;
import ir.ac.kntu.Meowter.service.SessionManager;
import javafx.fxml.FXML;
import fr.brouillard.oss.cssfx.CSSFX;
import io.github.palexdev.materialfx.theming.JavaFXThemes;
import io.github.palexdev.materialfx.theming.MaterialFXStylesheets;
import io.github.palexdev.materialfx.theming.UserAgentBuilder;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.concurrent.Task;
import javafx.concurrent.Service;


import ir.ac.kntu.Meowter.model.User;
import ir.ac.kntu.Meowter.service.UserService;

import java.util.Objects;

public class LoginController {
    @FXML
    private MFXTextField usernameField;
    @FXML
    private MFXPasswordField passwordField;
    @FXML
    private MFXButton loginButton;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        UserService userService = new UserService();
        Role role = null;

        User loggedInUser = userService.loginWithUsername(username, password);

        if (loggedInUser != null) {
            loginButton.setText("Loading...");
            loginButton.setDisable(true);

            Service<Void> loadingService = new Service<>() {
                @Override
                protected Task<Void> createTask() {
                    return new Task<>() {
                        @Override
                        protected Void call() throws Exception {
                            SessionManager.saveSession(loggedInUser);
                            Thread.sleep(2000);
                            return null;
                        }
                    };
                }
            };

            loadingService.setOnSucceeded(event -> {
                try {
                    UserAgentBuilder.builder()
                            .themes(JavaFXThemes.MODENA)
                            .themes(MaterialFXStylesheets.forAssemble(true))
                            .setDeploy(true)
                            .setResolveAssets(true)
                            .build()
                            .setGlobal();

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/ir/ac/kntu/views/dashboard.fxml"));
                    Parent root = loader.load();

                    DashboardController controller = loader.getController();
                    System.out.println(loggedInUser.getUsername());
                    controller.setUser(loggedInUser);
                    Stage stage = (Stage) usernameField.getScene().getWindow();
                    stage.setScene(new Scene(root, 800, 600));
                } catch (Exception e) {
                    e.printStackTrace();
                    loginButton.setText("Login");
                    loginButton.setDisable(false);
                    showError("Failed to load the dashboard. Please try again.");
                }
            });

            loadingService.setOnFailed(event -> {
                loginButton.setText("Login");
                loginButton.setDisable(false);
                showError("An error occurred during login. Please try again.");
            });

            loadingService.start();
        } else {
            showError("Invalid username or password");
        }
    }

    @FXML
    private void handleSignUp() {
        navigateToSignUp();
    }

    private void navigateToSignUp() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ir/ac/kntu/views/signup.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}