package com.example.vehicleidentificationsystem.controllers;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class LandingController {

    private Scene scene;
    private Runnable onLoginSuccess;

    public LandingController() {
        createLandingPage();
    }

    private void createLandingPage() {
        StackPane root = new StackPane();

        // Background image
        ImageView backgroundImage = new ImageView();
        try {
            Image bgImage = new Image(getClass().getResourceAsStream("/com/example/vehicleidentificationsystem/images/background.jpg"));
            backgroundImage.setImage(bgImage);
            backgroundImage.setFitWidth(1920);
            backgroundImage.setFitHeight(1080);
            backgroundImage.setPreserveRatio(false);
        } catch (Exception e) {
            root.setStyle("-fx-background-color: #0a0e27;");
        }

        // Subtle dark overlay (reduced opacity for better image visibility)
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.5);");

        BorderPane content = new BorderPane();
        content.setPadding(new Insets(40, 60, 60, 60));

        HBox header = createHeader();
        content.setTop(header);

        VBox hero = createHeroSection();
        content.setCenter(hero);

        HBox footer = createFooter();
        content.setBottom(footer);

        if (backgroundImage.getImage() != null) {
            root.getChildren().addAll(backgroundImage, overlay, content);
        } else {
            root.getChildren().addAll(overlay, content);
        }

        scene = new Scene(root, 1280, 800);
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(10, 0, 30, 0));
        header.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button signInBtn = createSignInButton();
        header.getChildren().addAll(spacer, signInBtn);

        return header;
    }

    private Button createSignInButton() {
        Button signInBtn = new Button("Sign In");
        // Clean, professional white button with subtle border - perfect for dark backgrounds
        signInBtn.setStyle("-fx-background-color: transparent; "
                + "-fx-text-fill: #ffffff; "
                + "-fx-font-weight: 500; "
                + "-fx-padding: 10 32; "
                + "-fx-border-color: rgba(255,255,255,0.4); "
                + "-fx-border-radius: 30; "
                + "-fx-background-radius: 30; "
                + "-fx-font-size: 13px; "
                + "-fx-cursor: hand;");

        // Hover effect
        signInBtn.setOnMouseEntered(e ->
                signInBtn.setStyle("-fx-background-color: rgba(255,255,255,0.15); "
                        + "-fx-text-fill: #ffffff; "
                        + "-fx-font-weight: 500; "
                        + "-fx-padding: 10 32; "
                        + "-fx-border-color: rgba(255,255,255,0.6); "
                        + "-fx-border-radius: 30; "
                        + "-fx-background-radius: 30; "
                        + "-fx-font-size: 13px; "
                        + "-fx-cursor: hand;")
        );

        signInBtn.setOnMouseExited(e ->
                signInBtn.setStyle("-fx-background-color: transparent; "
                        + "-fx-text-fill: #ffffff; "
                        + "-fx-font-weight: 500; "
                        + "-fx-padding: 10 32; "
                        + "-fx-border-color: rgba(255,255,255,0.4); "
                        + "-fx-border-radius: 30; "
                        + "-fx-background-radius: 30; "
                        + "-fx-font-size: 13px; "
                        + "-fx-cursor: hand;")
        );

        // Subtle glow effect instead of heavy drop shadow
        DropShadow glow = new DropShadow();
        glow.setColor(Color.rgb(255, 255, 255, 0.15));
        glow.setRadius(8);
        glow.setOffsetX(0);
        glow.setOffsetY(0);
        signInBtn.setEffect(glow);

        // Minimal fade transition
        FadeTransition fade = new FadeTransition(Duration.seconds(2), signInBtn);
        fade.setFromValue(1.0);
        fade.setToValue(0.85);
        fade.setCycleCount(FadeTransition.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();

        signInBtn.setOnAction(e -> showLoginModal());

        return signInBtn;
    }

    private VBox createHeroSection() {
        VBox hero = new VBox(20);
        hero.setAlignment(Pos.CENTER);
        hero.setPadding(new Insets(60, 0, 40, 0));

        Label title = new Label("Vehicle Identification System");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 48px; -fx-font-weight: 300; -fx-letter-spacing: -0.5px;");
        title.setFont(Font.font("System", FontWeight.LIGHT, 48));
        title.setWrapText(true);
        title.setAlignment(Pos.CENTER);

        Label subtitle = new Label("Enterprise Fleet & Asset Management Platform");
        subtitle.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 18px; -fx-font-weight: 300;");
        subtitle.setAlignment(Pos.CENTER);
        subtitle.setWrapText(true);

        hero.getChildren().addAll(title, subtitle);

        return hero;
    }

    private HBox createFooter() {
        HBox footer = new HBox();
        footer.setPadding(new Insets(40, 0, 0, 0));
        footer.setAlignment(Pos.CENTER);

        Label footerLabel = new Label("© 2025 Vehicle Identification System");
        footerLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.35); -fx-font-size: 11px;");

        footer.getChildren().add(footerLabel);

        return footer;
    }

    private void showLoginModal() {
        LoginController loginController = new LoginController();
        loginController.setOnLoginSuccess(() -> {
            System.out.println("✅ Login successful! Opening dashboard...");
            if (onLoginSuccess != null) {
                onLoginSuccess.run();
            }
        });
        loginController.show();
    }

    public void setOnLoginSuccess(Runnable callback) {
        this.onLoginSuccess = callback;
    }

    public Scene getScene() {
        return scene;
    }
}