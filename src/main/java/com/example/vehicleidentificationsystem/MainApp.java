package com.example.vehicleidentificationsystem;

import com.example.vehicleidentificationsystem.controllers.LandingController;
import com.example.vehicleidentificationsystem.services.DatabaseService;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainApp extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        showLandingPage();
    }

    private void showLandingPage() {
        LandingController landingController = new LandingController();
        landingController.setOnLoginSuccess(this::showDashboard);

        Scene scene = landingController.getScene();
        primaryStage.setTitle("Vehicle Identification System");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(500);
        primaryStage.setWidth(1200);
        primaryStage.setHeight(700);
        primaryStage.show();
    }

    private void showDashboard() {
        System.out.println("🚀 Opening Dashboard...");

        // Create new stage for dashboard
        Stage dashboardStage = new Stage();
        dashboardStage.setTitle("Vehicle Identification System - Dashboard");
        dashboardStage.setMinWidth(900);
        dashboardStage.setMinHeight(500);
        dashboardStage.setWidth(1200);
        dashboardStage.setHeight(700);

        // Close landing page
        primaryStage.close();

        // Show dashboard
        com.example.vehicleidentificationsystem.controllers.DashboardController dashboardController =
                new com.example.vehicleidentificationsystem.controllers.DashboardController();
        dashboardController.show(dashboardStage);
    }

    public static void main(String[] args) {
        // Test database connection before launching UI
        System.out.println("🔌 Testing PostgreSQL connection...");

        boolean connected = DatabaseService.testSuperUserConnection();
        if (connected) {
            System.out.println("✅ Database connection successful! PostgreSQL is running on port 5433");
        } else {
            System.err.println("❌ Cannot connect to PostgreSQL. Make sure it's running on port 5433");
            System.err.println("   Check: password is 'PABALLO123' and database 'vehicle_db' exists");
        }

        launch(args);
    }
}