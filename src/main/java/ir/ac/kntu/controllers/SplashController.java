package ir.ac.kntu.controllers;

import io.github.palexdev.materialfx.controls.MFXProgressBar;
import io.github.palexdev.materialfx.theming.JavaFXThemes;
import io.github.palexdev.materialfx.theming.MaterialFXStylesheets;
import io.github.palexdev.materialfx.theming.UserAgentBuilder;
import ir.ac.kntu.Meowter.model.User;
import ir.ac.kntu.Meowter.service.SessionManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import io.github.palexdev.materialfx.effects.Interpolators;
import io.github.palexdev.materialfx.utils.AnimationUtils.KeyFrames;
import io.github.palexdev.materialfx.utils.AnimationUtils.PauseBuilder;
import io.github.palexdev.materialfx.utils.AnimationUtils.TimelineBuilder;
import javafx.animation.Animation;
import javafx.scene.control.ProgressIndicator;

public class SplashController {
    @FXML
    private MFXProgressBar progressBar;

    @FXML
    public void initialize() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, event -> progressBar.setProgress(0)),
                new KeyFrame(Duration.seconds(5), event -> progressBar.setProgress(1))
        );
        timeline.play();
        createAndPlayAnimation(progressBar);

        timeline.setOnFinished(event -> {
            try {
                User loggedInUser = SessionManager.loadSession();

                if (loggedInUser != null) {
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


                    Stage stage = (Stage) progressBar.getScene().getWindow();
                    stage.setScene(new Scene(root, 800, 600));
                } else {
                    Parent root = FXMLLoader.load(getClass().getResource("/ir/ac/kntu/views/intro1.fxml"));
                    Stage stage = (Stage) progressBar.getScene().getWindow();

                    stage.setScene(new Scene(root, 800, 600));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    private void createAndPlayAnimation(ProgressIndicator indicator) {
        Animation a1 = TimelineBuilder.build()
                .add(
                        KeyFrames.of(2000, indicator.progressProperty(), 0.3, Interpolators.INTERPOLATOR_V1),
                        KeyFrames.of(4000, indicator.progressProperty(), 0.6, Interpolators.INTERPOLATOR_V1),
                        KeyFrames.of(6000, indicator.progressProperty(), 1.0, Interpolators.INTERPOLATOR_V1)
                )
                .getAnimation();

        Animation a2 = TimelineBuilder.build()
                .add(
                        KeyFrames.of(1000, indicator.progressProperty(), 0, Interpolators.INTERPOLATOR_V2)
                )
                .getAnimation();

        a1.setOnFinished(end -> PauseBuilder.build()
                .setDuration(Duration.seconds(1))
                .setOnFinished(event -> a2.playFromStart())
                .getAnimation()
                .play()
        );
        a2.setOnFinished(end -> PauseBuilder.build()
                .setDuration(Duration.seconds(1))
                .setOnFinished(event -> a1.playFromStart())
                .getAnimation()
                .play()
        );

        a1.play();
    }

    private void loadFonts() {
        Font.loadFont(getClass().getResourceAsStream("/fonts/Roboto/Roboto-Bold.ttf"), 24);
        Font.loadFont(getClass().getResourceAsStream("/fonts/OpenSans/OpenSans-Regular.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/fonts/Comfortaa/Comfortaa-Bold.ttf"), 18);
    }
}