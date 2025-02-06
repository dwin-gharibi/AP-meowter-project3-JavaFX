package ir.ac.kntu;

import ir.ac.kntu.Meowter.service.PostService;
import ir.ac.kntu.Meowter.util.ContentCheckerUtil;
import javafx.application.Application;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import fr.brouillard.oss.cssfx.CSSFX;
import io.github.palexdev.materialfx.theming.JavaFXThemes;
import io.github.palexdev.materialfx.theming.MaterialFXStylesheets;
import io.github.palexdev.materialfx.theming.UserAgentBuilder;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {

        UserAgentBuilder.builder()
                .themes(JavaFXThemes.MODENA)
                .themes(MaterialFXStylesheets.forAssemble(true))
                .setDeploy(true)
                .setResolveAssets(true)
                .build()
                .setGlobal();


        Parent root = FXMLLoader.load(getClass().getResource("/ir/ac/kntu/views/splash.fxml"));
        Scene scene = new Scene(root, 800, 600);
        scene.setFill(Color.TRANSPARENT);
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setScene(scene);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        primaryStage.setTitle("Meowter");
        primaryStage.setScene(scene);
        primaryStage.show();

        ContentCheckerUtil contentChecker = new ContentCheckerUtil(new PostService());
        contentChecker.startChecking();
    }

    public static void main(String[] args) {
        launch(args);
    }
}