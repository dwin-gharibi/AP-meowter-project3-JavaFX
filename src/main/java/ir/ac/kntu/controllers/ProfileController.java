package ir.ac.kntu.controllers;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.utils.DateTimeUtils;
import io.github.palexdev.materialfx.utils.others.dates.DateStringConverter;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import ir.ac.kntu.Meowter.model.Post;
import ir.ac.kntu.Meowter.model.User;
import ir.ac.kntu.Meowter.service.PostService;
import ir.ac.kntu.Meowter.service.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {

    @FXML
    private Label usernameLabel;
    @FXML
    private Label usernameText;
    @FXML
    private Label bioLabel;
    @FXML
    private Label followersLabel;
    @FXML
    private Label followingLabel;
    @FXML
    private ImageView profilePhoto;
    @FXML
    private VBox postsContainer;

    private User loggedInUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.setUser();
    }

    public void setUser() {
        this.loggedInUser = SessionManager.loadSession();
        populateUserInfo();
        populateUserPosts();
    }

    private void populateUserInfo() {
        if (loggedInUser != null) {
            usernameLabel.setText(loggedInUser.getUsername());
            usernameText.setText("@" + loggedInUser.getUsername());
            bioLabel.setText("Bio: " + loggedInUser.getBio());
            followersLabel.setText(loggedInUser.getFollowers().size() + " Followers");
            followingLabel.setText(loggedInUser.getFollowing().size() + " Following");

        }
    }


    private void populateUserPosts() {
        PostService postService = new PostService();
        if (loggedInUser != null && loggedInUser.getPosts() != null) {

            for (Post postContent : postService.getUserPosts(loggedInUser)) {
                VBox postContainer = createPost(postContent);
                postsContainer.getChildren().add(postContainer);
            }
        }
    }

    private VBox createPost(Post postContent) {
        VBox postContainer = new VBox();
        postContainer.setStyle("-fx-padding: 10; -fx-background-color: #fff; -fx-border-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.2), 5, 0, 0, 2);");

        HBox postHeader = new HBox();
        ImageView postAvatar = new ImageView();
        postHeader.getStyleClass().add("post-header");
        postAvatar.setFitWidth(40);
        postAvatar.setFitHeight(40);
        postAvatar.setImage(new Image(getClass().getResource("/images/avatar.png").toExternalForm()));
        postAvatar.getStyleClass().add("post-avatar");

        VBox postUserInfo = new VBox();
        Label postUsername = new Label(postContent.getUser().getFullname());
        postUsername.setStyle("-fx-font-weight: bold;");
        postUsername.getStyleClass().add("post-username");
        Label postHandle = new Label("@" + postContent.getUser().getUsername());
        postHandle.getStyleClass().add("post-handle");
        postUserInfo.getChildren().addAll(postUsername, postHandle);
        postHeader.getStyleClass().add("post-user-info");

        Label postTime = new Label(postContent.getCreatedAt().toString());
        postTime.setStyle("-fx-text-fill: #888;");
        postTime.getStyleClass().add("post-time");

        postHeader.getChildren().addAll(postAvatar, postUserInfo, postTime);
        postHeader.setSpacing(10);

        Label postContentLabel = new Label(postContent.getContent());
        postContentLabel.setStyle("-fx-font-size: 14px; -fx-padding: 10 0;");

        HBox postActions = new HBox();
        postActions.setSpacing(10);
        postActions.getStyleClass().add("post-actions");
        postActions.getChildren().addAll(
                createActionButton("Like", "fas-heart"),
                createActionButton("Comment", "fas-comment"),
                createActionButton("Share", "fas-retweet")
        );

        postContainer.getChildren().addAll(postHeader, postContentLabel, postActions);
        return postContainer;
    }

    private VBox createActionButton(String label, String iconDescription) {
        VBox button = new VBox();
        MFXButton actionButton = new MFXButton(label);
        actionButton.setStyle("-fx-padding: 5 15; -fx-background-color: #f0f0f0; -fx-font-size: 14px;");

        MFXFontIcon icon = new MFXFontIcon();
        icon.setDescription(iconDescription);
        icon.setSize(16);
        actionButton.setGraphic(icon);
        button.getChildren().add(actionButton);
        return button;
    }
}
