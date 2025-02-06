package ir.ac.kntu.controllers;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.controls.MFXToggleButton;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import ir.ac.kntu.Meowter.model.Post;
import ir.ac.kntu.Meowter.model.User;
import ir.ac.kntu.Meowter.service.PostService;
import ir.ac.kntu.Meowter.service.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

public class MenuDashboard {

    @FXML
    private VBox content;

    @FXML
    private MFXTextField searchField;

    @FXML
    private MFXToggleButton filter24H, filterWeek, filterMonth;

    private List<Post> allPosts;

    private User loggedInUser;

    private PostService postService;

    public MenuDashboard() {
        this.postService = new PostService();
    }

    @FXML
    public void initialize() {
        this.loggedInUser = SessionManager.loadSession();
        loadPosts();
        setupSearch();
        setupFilters();
    }

    private void loadPosts() {
        PostService postService = new PostService();
        allPosts = postService.getPostsFromFollowings(loggedInUser);
        reloadPosts();
    }

    private void reloadPosts() {
        content.getChildren().clear();
        for (Post post : allPosts) {
            content.getChildren().add(createPostItem(post));
        }
    }

    private VBox createPostItem(Post post) {
        VBox postContainer = new VBox();
        postContainer.getStyleClass().add("post-container");

        HBox postHeader = new HBox();
        postHeader.getStyleClass().add("post-header");

        ImageView avatar = new ImageView(new Image("/images/avatar.png"));
        avatar.setFitWidth(40);
        avatar.setFitHeight(40);

        VBox userInfo = new VBox();
        Label username = new Label(post.getUser().getUsername());
        username.getStyleClass().add("post-username");
        Label handle = new Label("@" + post.getUser().getUsername());
        handle.getStyleClass().add("post-handle");
        userInfo.getChildren().addAll(username, handle);

        Label postTime = new Label(post.getCreatedAt().toString());
        postTime.getStyleClass().add("post-time");

        postHeader.getChildren().addAll(avatar, userInfo, postTime);

        Label postText = new Label(post.getContent());
        postText.getStyleClass().add("post-content");

        HBox actions = new HBox();
        actions.getStyleClass().add("post-actions");

        int likes = post.getLikes().size();
        Label likeCount = new Label("");

        MFXButton likeButton = new MFXButton("");
        likeButton.getStyleClass().add("post-action-button");
        MFXFontIcon likeIcon = new MFXFontIcon("fas-heart", 16);
        likeButton.setGraphic(likeIcon);
        likeButton.setOnAction(e -> {
            likePost(post);
            reloadPosts();
        });

        MFXButton commentButton = new MFXButton("");
        commentButton.getStyleClass().add("post-action-button");
        MFXFontIcon commentIcon = new MFXFontIcon("fas-comment", 16);
        commentButton.setGraphic(commentIcon);

        commentButton.setOnAction(e -> openCommentDialog(post));

        MFXButton shareButton = new MFXButton("");
        shareButton.getStyleClass().add("post-action-button");
        MFXFontIcon shareIcon = new MFXFontIcon("fas-retweet", 16);
        shareButton.setGraphic(shareIcon);

        shareButton.setOnAction(e -> openRepostDialog(post));

        actions.getChildren().addAll(likeButton, likeCount, commentButton, shareButton);

        postContainer.getChildren().addAll(postHeader, postText, actions);
        return postContainer;
    }

    private void likePost(Post post) {
        PostService postService = new PostService();
        postService.addLike(loggedInUser, post.getId());
        reloadPosts();
    }

    private void openCommentDialog(Post post) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Add Comment");
        dialog.setHeaderText("Commenting on " + post.getUser().getUsername() + "'s post");

        ButtonType sendButtonType = new ButtonType("Send", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(sendButtonType, ButtonType.CANCEL);

        VBox dialogContent = new VBox();
        TextArea commentField = new TextArea();
        commentField.setPromptText("Write your comment...");
        dialogContent.getChildren().add(commentField);

        dialog.getDialogPane().setContent(dialogContent);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == sendButtonType) {
                String commentText = commentField.getText();
                if (!commentText.isEmpty()) {
                    PostService postService = new PostService();
                    postService.addComment(loggedInUser, post.getId(), commentText, false);
                    showAlert("Success", "Your comment has been added!");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void openRepostDialog(Post post) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Repost");
        dialog.setHeaderText("Reposting " + post.getUser().getUsername() + "'s post");

        ButtonType repostButtonType = new ButtonType("Repost", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(repostButtonType, ButtonType.CANCEL);

        VBox dialogContent = new VBox();
        TextArea repostMessage = new TextArea();
        repostMessage.setPromptText("Add a message (optional)...");
        dialogContent.getChildren().add(repostMessage);

        dialog.getDialogPane().setContent(dialogContent);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == repostButtonType) {
                String repostText = repostMessage.getText();
                PostService postService = new PostService();
                boolean[] flags = {false, false};
                postService.addPost(loggedInUser, post.getContent() + " " + repostText, flags);
                showAlert("Success", "You have reposted this post!");
                reloadPosts();
                TweetsController.getInstance().refresh();
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterPosts(newValue);
        });
    }

    private void filterPosts(String query) {
        List<Post> filteredPosts = allPosts.stream()
                .filter(post -> post.getUser().getUsername().toLowerCase().contains(query.toLowerCase()) ||
                        post.getContent().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());

        content.getChildren().clear();
        for (Post post : filteredPosts) {
            content.getChildren().add(createPostItem(post));
        }
    }

    private void setupFilters() {
        filter24H.setOnAction(e -> applyTimeFilter(24));
        filterWeek.setOnAction(e -> applyTimeFilter(168));
        filterMonth.setOnAction(e -> applyTimeFilter(720));
    }

    private void applyTimeFilter(int hours) {
        if (!filter24H.isSelected() && !filterWeek.isSelected() && !filterMonth.isSelected()) {
            reloadPosts();
            return;
        }

        List<Post> filteredPosts = allPosts.stream()
                .filter(post -> isWithinLastHours(post, hours))
                .collect(Collectors.toList());

        content.getChildren().clear();
        for (Post post : filteredPosts) {
            content.getChildren().add(createPostItem(post));
        }
    }

    public boolean isWithinLastHours(Post post, int hours) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime postTime = post.getCreatedAt().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        Duration duration = Duration.between(postTime, now);
        return duration.toHours() <= hours;
    }

}
