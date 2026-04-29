package com.example.vehicleidentificationsystem.controllers;

import com.example.vehicleidentificationsystem.MainApp;
import com.example.vehicleidentificationsystem.models.*;
import com.example.vehicleidentificationsystem.services.InsuranceService;
import com.example.vehicleidentificationsystem.services.PoliceService;
import com.example.vehicleidentificationsystem.services.SessionManager;
import com.example.vehicleidentificationsystem.services.VehicleService;
import com.example.vehicleidentificationsystem.utils.AlertUtils;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

public class DashboardController {

    private Stage primaryStage;
    private User currentUser;
    private TabPane mainTabPane;

    public void show(Stage stage) {
        this.primaryStage = stage;
        this.currentUser = SessionManager.getInstance().getCurrentUser();

        System.out.println("📊 Building dashboard for: " + currentUser.getUsername() + " (Role: " + currentUser.getRole() + ")");

        BorderPane root = new BorderPane();

        VBox topSection = new VBox(0);
        topSection.getChildren().addAll(createMenuBar(), createTopBar());
        root.setTop(topSection);

        mainTabPane = createTabPane();
        root.setCenter(mainTabPane);

        Scene scene = new Scene(root, 1200, 700);

        primaryStage.setTitle("Vehicle Identification System - Dashboard");
        primaryStage.setScene(scene);
        primaryStage.show();

        FadeTransition fade = new FadeTransition(Duration.seconds(0.5), root);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        System.out.println("✅ Dashboard displayed successfully!");
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");

        MenuItem refreshItem = new MenuItem("Refresh");
        refreshItem.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN));
        refreshItem.setOnAction(e -> refreshDashboard());

        SeparatorMenuItem separator = new SeparatorMenuItem();

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
        exitItem.setOnAction(e -> handleLogout());

        fileMenu.getItems().addAll(refreshItem, separator, exitItem);

        Menu helpMenu = new Menu("Help");

        MenuItem helpItem = new MenuItem("Help");
        helpItem.setAccelerator(new KeyCodeCombination(KeyCode.F1, KeyCombination.SHORTCUT_DOWN));
        helpItem.setOnAction(e -> showHelpDialog());

        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> showAboutDialog());

        helpMenu.getItems().addAll(helpItem, new SeparatorMenuItem(), aboutItem);

        menuBar.getMenus().addAll(fileMenu, helpMenu);

        return menuBar;
    }

    private void refreshDashboard() {
        mainTabPane.getTabs().clear();
        mainTabPane = createTabPane();
        BorderPane root = (BorderPane) primaryStage.getScene().getRoot();
        root.setCenter(mainTabPane);
        AlertUtils.showInfo("Refreshed", "Dashboard has been refreshed.");
    }

    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Vehicle Identification System");
        alert.setHeaderText("Vehicle Identification System (VIS)");
        alert.setContentText("Version: 1.0\n\n" +
                "A comprehensive system for managing vehicles,\n" +
                "service records, police reports, and insurance.\n\n" +
                "© 2024 - All Rights Reserved");
        alert.showAndWait();
    }

    private void showHelpDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Help");
        alert.setHeaderText("Vehicle Identification System Help");
        alert.setContentText("Modules:\n\n" +
                "• Workshop Module - Manage vehicles and service records\n" +
                "• Police Module - File accident/theft reports and violations\n" +
                "• Insurance Module - Manage vehicle insurance policies\n" +
                "• Customer Module - View vehicles and submit queries\n" +
                "• User Management - Manage system users\n\n" +
                "Keyboard Shortcuts:\n" +
                "• Ctrl+R - Refresh dashboard\n" +
                "• Ctrl+Q - Exit application\n" +
                "• F1 - Help");
        alert.showAndWait();
    }

    private HBox createTopBar() {
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(10, 20, 10, 20));
        topBar.setAlignment(Pos.CENTER_LEFT);

        TabPane navTabPane = new TabPane();
        navTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        HBox.setHgrow(navTabPane, Priority.ALWAYS);

        String role = currentUser.getRole();

        // Home Tab - EVERYONE sees this
        Tab homeNavTab = new Tab("Home");
        homeNavTab.setClosable(false);
        homeNavTab.setOnSelectionChanged(e -> {
            if (homeNavTab.isSelected() && mainTabPane != null) {
                mainTabPane.getSelectionModel().select(0);
            }
        });
        navTabPane.getTabs().add(homeNavTab);

        // For CUSTOMER role: ONLY show Home and Customer Module
        if (role.equals("CUSTOMER")) {
            // Customer Module Tab
            Tab customerNavTab = new Tab("Customer Portal");
            customerNavTab.setClosable(false);
            customerNavTab.setOnSelectionChanged(e -> {
                if (customerNavTab.isSelected() && mainTabPane != null) {
                    mainTabPane.getSelectionModel().select(1);
                }
            });
            navTabPane.getTabs().add(customerNavTab);
        }
        // For ADMIN role: Show all tabs
        else if (role.equals("ADMIN")) {
            // Workshop Module Tab
            Tab workshopNavTab = new Tab("Workshop Module");
            workshopNavTab.setClosable(false);
            workshopNavTab.setOnSelectionChanged(e -> {
                if (workshopNavTab.isSelected() && mainTabPane != null) {
                    mainTabPane.getSelectionModel().select(1);
                }
            });
            navTabPane.getTabs().add(workshopNavTab);

            // Police Module Tab
            Tab policeNavTab = new Tab("Police Module");
            policeNavTab.setClosable(false);
            policeNavTab.setOnSelectionChanged(e -> {
                if (policeNavTab.isSelected() && mainTabPane != null) {
                    mainTabPane.getSelectionModel().select(2);
                }
            });
            navTabPane.getTabs().add(policeNavTab);

            // Insurance Module Tab
            Tab insuranceNavTab = new Tab("Insurance Module");
            insuranceNavTab.setClosable(false);
            insuranceNavTab.setOnSelectionChanged(e -> {
                if (insuranceNavTab.isSelected() && mainTabPane != null) {
                    mainTabPane.getSelectionModel().select(3);
                }
            });
            navTabPane.getTabs().add(insuranceNavTab);

            // Customer Module Tab
            Tab customerNavTab = new Tab("Customer Portal");
            customerNavTab.setClosable(false);
            customerNavTab.setOnSelectionChanged(e -> {
                if (customerNavTab.isSelected() && mainTabPane != null) {
                    mainTabPane.getSelectionModel().select(4);
                }
            });
            navTabPane.getTabs().add(customerNavTab);

            // Queries Tab
            Tab queriesNavTab = new Tab("Queries");
            queriesNavTab.setClosable(false);
            queriesNavTab.setOnSelectionChanged(e -> {
                if (queriesNavTab.isSelected() && mainTabPane != null) {
                    mainTabPane.getSelectionModel().select(5);
                }
            });
            navTabPane.getTabs().add(queriesNavTab);

            // User Management Tab
            Tab adminNavTab = new Tab("User Management");
            adminNavTab.setClosable(false);
            adminNavTab.setOnSelectionChanged(e -> {
                if (adminNavTab.isSelected() && mainTabPane != null) {
                    mainTabPane.getSelectionModel().select(6);
                }
            });
            navTabPane.getTabs().add(adminNavTab);
        }
        // For WORKSHOP role
        else if (role.equals("WORKSHOP")) {
            Tab workshopNavTab = new Tab("Workshop Module");
            workshopNavTab.setClosable(false);
            workshopNavTab.setOnSelectionChanged(e -> {
                if (workshopNavTab.isSelected() && mainTabPane != null) {
                    mainTabPane.getSelectionModel().select(1);
                }
            });
            navTabPane.getTabs().add(workshopNavTab);
        }
        // For POLICE role
        else if (role.equals("POLICE")) {
            Tab policeNavTab = new Tab("Police Module");
            policeNavTab.setClosable(false);
            policeNavTab.setOnSelectionChanged(e -> {
                if (policeNavTab.isSelected() && mainTabPane != null) {
                    mainTabPane.getSelectionModel().select(1);
                }
            });
            navTabPane.getTabs().add(policeNavTab);
        }
        // For INSURANCE role
        else if (role.equals("INSURANCE")) {
            Tab insuranceNavTab = new Tab("Insurance Module");
            insuranceNavTab.setClosable(false);
            insuranceNavTab.setOnSelectionChanged(e -> {
                if (insuranceNavTab.isSelected() && mainTabPane != null) {
                    mainTabPane.getSelectionModel().select(1);
                }
            });
            navTabPane.getTabs().add(insuranceNavTab);
        }

        HBox userBox = new HBox(15);
        userBox.setAlignment(Pos.CENTER_RIGHT);

        Label userLabel = new Label("Welcome back, " + currentUser.getFullName());
        Label roleLabel = new Label("(" + currentUser.getRoleDisplayName() + ")");

        Button logoutBtn = new Button("Logout");
        logoutBtn.setOnAction(e -> handleLogout());

        userBox.getChildren().addAll(userLabel, roleLabel, logoutBtn);

        topBar.getChildren().addAll(navTabPane, userBox);

        return topBar;
    }

    private TabPane createTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setTabMinHeight(40);
        tabPane.setStyle("-fx-tab-max-height: 0; -fx-tab-min-height: 0; -fx-tab-max-width: 0;");

        String role = currentUser.getRole();

        // Home Tab - EVERYONE
        Tab homeTab = new Tab("Home");
        homeTab.setClosable(false);
        homeTab.setContent(createHomeContent());
        tabPane.getTabs().add(homeTab);

        // For CUSTOMER role: ONLY Home and Customer Portal
        if (role.equals("CUSTOMER")) {
            // Customer Module Tab
            Tab customerTab = new Tab("Customer Portal");
            customerTab.setClosable(false);
            CustomerController customerController = new CustomerController();
            customerTab.setContent(customerController.createView(currentUser));
            tabPane.getTabs().add(customerTab);
        }
        // For ADMIN role: All tabs
        else if (role.equals("ADMIN")) {
            // Workshop Module
            Tab workshopTab = new Tab("Workshop Module");
            workshopTab.setClosable(false);
            workshopTab.setContent(createWorkshopModuleContent());
            tabPane.getTabs().add(workshopTab);

            // Police Module
            Tab policeTab = new Tab("Police Module");
            policeTab.setClosable(false);
            PoliceController policeController = new PoliceController();
            policeTab.setContent(policeController.createView(currentUser));
            tabPane.getTabs().add(policeTab);

            // Insurance Module
            Tab insuranceTab = new Tab("Insurance Module");
            insuranceTab.setClosable(false);
            InsuranceController insuranceController = new InsuranceController();
            insuranceTab.setContent(insuranceController.createView(currentUser));
            tabPane.getTabs().add(insuranceTab);

            // Customer Module
            Tab customerTab = new Tab("Customer Portal");
            customerTab.setClosable(false);
            CustomerController customerController = new CustomerController();
            customerTab.setContent(customerController.createView(currentUser));
            tabPane.getTabs().add(customerTab);

            // Queries Tab
            Tab queriesTab = new Tab("Queries");
            queriesTab.setClosable(false);
            QueryManagementController queryManagementController = new QueryManagementController();
            queriesTab.setContent(queryManagementController.createView(currentUser));
            tabPane.getTabs().add(queriesTab);

            // User Management Tab
            Tab adminTab = new Tab("User Management");
            adminTab.setClosable(false);
            AdminController adminController = new AdminController();
            adminTab.setContent(adminController.createView(currentUser));
            tabPane.getTabs().add(adminTab);
        }
        // For WORKSHOP role
        else if (role.equals("WORKSHOP")) {
            Tab workshopTab = new Tab("Workshop Module");
            workshopTab.setClosable(false);
            workshopTab.setContent(createWorkshopModuleContent());
            tabPane.getTabs().add(workshopTab);
        }
        // For POLICE role
        else if (role.equals("POLICE")) {
            Tab policeTab = new Tab("Police Module");
            policeTab.setClosable(false);
            PoliceController policeController = new PoliceController();
            policeTab.setContent(policeController.createView(currentUser));
            tabPane.getTabs().add(policeTab);
        }
        // For INSURANCE role
        else if (role.equals("INSURANCE")) {
            Tab insuranceTab = new Tab("Insurance Module");
            insuranceTab.setClosable(false);
            InsuranceController insuranceController = new InsuranceController();
            insuranceTab.setContent(insuranceController.createView(currentUser));
            tabPane.getTabs().add(insuranceTab);
        }

        return tabPane;
    }

    private VBox createWorkshopModuleContent() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(10));

        Label moduleTitle = new Label("Workshop Module");
        moduleTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TabPane workshopTabPane = new TabPane();
        workshopTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Vehicle Management Tab inside Workshop Module
        Tab vehiclesInnerTab = new Tab("Vehicle Management");
        vehiclesInnerTab.setClosable(false);
        VehicleController vehicleController = new VehicleController();
        vehiclesInnerTab.setContent(vehicleController.createView(currentUser));
        workshopTabPane.getTabs().add(vehiclesInnerTab);

        // Service Records Tab inside Workshop Module
        Tab serviceInnerTab = new Tab("Service Records");
        serviceInnerTab.setClosable(false);
        WorkshopController workshopController = new WorkshopController();
        serviceInnerTab.setContent(workshopController.createView(currentUser));
        workshopTabPane.getTabs().add(serviceInnerTab);

        container.getChildren().addAll(moduleTitle, workshopTabPane);

        return container;
    }

    private ScrollPane createHomeContent() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setPadding(new Insets(0));

        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));

        VBox welcomeCard = new VBox(10);
        welcomeCard.setPadding(new Insets(15));
        welcomeCard.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-color: #f9f9f9;");

        Label welcomeLabel = new Label("Welcome to Vehicle Identification System");
        welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label welcomeMessage = new Label("You are logged in as " + currentUser.getRoleDisplayName() +
                ". Use the tabs above to access your authorized modules.");

        welcomeCard.getChildren().addAll(welcomeLabel, welcomeMessage);

        GridPane statsGrid = createStatsGrid();
        HBox splitSection = createSplitStudentSection();

        mainContent.getChildren().addAll(welcomeCard, statsGrid, splitSection);

        scrollPane.setContent(mainContent);

        return scrollPane;
    }

    private GridPane createStatsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(20, 0, 30, 0));

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(25);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(25);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(25);
        ColumnConstraints col4 = new ColumnConstraints();
        col4.setPercentWidth(25);
        grid.getColumnConstraints().addAll(col1, col2, col3, col4);

        int totalVehicles = getTotalVehicles();
        int activeInsurance = getActiveInsuranceCount();
        int policeReports = getPoliceReportsCount();
        int serviceRecords = getServiceRecordsCount();

        VBox card1 = createStatCard("Total Vehicles", String.valueOf(totalVehicles), "Active fleet vehicles");
        grid.add(card1, 0, 0);

        VBox card2 = createStatCard("Active Insurance", String.valueOf(activeInsurance), "Active policies");
        grid.add(card2, 1, 0);

        VBox card3 = createStatCard("Police Reports", String.valueOf(policeReports), "Total reports filed");
        grid.add(card3, 2, 0);

        VBox card4 = createStatCard("Service Records", String.valueOf(serviceRecords), "Total service records");
        grid.add(card4, 3, 0);

        card1.setMaxWidth(Double.MAX_VALUE);
        card2.setMaxWidth(Double.MAX_VALUE);
        card3.setMaxWidth(Double.MAX_VALUE);
        card4.setMaxWidth(Double.MAX_VALUE);

        return grid;
    }

    private VBox createStatCard(String title, String value, String subtitle) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(20, 15, 20, 15));
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-border-color: #ddd; -fx-border-radius: 8; -fx-background-color: white; -fx-background-radius: 8;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #999;");

        card.getChildren().addAll(titleLabel, valueLabel, subtitleLabel);

        return card;
    }

    private int getTotalVehicles() {
        try {
            List<Vehicle> vehicles = VehicleService.getAllVehicles(currentUser);
            return vehicles.size();
        } catch (Exception e) {
            return 0;
        }
    }

    private int getActiveInsuranceCount() {
        try {
            List<Insurance> insuranceList = InsuranceService.getAllInsurance(currentUser);
            int count = 0;
            for (Insurance i : insuranceList) {
                if ("Active".equals(i.getStatus())) {
                    count++;
                }
            }
            return count;
        } catch (Exception e) {
            return 0;
        }
    }

    private int getPoliceReportsCount() {
        try {
            List<PoliceReport> reports = PoliceService.getAllPoliceReports(currentUser);
            return reports.size();
        } catch (Exception e) {
            return 0;
        }
    }

    private int getServiceRecordsCount() {
        try {
            String sql = "SELECT COUNT(*) FROM service_record";
            try (java.sql.PreparedStatement pstmt = currentUser.getDbConnection().prepareStatement(sql);
                 java.sql.ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            return 0;
        }
        return 0;
    }

    private HBox createSplitStudentSection() {
        HBox splitPane = new HBox(20);
        splitPane.setPadding(new Insets(10, 0, 10, 0));
        splitPane.setAlignment(Pos.TOP_CENTER);

        VBox leftSection = createPaginationSection();
        VBox rightSection = createScrollableProgressSection();

        leftSection.setPrefWidth(550);
        rightSection.setPrefWidth(550);
        leftSection.setMaxWidth(550);
        rightSection.setMaxWidth(550);

        splitPane.getChildren().addAll(leftSection, rightSection);

        return splitPane;
    }

    private VBox createPaginationSection() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(15));
        container.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-color: #fafafa;");

        Label sectionTitle = new Label("Student Records (Pagination + ScrollPane)");
        sectionTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        String[] students = {
                "Thabo Mokoena", "Mpho Letsie", "Lerato Molapo", "Palesa Motsoeneng", "Tumelo Khotha",
                "Refiloe Nteso", "Maserame Mofolo", "Ntate Mahlaha", "Mamello Seisa", "Katleho Mothae",
                "Lebohang Ramathe", "Mmathabo Moeketsi", "Tsepo Ntsoane", "Mahlomola Makhele", "Lineo Phoofolo",
                "Rethabile Mohapi", "Mponeng Mofoka", "Neo Moteane", "Mpho Mohlomi", "Naledi Tšiu"
        };

        double[] progressPercentages = {
                85, 62, 93, 45, 78, 91, 54, 67, 82, 39, 71, 88, 76, 52, 94, 63, 49, 81, 75, 68
        };

        int studentsPerPage = 5;
        int totalPages = (int) Math.ceil((double) students.length / studentsPerPage);

        Pagination pagination = new Pagination(totalPages, 0);
        pagination.setPageFactory(pageIndex -> {
            VBox pageContent = new VBox(8);
            pageContent.setPadding(new Insets(10));

            int startIndex = pageIndex * studentsPerPage;
            int endIndex = Math.min(startIndex + studentsPerPage, students.length);

            Label pageHeader = new Label("Page " + (pageIndex + 1) + " (Students " + (startIndex + 1) + " to " + endIndex + ")");
            pageHeader.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #555;");
            pageContent.getChildren().add(pageHeader);

            for (int i = startIndex; i < endIndex; i++) {
                VBox studentItem = new VBox(5);
                studentItem.setStyle("-fx-padding: 8; -fx-background-color: #f9f9f9; -fx-border-color: #eee; -fx-border-radius: 5;");

                HBox nameRow = new HBox(10);
                nameRow.setAlignment(Pos.CENTER_LEFT);

                Label numberLabel = new Label(String.format("%02d.", (i + 1)));
                numberLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 35;");

                Label nameLabel = new Label(students[i]);
                nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

                nameRow.getChildren().addAll(numberLabel, nameLabel);

                HBox progressRow = new HBox(10);
                progressRow.setAlignment(Pos.CENTER_LEFT);

                double progressValue = progressPercentages[i] / 100.0;
                ProgressBar progressBar = new ProgressBar(progressValue);
                progressBar.setPrefWidth(150);
                progressBar.setStyle("-fx-accent: #4CAF50;");

                Label percentageLabel = new Label(String.format("%.0f%%", progressPercentages[i]));
                percentageLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 40;");

                progressRow.getChildren().addAll(progressBar, percentageLabel);
                studentItem.getChildren().addAll(nameRow, progressRow);
                pageContent.getChildren().add(studentItem);
            }

            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setContent(pageContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(350);
            scrollPane.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5;");

            return scrollPane;
        });

        pagination.setStyle("-fx-padding: 10 0 0 0;");

        container.getChildren().addAll(sectionTitle, pagination);

        return container;
    }

    private VBox createScrollableProgressSection() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(15));
        container.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-color: #fafafa;");

        Label sectionTitle = new Label("Student Course Progress (All Students)");
        sectionTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        String[] students = {
                "Thabo Mokoena", "Mpho Letsie", "Lerato Molapo", "Palesa Motsoeneng", "Tumelo Khotha",
                "Refiloe Nteso", "Maserame Mofolo", "Ntate Mahlaha", "Mamello Seisa", "Katleho Mothae",
                "Lebohang Ramathe", "Mmathabo Moeketsi", "Tsepo Ntsoane", "Mahlomola Makhele", "Lineo Phoofolo",
                "Rethabile Mohapi", "Mponeng Mofoka", "Neo Moteane", "Mpho Mohlomi", "Naledi Tšiu"
        };

        double[] progressValues = {
                0.85, 0.62, 0.93, 0.45, 0.78, 0.91, 0.54, 0.67, 0.82, 0.39,
                0.71, 0.88, 0.76, 0.52, 0.94, 0.63, 0.49, 0.81, 0.75, 0.68
        };

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        scrollPane.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5;");

        VBox progressList = new VBox(10);
        progressList.setPadding(new Insets(10));

        double classTotal = 0;

        for (int i = 0; i < students.length; i++) {
            VBox studentProgressBox = new VBox(5);
            studentProgressBox.setStyle("-fx-padding: 10; -fx-background-color: #f9f9f9; -fx-border-color: #eee; -fx-border-radius: 5;");

            Label studentLabel = new Label((i + 1) + ". " + students[i]);
            studentLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

            HBox progressRow = new HBox(15);
            progressRow.setAlignment(Pos.CENTER_LEFT);

            ProgressBar progressBar = new ProgressBar(progressValues[i]);
            progressBar.setPrefWidth(200);
            progressBar.setStyle("-fx-accent: #2196F3;");

            ProgressIndicator progressIndicator = new ProgressIndicator(progressValues[i]);
            progressIndicator.setMaxSize(25, 25);

            Label percentageLabel = new Label(String.format("%.0f%%", progressValues[i] * 100));
            percentageLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 40; -fx-font-size: 12px;");

            progressRow.getChildren().addAll(progressBar, progressIndicator, percentageLabel);
            studentProgressBox.getChildren().addAll(studentLabel, progressRow);
            progressList.getChildren().add(studentProgressBox);

            classTotal += progressValues[i];
        }

        double classAverage = classTotal / students.length;

        Separator separator = new Separator();
        separator.setPadding(new Insets(15, 0, 10, 0));

        VBox overallProgress = new VBox(8);
        overallProgress.setStyle("-fx-padding: 15; -fx-background-color: #e8f5e9; -fx-border-color: #c8e6c9; -fx-border-radius: 8;");

        Label overallLabel = new Label("Overall Class Progress");
        overallLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        HBox overallRow = new HBox(15);
        overallRow.setAlignment(Pos.CENTER_LEFT);

        ProgressBar overallBar = new ProgressBar(classAverage);
        overallBar.setPrefWidth(200);
        overallBar.setStyle("-fx-accent: #e63946;");

        ProgressIndicator overallIndicator = new ProgressIndicator(classAverage);
        overallIndicator.setMaxSize(30, 30);

        Label overallPercentage = new Label(String.format("%.0f%%", classAverage * 100));
        overallPercentage.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        overallRow.getChildren().addAll(overallBar, overallIndicator, overallPercentage);
        overallProgress.getChildren().addAll(overallLabel, overallRow);

        progressList.getChildren().addAll(separator, overallProgress);
        scrollPane.setContent(progressList);

        container.getChildren().addAll(sectionTitle, scrollPane);

        return container;
    }

    private void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to logout?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Logout");
        confirm.setHeaderText(null);

        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            SessionManager.getInstance().logout();
            primaryStage.close();

            Platform.runLater(() -> {
                try {
                    new MainApp().start(new Stage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}