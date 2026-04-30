package com.example.vehicleidentificationsystem.controllers;

import com.example.vehicleidentificationsystem.models.User;
import com.example.vehicleidentificationsystem.services.LoginService;
import com.example.vehicleidentificationsystem.services.SessionManager;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class LoginController {

    private Stage loginStage;
    private Runnable onLoginSuccess;
    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private ProgressIndicator loadingIndicator;
    private Label errorLabel;

    // For dragging window
    private double xOffset = 0;
    private double yOffset = 0;

    public LoginController() {
        createLoginModal();
    }

    private void createLoginModal() {
        loginStage = new Stage();
        loginStage.initModality(Modality.APPLICATION_MODAL);
        loginStage.initStyle(StageStyle.UNDECORATED);

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: rgba(0,0,0,0.4);");

        VBox card = new VBox();
        card.setPrefWidth(360);
        card.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 12;
        """);

        DropShadow shadow = new DropShadow(15, Color.rgb(0,0,0,0.2));
        card.setEffect(shadow);

        // 🔴 TOP BAR WITH CLOSE BUTTON
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setPadding(new Insets(5, 10, 5, 10));
        topBar.setStyle("""
            -fx-background-color: #1d3557;
            -fx-background-radius: 12 12 0 0;
        """);

        Button closeButton = new Button("X");
        closeButton.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: white;
            -fx-font-size: 12px;
            -fx-font-weight: bold;
        """);

        // Hover effect (red)
        closeButton.setOnMouseEntered(e ->
                closeButton.setStyle("""
                    -fx-background-color: #e63946;
                    -fx-text-fill: white;
                    -fx-font-weight: bold;
                """)
        );

        closeButton.setOnMouseExited(e ->
                closeButton.setStyle("""
                    -fx-background-color: transparent;
                    -fx-text-fill: white;
                    -fx-font-weight: bold;
                """)
        );

        closeButton.setOnAction(e -> loginStage.close());

        topBar.getChildren().add(closeButton);

        // 🔵 DRAGGING FUNCTIONALITY
        topBar.setOnMousePressed(e -> {
            xOffset = e.getSceneX();
            yOffset = e.getSceneY();
        });

        topBar.setOnMouseDragged(e -> {
            loginStage.setX(e.getScreenX() - xOffset);
            loginStage.setY(e.getScreenY() - yOffset);
        });

        // 🧾 CONTENT
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Vehicle Identification System");
        titleLabel.setStyle("""
            -fx-font-size: 16px;
            -fx-font-weight: bold;
        """);

        Label subtitleLabel = new Label("Sign in to your account");
        subtitleLabel.setStyle("""
            -fx-font-size: 12px;
            -fx-text-fill: #777;
        """);

        usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setStyle(inputStyle());

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle(inputStyle());

        VBox formFields = new VBox(10, usernameField, passwordField);

        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #e63946; -fx-font-size: 11px;");
        errorLabel.setVisible(false);

        loginButton = new Button("Sign In");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setStyle(buttonStyle());

        loginButton.setOnMouseEntered(e -> loginButton.setStyle(buttonHoverStyle()));
        loginButton.setOnMouseExited(e -> loginButton.setStyle(buttonStyle()));

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);
        loadingIndicator.setMaxSize(18, 18);

        HBox buttonBox = new HBox(10, loginButton, loadingIndicator);
        buttonBox.setAlignment(Pos.CENTER);

        loginButton.setOnAction(e -> handleLogin());
        passwordField.setOnAction(e -> handleLogin());

        content.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                formFields,
                errorLabel,
                buttonBox
        );

        card.getChildren().addAll(topBar, content);
        root.getChildren().add(card);

        Scene scene = new Scene(root, 400, 400);
        loginStage.setScene(scene);

        // Fade animation
        FadeTransition fade = new FadeTransition(Duration.millis(400), card);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private String inputStyle() {
        return """
            -fx-background-radius: 8;
            -fx-border-radius: 8;
            -fx-border-color: #ccc;
            -fx-padding: 8 10;
            -fx-font-size: 12px;
        """;
    }

    private String buttonStyle() {
        return """
            -fx-background-color: #1d3557;
            -fx-text-fill: white;
            -fx-font-size: 13px;
            -fx-background-radius: 8;
            -fx-padding: 8 16;
        """;
    }

    private String buttonHoverStyle() {
        return """
            -fx-background-color: #457b9d;
            -fx-text-fill: white;
            -fx-font-size: 13px;
            -fx-background-radius: 8;
            -fx-padding: 8 16;
        """;
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        errorLabel.setVisible(false);

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both username and password");
            errorLabel.setVisible(true);
            return;
        }

        loginButton.setDisable(true);
        loadingIndicator.setVisible(true);

        Thread loginThread = new Thread(() -> {
            User user = LoginService.authenticate(username, password);

            javafx.application.Platform.runLater(() -> {
                loginButton.setDisable(false);
                loadingIndicator.setVisible(false);

                if (user != null) {
                    SessionManager.getInstance().setCurrentUser(user);
                    loginStage.close();

                    if (onLoginSuccess != null) {
                        onLoginSuccess.run();
                    }
                } else {
                    errorLabel.setText("Invalid username or password");
                    errorLabel.setVisible(true);
                }
            });
        });

        loginThread.setDaemon(true);
        loginThread.start();
    }

    public void setOnLoginSuccess(Runnable callback) {
        this.onLoginSuccess = callback;
    }

    public void show() {
        usernameField.clear();
        passwordField.clear();
        errorLabel.setVisible(false);
        loginStage.showAndWait();
    }
}