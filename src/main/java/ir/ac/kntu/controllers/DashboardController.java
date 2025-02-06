package ir.ac.kntu.controllers;

import io.github.palexdev.materialfx.controls.MFXIconWrapper;
import io.github.palexdev.materialfx.controls.MFXRectangleToggleNode;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import ir.ac.kntu.MFXDemoResourcesLoader;
import io.github.palexdev.materialfx.utils.ScrollUtils;
import io.github.palexdev.materialfx.utils.ToggleButtonsUtil;
import io.github.palexdev.materialfx.utils.others.loader.MFXLoader;
import io.github.palexdev.materialfx.utils.others.loader.MFXLoaderBean;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import ir.ac.kntu.Meowter.controller.ProfileController;
import ir.ac.kntu.Meowter.model.User;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static ir.ac.kntu.MFXDemoResourcesLoader.loadURL;

public class DashboardController implements Initializable {
    private final Stage stage;
    private double xOffset;
    private double yOffset;
    private final ToggleGroup toggleGroup;

    private User loggedInUser;

    @FXML
    private Label username_text;

    @FXML
    private HBox windowHeader;

    @FXML
    private MFXFontIcon closeIcon;

    @FXML
    private MFXFontIcon minimizeIcon;

    @FXML
    private MFXFontIcon alwaysOnTopIcon;

    @FXML
    private AnchorPane rootPane;

    @FXML
    private MFXScrollPane scrollPane;

    @FXML
    private VBox navBar;

    @FXML
    private StackPane contentPane;

    @FXML
    private StackPane logoContainer;

    public DashboardController(Stage stage) {
        this.stage = stage;
        this.toggleGroup = new ToggleGroup();
        ToggleButtonsUtil.addAlwaysOneSelectedSupport(toggleGroup);
    }

    public DashboardController() {
        this(new Stage());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        closeIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> Platform.exit());
        minimizeIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> ((Stage) rootPane.getScene().getWindow()).setIconified(true));
        alwaysOnTopIcon.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            boolean newVal = !stage.isAlwaysOnTop();
            alwaysOnTopIcon.pseudoClassStateChanged(PseudoClass.getPseudoClass("always-on-top"), newVal);
            stage.setAlwaysOnTop(newVal);
        });

        windowHeader.setOnMousePressed(event -> {
            xOffset = stage.getX() - event.getScreenX();
            yOffset = stage.getY() - event.getScreenY();
        });
        windowHeader.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() + xOffset);
            stage.setY(event.getScreenY() + yOffset);
        });

        initializeLoader();

        ScrollUtils.addSmoothScrolling(scrollPane);
    }

    private void initializeLoader() {
        MFXLoader loader = new MFXLoader();

        loader.addView(MFXLoaderBean.of("Dashboard", loadURL("/ir/ac/kntu/views/menu-dashboard.fxml")).setBeanToNodeMapper(() -> createToggle("fas-house", "Dashboard")).get());
        loader.addView(MFXLoaderBean.of("Tweets", loadURL("/ir/ac/kntu/views/tweet.fxml")).setBeanToNodeMapper(() -> createToggle("fas-gem", "Tweets")).get());
        loader.addView(MFXLoaderBean.of("Messages", loadURL("/ir/ac/kntu/views/messages.fxml")).setBeanToNodeMapper(() -> createToggle("fas-paper-plane", "Messages")).get());
        loader.addView(MFXLoaderBean.of("Profile", loadURL("/ir/ac/kntu/views/profile.fxml")).setBeanToNodeMapper(() -> createToggle("fas-circle-user", "Profile")).get());
        loader.addView(MFXLoaderBean.of("Notifications", loadURL("/ir/ac/kntu/views/notification.fxml")).setBeanToNodeMapper(() -> createToggle("fas-bell", "Notifications")).get());
        loader.addView(MFXLoaderBean.of("Settings", loadURL("/ir/ac/kntu/views/settings.fxml")).setBeanToNodeMapper(() -> createToggle("fas-gear", "Settings")).get());
        loader.addView(MFXLoaderBean.of("About Us", loadURL("/ir/ac/kntu/views/about.fxml")).setBeanToNodeMapper(() -> createToggle("fas-address-card", "About us")).setDefaultRoot(true).get());
        loader.addView(MFXLoaderBean.of("Logout", loadURL("/ir/ac/kntu/views/logout.fxml")).setBeanToNodeMapper(() -> createToggle("fas-arrow-right-from-bracket", "Logout")).get());
        loader.setOnLoadedAction(beans -> {
            List<ToggleButton> nodes = beans.stream()
                    .map(bean -> {
                        ToggleButton toggle = (ToggleButton) bean.getBeanToNodeMapper().get();
                        toggle.setOnAction(event -> contentPane.getChildren().setAll(bean.getRoot()));
                        if (bean.isDefaultView()) {
                            contentPane.getChildren().setAll(bean.getRoot());
                            toggle.setSelected(true);
                        }
                        return toggle;
                    })
                    .toList();
            navBar.getChildren().setAll(nodes);
        });
        loader.start();
    }


    private ToggleButton createToggle(String icon, String text) {
        return createToggle(icon, text, 0);
    }

    private ToggleButton createToggle(String icon, String text, double rotate) {
        MFXIconWrapper wrapper = new MFXIconWrapper(icon, 24, 32);
        MFXRectangleToggleNode toggleNode = new MFXRectangleToggleNode(text, wrapper);
        toggleNode.setAlignment(Pos.CENTER_LEFT);
        toggleNode.setMaxWidth(Double.MAX_VALUE);
        toggleNode.setToggleGroup(toggleGroup);
        if (rotate != 0) wrapper.getIcon().setRotate(rotate);
        return toggleNode;
    }

    public void setUser(User loggedInUser) {
        this.loggedInUser = loggedInUser;
        if (loggedInUser != null) {
            Platform.runLater(() -> username_text.setText("Welcome Back " + loggedInUser.getUsername() + "!"));
        }
    }
}
