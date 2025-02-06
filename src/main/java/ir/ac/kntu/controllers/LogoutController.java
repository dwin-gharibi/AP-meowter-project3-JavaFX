package ir.ac.kntu.controllers;

import ir.ac.kntu.Meowter.service.SessionManager;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LogoutController {

    @FXML
    public void logout(ActionEvent event) {
        SessionManager.clearSession();

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(event2 -> {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/ir/ac/kntu/views/login.fxml"));
                stage.setScene(new Scene(root, 800, 600));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        pause.play();
    }

}
