package ir.ac.kntu.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class IntroController {
    public static int intro_counter = 0;

    @FXML
    private void handleNext(ActionEvent event) {
        try {
            Parent root;
            intro_counter++;

            if (intro_counter >= 4) {
                root = FXMLLoader.load(getClass().getResource("/ir/ac/kntu/views/login.fxml"));
            } else {
                root = FXMLLoader.load(getClass().getResource("/ir/ac/kntu/views/intro" + intro_counter + ".fxml"));
            }
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}