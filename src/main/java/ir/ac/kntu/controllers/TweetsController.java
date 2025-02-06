package ir.ac.kntu.controllers;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.mfxresources.fonts.MFXFontIcon;
import ir.ac.kntu.Meowter.model.Comment;
import ir.ac.kntu.Meowter.model.Post;
import ir.ac.kntu.Meowter.model.User;
import ir.ac.kntu.Meowter.service.PostService;
import ir.ac.kntu.Meowter.service.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class TweetsController {

    private static final Logger log = LoggerFactory.getLogger(TweetsController.class);

    @FXML
    private VBox postsContainer;

    @FXML
    private MFXButton createPostButton;

    private User loggedInUser;
    private final PostService postService;

    private static TweetsController instance;

    public static TweetsController getInstance() {
        return instance;
    }

    public TweetsController() {
        this.postService = new PostService();
        instance = this;
    }

    @FXML
    public void initialize() {
        loggedInUser = SessionManager.loadSession();
        refresh();
    }

    public void refresh() {
        Platform.runLater(() -> {
            postsContainer.getChildren().clear();
            postService.getUserPosts(loggedInUser).forEach(post -> postsContainer.getChildren().add(createPostBox(post)));
        });
    }

    private HBox createPostBox(Post post) {
        HBox postBox = new HBox();
        postBox.getStyleClass().add("post-container");

        ImageView avatar = new ImageView(new Image("/images/avatar.png"));
        avatar.setFitWidth(40);
        avatar.setFitHeight(40);
        avatar.getStyleClass().add("post-avatar");

        VBox userInfo = new VBox();
        Label usernameLabel = new Label(post.getUser().getUsername());
        usernameLabel.getStyleClass().add("post-username");
        Label handleLabel = new Label("@" + post.getUser().getUsername());
        handleLabel.getStyleClass().add("post-handle");
        userInfo.getChildren().addAll(usernameLabel, handleLabel);
        userInfo.getStyleClass().add("post-user-info");

        HBox postStats = new HBox();
        postStats.getStyleClass().add("post-stats");
        Label likesLabel = new Label(post.getLikes().size() + " Likes");
        Label commentsLabel = new Label(post.getComments().size() + " Comments");
        postStats.getChildren().addAll(likesLabel, commentsLabel);

        Label postContent = new Label(removeAnsiCodes(post.getContent()));
        postContent.getStyleClass().add("post-content");

        HBox actions = new HBox();
        actions.getStyleClass().add("post-actions");

        MFXButton editButton = new MFXButton("");

        editButton.getStyleClass().add("post-action-button");
        MFXFontIcon editIcon = new MFXFontIcon("fas-pen", 16);
        editButton.setGraphic(editIcon);

        editButton.setOnAction(event -> editPost(post));

        MFXButton deleteButton = new MFXButton("");
        deleteButton.getStyleClass().add("post-action-button");
        MFXFontIcon deleteIcon = new MFXFontIcon("fas-circle-xmark", 16);
        deleteButton.setGraphic(deleteIcon);

        deleteButton.setOnAction(event -> deletePost(post));

        MFXButton viewCommentsButton = new MFXButton("");
        viewCommentsButton.getStyleClass().add("post-action-button");
        MFXFontIcon commentIcon = new MFXFontIcon("fas-comment", 16);
        viewCommentsButton.setGraphic(commentIcon);

        viewCommentsButton.setOnAction(event -> showCommentsDialog(post));


        actions.getChildren().addAll(editButton, deleteButton, viewCommentsButton);

        postBox.getChildren().addAll(avatar, userInfo, postContent, postStats, actions);
        return postBox;
    }

    @FXML
    public void createNewPost(javafx.event.ActionEvent event) {
        showPostDialog(null);
    }

    private void editPost(Post post) {
        showPostDialog(post);
    }

    public static String removeAnsiCodes(String input) {
        return input.replaceAll("\u001B\\[[;\\d]*[A-Za-z]", "");
    }

    private void showPostDialog(Post post) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(post == null ? "Create New Post" : "Edit Post");
        dialog.setHeaderText(post == null ? "Write a new post:" : "Edit your post:");

        TextArea postField = new TextArea();
        postField.setPromptText("Write your post here...");
        postField.setWrapText(true);

        if (post != null) {
            postField.setText(removeAnsiCodes(post.getContent()));
        }

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(postField);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return postField.getText().trim();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newContent -> {
            if (!newContent.isEmpty()) {
                if (post == null) {
                    postService.addPost(loggedInUser, newContent, new boolean[]{false, false});
                } else {
                    postService.editPost(loggedInUser, post.getId(), newContent);
                }
                refresh();
            } else {
                showError("Post content cannot be empty!");
            }
        });
    }

    private void deletePost(Post post) {
        postService.deletePost(loggedInUser, post.getId());
        refresh();
    }

    private void showCommentsDialog(Post post) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Post Comments");
        dialog.setHeaderText("Comments on this post:");

        VBox commentsBox = new VBox();
        loadComments(post, commentsBox, dialog);

        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void loadComments(Post post, VBox commentsBox, Dialog<Void> dialog) {
        commentsBox.getChildren().clear();

        if (!post.getComments().isEmpty()) {
            post.getComments().forEach(comment -> {
                HBox commentBox = new HBox();
                commentBox.getStyleClass().add("comment-container");

                Label commentLabel = new Label(comment.getUser().getUsername() + ": " + comment.getContent());
                commentLabel.getStyleClass().add("comment-text");

                MFXButton deleteCommentButton = new MFXButton("Delete");
                deleteCommentButton.getStyleClass().add("comment-delete-button");
                deleteCommentButton.setOnAction(event -> {
                    postService.removeComment(loggedInUser, post.getId() ,comment.getId());
                    post.getComments().remove(comment);
                    loadComments(post, commentsBox, dialog);
                    refresh();
                });

                commentBox.getChildren().addAll(commentLabel, deleteCommentButton);
                commentsBox.getChildren().add(commentBox);
                refresh();
            });
        } else {
            commentsBox.getChildren().add(new Label("No comments yet."));
        }

        dialog.getDialogPane().setContent(commentsBox);
    }

    private void processComments(Post post, Set<Comment> comments, VBox commentsBox) {
        for (Comment comment : comments) {
            HBox commentBox = new HBox();
            commentBox.getStyleClass().add("comment-container");

            Label commentLabel = new Label(comment.getUser().getUsername() + ": " + comment.getContent());
            commentLabel.getStyleClass().add("comment-text");

            MFXButton deleteCommentButton = new MFXButton("Delete");
            deleteCommentButton.getStyleClass().add("comment-delete-button");
            deleteCommentButton.setOnAction(event -> {
                postService.removeComment(loggedInUser, post.getId() ,comment.getId());
                refresh();
            });

            commentBox.getChildren().addAll(commentLabel, deleteCommentButton);
            commentsBox.getChildren().add(commentBox);
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
