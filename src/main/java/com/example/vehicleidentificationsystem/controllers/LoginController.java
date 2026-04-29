package com.example.vehicleidentificationsystem.controllers;

import com.example.vehicleidentificationsystem.models.User;
import com.example.vehicleidentificationsystem.services.LoginService;
import com.example.vehicleidentificationsystem.services.SessionManager;
import com.example.vehicleidentificationsystem.utils.AlertUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class LoginController {

    private Stage loginStage;
    private Runnable onLoginSuccess;
    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private ProgressIndicator loadingIndicator;

    public LoginController() {
        createLoginModal();
    }

    private void createLoginModal() {
        loginStage = new Stage();
        loginStage.initModality(Modality.APPLICATION_MODAL);
        loginStage.initStyle(StageStyle.UTILITY);
        loginStage.setTitle("Login - Vehicle Identification System");

        // Main VBox as root
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);

        // Title - No VIS, just plain text
        Label titleLabel = new Label("Vehicle Identification System");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label subtitleLabel = new Label("Please sign in to continue");

        // Separator
        Separator separator = new Separator();

        // Form fields
        VBox formFields = new VBox(8);

        Label usernameLabel = new Label("Username:");
        usernameField = new TextField();
        usernameField.setPromptText("Enter your username");

        Label passwordLabel = new Label("Password:");
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");

        formFields.getChildren().addAll(usernameLabel, usernameField, passwordLabel, passwordField);

        // Login button - Default JavaFX style (NOT red)
        loginButton = new Button("Sign In");
        loginButton.setMaxWidth(Double.MAX_VALUE);

        // Loading indicator
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);
        loadingIndicator.setMaxSize(25, 25);

        HBox buttonBox = new HBox(10, loginButton, loadingIndicator);
        buttonBox.setAlignment(Pos.CENTER);

        // Demo credentials hint
        VBox demoHint = new VBox(5);
        demoHint.setAlignment(Pos.CENTER);

        Label demoLabel = new Label("Demo Credentials:");
        demoLabel.setStyle("-fx-font-size: 11px;");

        Label demoCreds = new Label("admin_user / admin123");
        demoCreds.setStyle("-fx-font-size: 11px;");

        demoHint.getChildren().addAll(demoLabel, demoCreds);

        loginButton.setOnAction(e -> handleLogin());

        root.getChildren().addAll(titleLabel, subtitleLabel, separator, formFields, buttonBox, demoHint);

        root.setPrefWidth(350);
        root.setPrefHeight(380);

        Scene scene = new Scene(root);
        loginStage.setScene(scene);
        loginStage.setResizable(false);
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            AlertUtils.showWarning("Validation Error", "Please enter both username and password.");
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
                    System.out.println("✅ User object created: " + user.getUsername() + " - " + user.getRoleDisplayName());
                    SessionManager.getInstance().setCurrentUser(user);
                    loginStage.close();
                    if (onLoginSuccess != null) {
                        onLoginSuccess.run();
                    }
                } else {
                    AlertUtils.showError("Login Failed", "Invalid username or password.");
                }
            });
        });
        loginThread.start();
    }

    public void setOnLoginSuccess(Runnable callback) {
        this.onLoginSuccess = callback;
    }

    public void show() {
        usernameField.clear();
        passwordField.clear();
        loginStage.showAndWait();
    }
}