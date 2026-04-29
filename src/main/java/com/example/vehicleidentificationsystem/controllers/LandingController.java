package com.example.vehicleidentificationsystem.controllers;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.util.Duration;

public class LandingController {

    private Scene scene;
    private Runnable onLoginSuccess;

    public LandingController() {
        createLandingPage();
    }

    private void createLandingPage() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        // Header with Sign In button
        HBox header = createHeader();
        root.setTop(header);

        // Hero section
        VBox hero = createHeroSection();
        root.setCenter(hero);

        // Create scene with same size as dashboard (1200x700)
        scene = new Scene(root, 1200, 700);
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(10, 20, 10, 20));
        header.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button signInBtn = new Button("Sign In");

        // Fade transition effect
        FadeTransition fade = new FadeTransition(Duration.seconds(1.5), signInBtn);
        fade.setFromValue(1.0);
        fade.setToValue(0.6);
        fade.setCycleCount(FadeTransition.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();

        signInBtn.setOnAction(e -> showLoginModal());

        header.getChildren().addAll(spacer, signInBtn);

        return header;
    }

    private VBox createHeroSection() {
        VBox hero = new VBox(15);
        hero.setAlignment(Pos.CENTER);
        hero.setPadding(new Insets(20));

        Label title = new Label("Vehicle Identification System");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        Label subtitle = new Label("Identify vehicles, track service history, manage insurance,\nand access police reports all in one place.");
        subtitle.setStyle("-fx-font-size: 14px;");
        subtitle.setAlignment(Pos.CENTER);

        hero.getChildren().addAll(title, subtitle);

        return hero;
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