package com.example.vehicleidentificationsystem.controllers;

import com.example.vehicleidentificationsystem.MainApp;
import com.example.vehicleidentificationsystem.models.*;
import com.example.vehicleidentificationsystem.services.*;
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

    private final String BG = "#f4f6f9";
    private final String PRIMARY = "#1d3557";
    private final String SECONDARY = "#457b9d";
    private final String TEXT = "#1f2937";
    private final String MUTED = "#6b7280";

    public void show(Stage stage) {
        this.primaryStage = stage;
        this.currentUser = SessionManager.getInstance().getCurrentUser();

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG + ";");

        VBox topSection = new VBox(0);
        topSection.getChildren().addAll(createMenuBar(), createTopBar());
        root.setTop(topSection);

        mainTabPane = createTabPane();
        root.setCenter(mainTabPane);

        Scene scene = new Scene(root, 1280, 800);
        primaryStage.setTitle("Vehicle Identification System");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(768);
        primaryStage.show();

        fadeIn(root);
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");

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

    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Vehicle Identification System");
        alert.setContentText("Version 2.0\n\nA comprehensive system for managing vehicles,\nservice records, police reports, and insurance.");
        alert.showAndWait();
    }

    private void showHelpDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Help");
        alert.setHeaderText("Help");
        alert.setContentText("Modules:\n\n• Workshop Module - Manage vehicles and service records\n• Police Module - File reports and violations\n• Insurance Module - Manage insurance policies\n• Customer Portal - View vehicles and submit queries\n• User Management - Manage system users\n\nShortcuts:\n• Ctrl+R - Refresh\n• Ctrl+Q - Exit\n• F1 - Help");
        alert.showAndWait();
    }

    private HBox createTopBar() {
        HBox bar = new HBox(15);
        bar.setPadding(new Insets(12, 24, 12, 24));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 2);");

        TabPane navTabPane = new TabPane();
        navTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        navTabPane.setStyle("-fx-background-color: transparent;");
        HBox.setHgrow(navTabPane, Priority.ALWAYS);

        String role = currentUser.getRole();

        Tab homeNavTab = new Tab("Home");
        homeNavTab.setClosable(false);
        homeNavTab.setStyle("-fx-font-size: 13px;");
        homeNavTab.setOnSelectionChanged(e -> {
            if (homeNavTab.isSelected() && mainTabPane != null) {
                mainTabPane.getSelectionModel().select(0);
            }
        });
        navTabPane.getTabs().add(homeNavTab);

        if (role.equals("CUSTOMER")) {
            Tab customerNavTab = new Tab("Customer Portal");
            customerNavTab.setClosable(false);
            customerNavTab.setStyle("-fx-font-size: 13px;");
            customerNavTab.setOnSelectionChanged(e -> {
                if (customerNavTab.isSelected() && mainTabPane != null) {
                    mainTabPane.getSelectionModel().select(1);
                }
            });
            navTabPane.getTabs().add(customerNavTab);
        } else if (role.equals("ADMIN")) {
            String[] modules = {"Workshop", "Police", "Insurance", "Customer", "Queries", "Users"};
            for (int i = 0; i < modules.length; i++) {
                final int index = i + 1;
                Tab moduleTab = new Tab(modules[i]);
                moduleTab.setClosable(false);
                moduleTab.setStyle("-fx-font-size: 13px;");
                moduleTab.setOnSelectionChanged(e -> {
                    if (moduleTab.isSelected() && mainTabPane != null) {
                        mainTabPane.getSelectionModel().select(index);
                    }
                });
                navTabPane.getTabs().add(moduleTab);
            }
        } else if (role.equals("WORKSHOP")) {
            Tab workshopNavTab = new Tab("Workshop");
            workshopNavTab.setClosable(false);
            workshopNavTab.setStyle("-fx-font-size: 13px");
            workshopNavTab.setOnSelectionChanged(e -> {
                if (workshopNavTab.isSelected() && mainTabPane != null) {
                    mainTabPane.getSelectionModel().select(1);
                }
            });
            navTabPane.getTabs().add(workshopNavTab);
        } else if (role.equals("POLICE")) {
            Tab policeNavTab = new Tab("Police");
            policeNavTab.setClosable(false);
            policeNavTab.setStyle("-fx-font-size: 13px;");
            policeNavTab.setOnSelectionChanged(e -> {
                if (policeNavTab.isSelected() && mainTabPane != null) {
                    mainTabPane.getSelectionModel().select(1);
                }
            });
            navTabPane.getTabs().add(policeNavTab);
        } else if (role.equals("INSURANCE")) {
            Tab insuranceNavTab = new Tab("Insurance");
            insuranceNavTab.setClosable(false);
            insuranceNavTab.setStyle("-fx-font-size: 13px;");
            insuranceNavTab.setOnSelectionChanged(e -> {
                if (insuranceNavTab.isSelected() && mainTabPane != null) {
                    mainTabPane.getSelectionModel().select(1);
                }
            });
            navTabPane.getTabs().add(insuranceNavTab);
        }

        HBox userBox = new HBox(15);
        userBox.setAlignment(Pos.CENTER_RIGHT);

        VBox userInfo = new VBox(2);
        userInfo.setAlignment(Pos.CENTER_RIGHT);

        Label userName = new Label(currentUser.getFullName());
        userName.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label userRole = new Label(currentUser.getRoleDisplayName());
        userRole.setStyle("-fx-font-size: 11px; -fx-text-fill: " + MUTED + ";");

        userInfo.getChildren().addAll(userName, userRole);

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle(buttonStyle());
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle(buttonHover()));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setStyle(buttonStyle()));
        logoutBtn.setOnAction(e -> handleLogout());

        userBox.getChildren().addAll(userInfo, logoutBtn);

        bar.getChildren().addAll(navTabPane, userBox);
        HBox.setHgrow(navTabPane, Priority.ALWAYS);

        return bar;
    }

    private TabPane createTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setTabMinHeight(40);
        tabPane.setStyle("-fx-tab-max-height: 0; -fx-tab-min-height: 0; -fx-tab-max-width: 0; -fx-background-color: " + BG + ";");

        String role = currentUser.getRole();

        Tab homeTab = new Tab("Home");
        homeTab.setClosable(false);
        homeTab.setContent(createHomeContent());
        tabPane.getTabs().add(homeTab);

        if (role.equals("CUSTOMER")) {
            Tab customerTab = new Tab("Customer Portal");
            customerTab.setClosable(false);
            CustomerController customerController = new CustomerController();
            customerTab.setContent(customerController.createView(currentUser));
            tabPane.getTabs().add(customerTab);
        } else if (role.equals("ADMIN")) {
            Tab workshopTab = new Tab("Workshop");
            workshopTab.setClosable(false);
            workshopTab.setContent(createWorkshopModuleContent());
            tabPane.getTabs().add(workshopTab);

            Tab policeTab = new Tab("Police");
            policeTab.setClosable(false);
            PoliceController policeController = new PoliceController();
            policeTab.setContent(policeController.createView(currentUser));
            tabPane.getTabs().add(policeTab);

            Tab insuranceTab = new Tab("Insurance");
            insuranceTab.setClosable(false);
            InsuranceController insuranceController = new InsuranceController();
            insuranceTab.setContent(insuranceController.createView(currentUser));
            tabPane.getTabs().add(insuranceTab);

            Tab customerTab = new Tab("Customer");
            customerTab.setClosable(false);
            CustomerController customerController = new CustomerController();
            customerTab.setContent(customerController.createView(currentUser));
            tabPane.getTabs().add(customerTab);

            Tab queriesTab = new Tab("Queries");
            queriesTab.setClosable(false);
            QueryManagementController queryManagementController = new QueryManagementController();
            queriesTab.setContent(queryManagementController.createView(currentUser));
            tabPane.getTabs().add(queriesTab);

            Tab adminTab = new Tab("Users");
            adminTab.setClosable(false);
            AdminController adminController = new AdminController();
            adminTab.setContent(adminController.createView(currentUser));
            tabPane.getTabs().add(adminTab);
        } else if (role.equals("WORKSHOP")) {
            Tab workshopTab = new Tab("Workshop");
            workshopTab.setClosable(false);
            workshopTab.setContent(createWorkshopModuleContent());
            tabPane.getTabs().add(workshopTab);
        } else if (role.equals("POLICE")) {
            Tab policeTab = new Tab("Police");
            policeTab.setClosable(false);
            PoliceController policeController = new PoliceController();
            policeTab.setContent(policeController.createView(currentUser));
            tabPane.getTabs().add(policeTab);
        } else if (role.equals("INSURANCE")) {
            Tab insuranceTab = new Tab("Insurance");
            insuranceTab.setClosable(false);
            InsuranceController insuranceController = new InsuranceController();
            insuranceTab.setContent(insuranceController.createView(currentUser));
            tabPane.getTabs().add(insuranceTab);
        }

        return tabPane;
    }

    private VBox createWorkshopModuleContent() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");

        TabPane workshopTabPane = new TabPane();
        workshopTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        workshopTabPane.setStyle("-fx-background-color: transparent;");

        Tab vehiclesInnerTab = new Tab("Vehicle Management");
        vehiclesInnerTab.setClosable(false);
        VehicleController vehicleController = new VehicleController();
        vehiclesInnerTab.setContent(vehicleController.createView(currentUser));
        workshopTabPane.getTabs().add(vehiclesInnerTab);

        Tab serviceInnerTab = new Tab("Service Records");
        serviceInnerTab.setClosable(false);
        WorkshopController workshopController = new WorkshopController();
        serviceInnerTab.setContent(workshopController.createView(currentUser));
        workshopTabPane.getTabs().add(serviceInnerTab);

        container.getChildren().add(workshopTabPane);
        VBox.setVgrow(workshopTabPane, Priority.ALWAYS);

        return container;
    }

    private ScrollPane createHomeContent() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setPadding(new Insets(0));
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        VBox mainContent = new VBox(24);
        mainContent.setPadding(new Insets(24));

        // Just the stats grid and student section - NO welcome card
        GridPane statsGrid = createStatsGrid();
        HBox splitSection = createSplitStudentSection();

        mainContent.getChildren().addAll(statsGrid, splitSection);
        scrollPane.setContent(mainContent);

        return scrollPane;
    }

    private GridPane createStatsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(10, 0, 20, 0));

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

        VBox card1 = createStatCard("Total Vehicles", String.valueOf(totalVehicles));
        grid.add(card1, 0, 0);

        VBox card2 = createStatCard("Active Insurance", String.valueOf(activeInsurance));
        grid.add(card2, 1, 0);

        VBox card3 = createStatCard("Police Reports", String.valueOf(policeReports));
        grid.add(card3, 2, 0);

        VBox card4 = createStatCard("Service Records", String.valueOf(serviceRecords));
        grid.add(card4, 3, 0);

        return grid;
    }

    private VBox createStatCard(String title, String value) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + MUTED + "; -fx-font-weight: 500;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 34px; -fx-font-weight: bold; -fx-text-fill: " + TEXT + ";");

        card.getChildren().addAll(titleLabel, valueLabel);
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
            return (int) insuranceList.stream().filter(i -> "Active".equals(i.getStatus())).count();
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
        splitPane.setPadding(new Insets(20, 0, 10, 0));
        splitPane.setAlignment(Pos.TOP_CENTER);

        VBox leftSection = createPaginationSection();
        VBox rightSection = createScrollableProgressSection();

        leftSection.setPrefWidth(550);
        rightSection.setPrefWidth(550);
        leftSection.setMaxWidth(550);
        rightSection.setMaxWidth(550);
        leftSection.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");
        rightSection.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");

        splitPane.getChildren().addAll(leftSection, rightSection);
        return splitPane;
    }

    private VBox createPaginationSection() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(20));

        Label sectionTitle = new Label("Student Records");
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + TEXT + ";");

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
        pagination.setStyle("-fx-padding: 10;");
        pagination.setPageFactory(pageIndex -> {
            VBox pageContent = new VBox(8);
            pageContent.setPadding(new Insets(10));

            int startIndex = pageIndex * studentsPerPage;
            int endIndex = Math.min(startIndex + studentsPerPage, students.length);

            Label pageHeader = new Label("Page " + (pageIndex + 1) + " (Students " + (startIndex + 1) + " to " + endIndex + ")");
            pageHeader.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + MUTED + ";");
            pageContent.getChildren().add(pageHeader);

            for (int i = startIndex; i < endIndex; i++) {
                HBox studentRow = new HBox(10);
                studentRow.setPadding(new Insets(8, 12, 8, 12));
                studentRow.setStyle("-fx-background-color: #f9fafb; -fx-border-color: #e5e7eb; -fx-border-radius: 6;");
                studentRow.setAlignment(Pos.CENTER_LEFT);

                Label numberLabel = new Label(String.format("%02d.", (i + 1)));
                numberLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 35; -fx-text-fill: #4b5563;");

                Label nameLabel = new Label(students[i]);
                nameLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151;");

                double progressValue = progressPercentages[i] / 100.0;
                ProgressBar progressBar = new ProgressBar(progressValue);
                progressBar.setPrefWidth(120);
                progressBar.setStyle("-fx-accent: #10b981;");

                Label percentageLabel = new Label(String.format("%.0f%%", progressPercentages[i]));
                percentageLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 45; -fx-font-size: 12px; -fx-text-fill: #059669;");

                studentRow.getChildren().addAll(numberLabel, nameLabel, progressBar, percentageLabel);
                pageContent.getChildren().add(studentRow);
            }

            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setContent(pageContent);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(320);
            scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

            return scrollPane;
        });

        container.getChildren().addAll(sectionTitle, pagination);
        return container;
    }

    private VBox createScrollableProgressSection() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(20));

        Label sectionTitle = new Label("Student Progress");
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + TEXT + ";");

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
        scrollPane.setPrefHeight(420);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        VBox progressList = new VBox(8);
        progressList.setPadding(new Insets(5));

        double classTotal = 0;

        for (int i = 0; i < students.length; i++) {
            VBox studentProgressBox = new VBox(5);
            studentProgressBox.setPadding(new Insets(10, 12, 10, 12));
            studentProgressBox.setStyle("-fx-background-color: #f9fafb; -fx-border-color: #e5e7eb; -fx-border-radius: 6;");

            Label studentLabel = new Label((i + 1) + ". " + students[i]);
            studentLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #374151;");

            HBox progressRow = new HBox(12);
            progressRow.setAlignment(Pos.CENTER_LEFT);

            ProgressBar progressBar = new ProgressBar(progressValues[i]);
            progressBar.setPrefWidth(180);
            progressBar.setStyle("-fx-accent: #3b82f6;");

            ProgressIndicator progressIndicator = new ProgressIndicator(progressValues[i]);
            progressIndicator.setMaxSize(25, 25);

            Label percentageLabel = new Label(String.format("%.0f%%", progressValues[i] * 100));
            percentageLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 45; -fx-font-size: 12px; -fx-text-fill: #2563eb;");

            progressRow.getChildren().addAll(progressBar, progressIndicator, percentageLabel);
            studentProgressBox.getChildren().addAll(studentLabel, progressRow);
            progressList.getChildren().add(studentProgressBox);

            classTotal += progressValues[i];
        }

        double classAverage = classTotal / students.length;

        Separator separator = new Separator();
        separator.setPadding(new Insets(15, 0, 10, 0));

        VBox overallProgress = new VBox(8);
        overallProgress.setPadding(new Insets(15));
        overallProgress.setStyle("-fx-background-color: #f0fdf4; -fx-border-color: #dcfce7; -fx-border-radius: 8;");

        Label overallLabel = new Label("Overall Class Progress");
        overallLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #166534;");

        HBox overallRow = new HBox(12);
        overallRow.setAlignment(Pos.CENTER_LEFT);

        ProgressBar overallBar = new ProgressBar(classAverage);
        overallBar.setPrefWidth(180);
        overallBar.setStyle("-fx-accent: #ef4444;");

        ProgressIndicator overallIndicator = new ProgressIndicator(classAverage);
        overallIndicator.setMaxSize(30, 30);

        Label overallPercentage = new Label(String.format("%.0f%%", classAverage * 100));
        overallPercentage.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #dc2626;");

        overallRow.getChildren().addAll(overallBar, overallIndicator, overallPercentage);
        overallProgress.getChildren().addAll(overallLabel, overallRow);

        progressList.getChildren().addAll(separator, overallProgress);
        scrollPane.setContent(progressList);

        container.getChildren().addAll(sectionTitle, scrollPane);
        return container;
    }

    private void refreshDashboard() {
        mainTabPane.getTabs().clear();
        mainTabPane = createTabPane();
        BorderPane root = (BorderPane) primaryStage.getScene().getRoot();
        root.setCenter(mainTabPane);
        AlertUtils.showInfo("Refreshed", "Dashboard has been refreshed.");
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

    private String buttonStyle() {
        return "-fx-background-color: " + PRIMARY + "; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 6 14; -fx-cursor: hand;";
    }

    private String buttonHover() {
        return "-fx-background-color: " + SECONDARY + "; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 6 14; -fx-cursor: hand;";
    }

    private void fadeIn(Pane p) {
        FadeTransition ft = new FadeTransition(Duration.millis(400), p);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }
}