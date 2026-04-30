package com.example.vehicleidentificationsystem.controllers;

import com.example.vehicleidentificationsystem.models.*;
import com.example.vehicleidentificationsystem.services.PoliceService;
import com.example.vehicleidentificationsystem.services.VehicleService;
import com.example.vehicleidentificationsystem.utils.AlertUtils;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PoliceController {

    private TableView<PoliceReport> reportTable;
    private TableView<Violation> violationTable;
    private TabPane tabPane;
    private User currentUser;

    // For ADMIN - search vehicle fields
    private TextField txtSearchVehicleReport;
    private ComboBox<Vehicle> vehicleReportSearchCombo;
    private Label selectedVehicleReportLabel;

    private TextField txtSearchVehicleViolation;
    private ComboBox<Vehicle> vehicleViolationSearchCombo;
    private Label selectedVehicleViolationLabel;

    // For ADMIN - search officer fields
    private TextField txtSearchOfficerReport;
    private ComboBox<User> officerReportSearchCombo;
    private Label selectedOfficerReportLabel;

    private TextField txtSearchOfficerViolation;
    private ComboBox<User> officerViolationSearchCombo;
    private Label selectedOfficerViolationLabel;

    // For regular users - simple combos
    private ComboBox<Vehicle> vehicleReportCombo;
    private ComboBox<Vehicle> vehicleViolationCombo;

    private ComboBox<String> reportTypeCombo;
    private TextArea txtReportDesc;

    private ComboBox<String> violationTypeCombo;
    private TextField txtFineAmount;

    public VBox createView(User user) {
        this.currentUser = user;

        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-font-size: 13px;");

        Tab reportsTab = new Tab("Police Reports");
        reportsTab.setContent(createReportsTab());
        reportsTab.setClosable(false);

        Tab violationsTab = new Tab("Violations");
        violationsTab.setContent(createViolationsTab());
        violationsTab.setClosable(false);

        tabPane.getTabs().addAll(reportsTab, violationsTab);

        return new VBox(tabPane);
    }

    private VBox createReportsTab() {
        // Main container with 40/60 split
        HBox mainContainer = new HBox(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setAlignment(Pos.TOP_CENTER);

        // LEFT SIDE (40%) - Form
        VBox leftPanel = createReportLeftPanel();

        // RIGHT SIDE (60%) - Table
        VBox rightPanel = createReportRightPanel();

        leftPanel.setPrefWidth(400);
        rightPanel.setPrefWidth(600);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        mainContainer.getChildren().addAll(leftPanel, rightPanel);

        loadReports();

        return new VBox(mainContainer);
    }

    private VBox createReportLeftPanel() {
        VBox leftPanel = new VBox(15);
        leftPanel.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");
        leftPanel.setPadding(new Insets(20));

        Label formTitle = new Label("Add New Police Report");
        formTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

        VBox formPanel = createReportFormPanel();

        leftPanel.getChildren().addAll(formTitle, formPanel);

        return leftPanel;
    }

    private VBox createReportRightPanel() {
        VBox rightPanel = new VBox(10);
        rightPanel.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");
        rightPanel.setPadding(new Insets(15));

        Label tableTitle = new Label("Police Reports");
        tableTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

        reportTable = new TableView<>();
        reportTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        reportTable.setPlaceholder(new Label("No police reports found"));
        createReportColumns();

        VBox.setVgrow(reportTable, Priority.ALWAYS);

        rightPanel.getChildren().addAll(tableTitle, reportTable);

        return rightPanel;
    }

    private VBox createReportFormPanel() {
        VBox formPanel = new VBox(12);

        GridPane formGrid = new GridPane();
        formGrid.setHgap(12);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(10, 0, 0, 0));

        int row = 0;

        if (currentUser.getRole().equals("ADMIN")) {
            // Vehicle Search Section
            formGrid.add(new Label("Vehicle:"), 0, row);
            HBox vehicleSearchBox = new HBox(8);
            txtSearchVehicleReport = new TextField();
            txtSearchVehicleReport.setPromptText("Enter registration number");
            txtSearchVehicleReport.setPrefWidth(180);
            Button btnSearchVehicle = new Button("Search");
            btnSearchVehicle.setStyle("-fx-background-color: #1d3557; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 6;");
            btnSearchVehicle.setOnAction(e -> searchVehicleForReport());
            vehicleSearchBox.getChildren().addAll(txtSearchVehicleReport, btnSearchVehicle);
            formGrid.add(vehicleSearchBox, 1, row);
            row++;

            formGrid.add(new Label("Select:"), 0, row);
            vehicleReportSearchCombo = new ComboBox<>();
            vehicleReportSearchCombo.setPromptText("Select from results");
            vehicleReportSearchCombo.setPrefWidth(250);
            vehicleReportSearchCombo.setVisible(false);
            vehicleReportSearchCombo.setOnAction(e -> {
                if (vehicleReportSearchCombo.getValue() != null) {
                    selectedVehicleReportLabel.setText("Selected: " + vehicleReportSearchCombo.getValue().getRegistrationNumber());
                }
            });
            formGrid.add(vehicleReportSearchCombo, 1, row);
            row++;

            selectedVehicleReportLabel = new Label("No vehicle selected");
            selectedVehicleReportLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");
            formGrid.add(selectedVehicleReportLabel, 1, row);
            row++;

            // Officer Search Section
            formGrid.add(new Label("Officer:"), 0, row);
            HBox officerSearchBox = new HBox(8);
            txtSearchOfficerReport = new TextField();
            txtSearchOfficerReport.setPromptText("Enter officer name");
            txtSearchOfficerReport.setPrefWidth(180);
            Button btnSearchOfficer = new Button("Search");
            btnSearchOfficer.setStyle("-fx-background-color: #1d3557; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 6;");
            btnSearchOfficer.setOnAction(e -> searchOfficerForReport());
            officerSearchBox.getChildren().addAll(txtSearchOfficerReport, btnSearchOfficer);
            formGrid.add(officerSearchBox, 1, row);
            row++;

            formGrid.add(new Label("Select:"), 0, row);
            officerReportSearchCombo = new ComboBox<>();
            officerReportSearchCombo.setPromptText("Select from results");
            officerReportSearchCombo.setPrefWidth(250);
            officerReportSearchCombo.setVisible(false);
            officerReportSearchCombo.setOnAction(e -> {
                if (officerReportSearchCombo.getValue() != null) {
                    selectedOfficerReportLabel.setText("Selected: " + officerReportSearchCombo.getValue().getFullName());
                }
            });
            formGrid.add(officerReportSearchCombo, 1, row);
            row++;

            selectedOfficerReportLabel = new Label("No officer selected");
            selectedOfficerReportLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");
            formGrid.add(selectedOfficerReportLabel, 1, row);
            row++;
        } else {
            // For regular POLICE user
            formGrid.add(new Label("Vehicle:"), 0, row);
            vehicleReportCombo = new ComboBox<>();
            vehicleReportCombo.setPromptText("Select vehicle");
            vehicleReportCombo.setPrefWidth(250);
            formGrid.add(vehicleReportCombo, 1, row);
            row++;
        }

        // Separator
        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));
        formGrid.add(separator, 0, row, 2, 1);
        row++;

        // Report Type
        formGrid.add(new Label("Report Type:"), 0, row);
        reportTypeCombo = new ComboBox<>();
        reportTypeCombo.getItems().addAll("Accident", "Theft", "Stolen Vehicle");
        reportTypeCombo.setPrefWidth(200);
        formGrid.add(reportTypeCombo, 1, row);
        row++;

        // Description
        formGrid.add(new Label("Description:"), 0, row);
        txtReportDesc = new TextArea();
        txtReportDesc.setPromptText("Enter incident details...");
        txtReportDesc.setPrefRowCount(3);
        txtReportDesc.setPrefWidth(280);
        txtReportDesc.setWrapText(true);
        formGrid.add(txtReportDesc, 1, row);
        row++;

        // Submit Button
        Button btnSubmit = new Button("Add Police Report");
        btnSubmit.setStyle("-fx-background-color: #1d3557; -fx-text-fill: white; -fx-font-weight: 500; -fx-padding: 10 20; -fx-background-radius: 6; -fx-cursor: hand;");
        btnSubmit.setOnMouseEntered(e -> btnSubmit.setStyle("-fx-background-color: #457b9d; -fx-text-fill: white; -fx-font-weight: 500; -fx-padding: 10 20; -fx-background-radius: 6; -fx-cursor: hand;"));
        btnSubmit.setOnMouseExited(e -> btnSubmit.setStyle("-fx-background-color: #1d3557; -fx-text-fill: white; -fx-font-weight: 500; -fx-padding: 10 20; -fx-background-radius: 6; -fx-cursor: hand;"));
        btnSubmit.setOnAction(e -> handleAddReport());

        HBox buttonBox = new HBox(btnSubmit);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 5, 0));
        formGrid.add(buttonBox, 0, row, 2, 1);

        formPanel.getChildren().add(formGrid);
        return formPanel;
    }

    private VBox createViolationsTab() {
        // Main container with 40/60 split
        HBox mainContainer = new HBox(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setAlignment(Pos.TOP_CENTER);

        // LEFT SIDE (40%) - Form
        VBox leftPanel = createViolationLeftPanel();

        // RIGHT SIDE (60%) - Table
        VBox rightPanel = createViolationRightPanel();

        leftPanel.setPrefWidth(400);
        rightPanel.setPrefWidth(600);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        mainContainer.getChildren().addAll(leftPanel, rightPanel);

        loadViolations();

        return new VBox(mainContainer);
    }

    private VBox createViolationLeftPanel() {
        VBox leftPanel = new VBox(15);
        leftPanel.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");
        leftPanel.setPadding(new Insets(20));

        Label formTitle = new Label("Add New Violation");
        formTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

        VBox formPanel = createViolationFormPanel();

        leftPanel.getChildren().addAll(formTitle, formPanel);

        return leftPanel;
    }

    private VBox createViolationRightPanel() {
        VBox rightPanel = new VBox(10);
        rightPanel.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");
        rightPanel.setPadding(new Insets(15));

        Label tableTitle = new Label("Violations");
        tableTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

        violationTable = new TableView<>();
        violationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        violationTable.setPlaceholder(new Label("No violations found"));
        createViolationColumns();

        VBox.setVgrow(violationTable, Priority.ALWAYS);

        rightPanel.getChildren().addAll(tableTitle, violationTable);

        return rightPanel;
    }

    private VBox createViolationFormPanel() {
        VBox formPanel = new VBox(12);

        GridPane formGrid = new GridPane();
        formGrid.setHgap(12);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(10, 0, 0, 0));

        int row = 0;

        if (currentUser.getRole().equals("ADMIN")) {
            // Vehicle Search Section
            formGrid.add(new Label("Vehicle:"), 0, row);
            HBox vehicleSearchBox = new HBox(8);
            txtSearchVehicleViolation = new TextField();
            txtSearchVehicleViolation.setPromptText("Enter registration number");
            txtSearchVehicleViolation.setPrefWidth(180);
            Button btnSearchVehicle = new Button("Search");
            btnSearchVehicle.setStyle("-fx-background-color: #1d3557; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 6;");
            btnSearchVehicle.setOnAction(e -> searchVehicleForViolation());
            vehicleSearchBox.getChildren().addAll(txtSearchVehicleViolation, btnSearchVehicle);
            formGrid.add(vehicleSearchBox, 1, row);
            row++;

            formGrid.add(new Label("Select:"), 0, row);
            vehicleViolationSearchCombo = new ComboBox<>();
            vehicleViolationSearchCombo.setPromptText("Select from results");
            vehicleViolationSearchCombo.setPrefWidth(250);
            vehicleViolationSearchCombo.setVisible(false);
            vehicleViolationSearchCombo.setOnAction(e -> {
                if (vehicleViolationSearchCombo.getValue() != null) {
                    selectedVehicleViolationLabel.setText("Selected: " + vehicleViolationSearchCombo.getValue().getRegistrationNumber());
                }
            });
            formGrid.add(vehicleViolationSearchCombo, 1, row);
            row++;

            selectedVehicleViolationLabel = new Label("No vehicle selected");
            selectedVehicleViolationLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");
            formGrid.add(selectedVehicleViolationLabel, 1, row);
            row++;

            // Officer Search Section
            formGrid.add(new Label("Officer:"), 0, row);
            HBox officerSearchBox = new HBox(8);
            txtSearchOfficerViolation = new TextField();
            txtSearchOfficerViolation.setPromptText("Enter officer name");
            txtSearchOfficerViolation.setPrefWidth(180);
            Button btnSearchOfficer = new Button("Search");
            btnSearchOfficer.setStyle("-fx-background-color: #1d3557; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 6;");
            btnSearchOfficer.setOnAction(e -> searchOfficerForViolation());
            officerSearchBox.getChildren().addAll(txtSearchOfficerViolation, btnSearchOfficer);
            formGrid.add(officerSearchBox, 1, row);
            row++;

            formGrid.add(new Label("Select:"), 0, row);
            officerViolationSearchCombo = new ComboBox<>();
            officerViolationSearchCombo.setPromptText("Select from results");
            officerViolationSearchCombo.setPrefWidth(250);
            officerViolationSearchCombo.setVisible(false);
            officerViolationSearchCombo.setOnAction(e -> {
                if (officerViolationSearchCombo.getValue() != null) {
                    selectedOfficerViolationLabel.setText("Selected: " + officerViolationSearchCombo.getValue().getFullName());
                }
            });
            formGrid.add(officerViolationSearchCombo, 1, row);
            row++;

            selectedOfficerViolationLabel = new Label("No officer selected");
            selectedOfficerViolationLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");
            formGrid.add(selectedOfficerViolationLabel, 1, row);
            row++;
        } else {
            // For regular POLICE user
            formGrid.add(new Label("Vehicle:"), 0, row);
            vehicleViolationCombo = new ComboBox<>();
            vehicleViolationCombo.setPromptText("Select vehicle");
            vehicleViolationCombo.setPrefWidth(250);
            formGrid.add(vehicleViolationCombo, 1, row);
            row++;
        }

        // Separator
        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));
        formGrid.add(separator, 0, row, 2, 1);
        row++;

        // Violation Type
        formGrid.add(new Label("Violation Type:"), 0, row);
        violationTypeCombo = new ComboBox<>();
        violationTypeCombo.getItems().addAll("Speeding", "No seatbelt", "Running red light", "Drunk driving", "No license", "Overloading");
        violationTypeCombo.setPrefWidth(200);
        formGrid.add(violationTypeCombo, 1, row);
        row++;

        // Fine Amount
        formGrid.add(new Label("Fine Amount (M):"), 0, row);
        txtFineAmount = new TextField();
        txtFineAmount.setPromptText("Enter amount");
        txtFineAmount.setPrefWidth(150);
        formGrid.add(txtFineAmount, 1, row);
        row++;

        // Submit Button
        Button btnSubmit = new Button("Add Violation");
        btnSubmit.setStyle("-fx-background-color: #1d3557; -fx-text-fill: white; -fx-font-weight: 500; -fx-padding: 10 20; -fx-background-radius: 6; -fx-cursor: hand;");
        btnSubmit.setOnMouseEntered(e -> btnSubmit.setStyle("-fx-background-color: #457b9d; -fx-text-fill: white; -fx-font-weight: 500; -fx-padding: 10 20; -fx-background-radius: 6; -fx-cursor: hand;"));
        btnSubmit.setOnMouseExited(e -> btnSubmit.setStyle("-fx-background-color: #1d3557; -fx-text-fill: white; -fx-font-weight: 500; -fx-padding: 10 20; -fx-background-radius: 6; -fx-cursor: hand;"));
        btnSubmit.setOnAction(e -> handleAddViolation());

        HBox buttonBox = new HBox(btnSubmit);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 5, 0));
        formGrid.add(buttonBox, 0, row, 2, 1);

        formPanel.getChildren().add(formGrid);
        return formPanel;
    }

    private void createReportColumns() {
        TableColumn<PoliceReport, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("reportId"));
        idCol.setPrefWidth(50);

        TableColumn<PoliceReport, String> vehicleCol = new TableColumn<>("Vehicle");
        vehicleCol.setCellValueFactory(new PropertyValueFactory<>("vehicleReg"));
        vehicleCol.setPrefWidth(120);

        TableColumn<PoliceReport, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("reportDate"));
        dateCol.setPrefWidth(100);

        TableColumn<PoliceReport, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("reportType"));
        typeCol.setPrefWidth(100);

        TableColumn<PoliceReport, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(250);

        TableColumn<PoliceReport, String> officerCol = new TableColumn<>("Officer");
        officerCol.setCellValueFactory(new PropertyValueFactory<>("officerName"));
        officerCol.setPrefWidth(150);

        reportTable.getColumns().addAll(idCol, vehicleCol, dateCol, typeCol, descCol, officerCol);
    }

    private void createViolationColumns() {
        TableColumn<Violation, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("violationId"));
        idCol.setPrefWidth(50);

        TableColumn<Violation, String> vehicleCol = new TableColumn<>("Vehicle");
        vehicleCol.setCellValueFactory(new PropertyValueFactory<>("vehicleReg"));
        vehicleCol.setPrefWidth(120);

        TableColumn<Violation, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("violationDate"));
        dateCol.setPrefWidth(100);

        TableColumn<Violation, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("violationType"));
        typeCol.setPrefWidth(120);

        TableColumn<Violation, Double> fineCol = new TableColumn<>("Fine Amount");
        fineCol.setCellValueFactory(new PropertyValueFactory<>("fineAmount"));
        fineCol.setPrefWidth(100);

        TableColumn<Violation, String> officerCol = new TableColumn<>("Officer");
        officerCol.setCellValueFactory(new PropertyValueFactory<>("officerName"));
        officerCol.setPrefWidth(150);

        violationTable.getColumns().addAll(idCol, vehicleCol, dateCol, typeCol, fineCol, officerCol);
    }

    private void searchVehicleForReport() {
        String searchText = txtSearchVehicleReport.getText().trim();
        if (searchText.isEmpty()) {
            AlertUtils.showWarning("Missing Data", "Please enter registration number to search.");
            return;
        }

        List<Vehicle> vehicles = new ArrayList<>();
        String sql = "SELECT v.vehicle_id, v.registration_number, v.make, v.model, v.year, v.owner_id, c.name as owner_name FROM vehicle v LEFT JOIN customer c ON v.owner_id = c.customer_id WHERE v.registration_number ILIKE ?";

        try (PreparedStatement pstmt = currentUser.getDbConnection().prepareStatement(sql)) {
            String pattern = "%" + searchText + "%";
            pstmt.setString(1, pattern);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Vehicle vehicle = new Vehicle(
                        rs.getInt("vehicle_id"),
                        rs.getString("registration_number"),
                        rs.getString("make"),
                        rs.getString("model"),
                        rs.getInt("year"),
                        rs.getInt("owner_id"),
                        rs.getString("owner_name")
                );
                vehicles.add(vehicle);
            }
        } catch (SQLException e) {
            AlertUtils.showError("Database Error", "Failed to search vehicles: " + e.getMessage());
        }

        if (vehicles.isEmpty()) {
            AlertUtils.showWarning("No Results", "No vehicles found matching: " + searchText);
            vehicleReportSearchCombo.setVisible(false);
        } else {
            vehicleReportSearchCombo.setItems(FXCollections.observableArrayList(vehicles));
            vehicleReportSearchCombo.setCellFactory(lv -> new ListCell<Vehicle>() {
                @Override
                protected void updateItem(Vehicle item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getRegistrationNumber() + " - " + item.getMake() + " " + item.getModel());
                }
            });
            vehicleReportSearchCombo.setVisible(true);
            vehicleReportSearchCombo.setPromptText("Select vehicle (" + vehicles.size() + " found)");
        }
    }

    private void searchOfficerForReport() {
        String searchText = txtSearchOfficerReport.getText().trim();
        if (searchText.isEmpty()) {
            AlertUtils.showWarning("Missing Data", "Please enter officer name to search.");
            return;
        }

        List<User> officers = new ArrayList<>();
        String sql = "SELECT user_id, username, full_name FROM app_user WHERE user_type = 'POLICE' AND (username LIKE ? OR full_name LIKE ?)";

        try (PreparedStatement pstmt = currentUser.getDbConnection().prepareStatement(sql)) {
            String pattern = "%" + searchText + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                User officer = new PoliceUser(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("full_name"),
                        currentUser.getDbConnection()
                );
                officers.add(officer);
            }
        } catch (SQLException e) {
            AlertUtils.showError("Database Error", "Failed to search officers: " + e.getMessage());
        }

        if (officers.isEmpty()) {
            AlertUtils.showWarning("No Results", "No police officers found matching: " + searchText);
            officerReportSearchCombo.setVisible(false);
        } else {
            officerReportSearchCombo.setItems(FXCollections.observableArrayList(officers));
            officerReportSearchCombo.setCellFactory(lv -> new ListCell<User>() {
                @Override
                protected void updateItem(User item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getFullName() + " (" + item.getUsername() + ")");
                }
            });
            officerReportSearchCombo.setVisible(true);
            officerReportSearchCombo.setPromptText("Select officer (" + officers.size() + " found)");
        }
    }

    private void searchVehicleForViolation() {
        String searchText = txtSearchVehicleViolation.getText().trim();
        if (searchText.isEmpty()) {
            AlertUtils.showWarning("Missing Data", "Please enter registration number to search.");
            return;
        }

        List<Vehicle> vehicles = new ArrayList<>();
        String sql = "SELECT v.vehicle_id, v.registration_number, v.make, v.model, v.year, v.owner_id, c.name as owner_name FROM vehicle v LEFT JOIN customer c ON v.owner_id = c.customer_id WHERE v.registration_number ILIKE ?";

        try (PreparedStatement pstmt = currentUser.getDbConnection().prepareStatement(sql)) {
            String pattern = "%" + searchText + "%";
            pstmt.setString(1, pattern);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Vehicle vehicle = new Vehicle(
                        rs.getInt("vehicle_id"),
                        rs.getString("registration_number"),
                        rs.getString("make"),
                        rs.getString("model"),
                        rs.getInt("year"),
                        rs.getInt("owner_id"),
                        rs.getString("owner_name")
                );
                vehicles.add(vehicle);
            }
        } catch (SQLException e) {
            AlertUtils.showError("Database Error", "Failed to search vehicles: " + e.getMessage());
        }

        if (vehicles.isEmpty()) {
            AlertUtils.showWarning("No Results", "No vehicles found matching: " + searchText);
            vehicleViolationSearchCombo.setVisible(false);
        } else {
            vehicleViolationSearchCombo.setItems(FXCollections.observableArrayList(vehicles));
            vehicleViolationSearchCombo.setCellFactory(lv -> new ListCell<Vehicle>() {
                @Override
                protected void updateItem(Vehicle item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getRegistrationNumber() + " - " + item.getMake() + " " + item.getModel());
                }
            });
            vehicleViolationSearchCombo.setVisible(true);
            vehicleViolationSearchCombo.setPromptText("Select vehicle (" + vehicles.size() + " found)");
        }
    }

    private void searchOfficerForViolation() {
        String searchText = txtSearchOfficerViolation.getText().trim();
        if (searchText.isEmpty()) {
            AlertUtils.showWarning("Missing Data", "Please enter officer name to search.");
            return;
        }

        List<User> officers = new ArrayList<>();
        String sql = "SELECT user_id, username, full_name FROM app_user WHERE user_type = 'POLICE' AND (username LIKE ? OR full_name LIKE ?)";

        try (PreparedStatement pstmt = currentUser.getDbConnection().prepareStatement(sql)) {
            String pattern = "%" + searchText + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                User officer = new PoliceUser(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("full_name"),
                        currentUser.getDbConnection()
                );
                officers.add(officer);
            }
        } catch (SQLException e) {
            AlertUtils.showError("Database Error", "Failed to search officers: " + e.getMessage());
        }

        if (officers.isEmpty()) {
            AlertUtils.showWarning("No Results", "No police officers found matching: " + searchText);
            officerViolationSearchCombo.setVisible(false);
        } else {
            officerViolationSearchCombo.setItems(FXCollections.observableArrayList(officers));
            officerViolationSearchCombo.setCellFactory(lv -> new ListCell<User>() {
                @Override
                protected void updateItem(User item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getFullName() + " (" + item.getUsername() + ")");
                }
            });
            officerViolationSearchCombo.setVisible(true);
            officerViolationSearchCombo.setPromptText("Select officer (" + officers.size() + " found)");
        }
    }

    private void loadReports() {
        List<PoliceReport> reports;

        if (currentUser.getRole().equals("CUSTOMER")) {
            reports = getCustomerPoliceReports();
        } else {
            reports = PoliceService.getAllPoliceReports(currentUser);
        }

        reportTable.setItems(FXCollections.observableArrayList(reports));

        if (!currentUser.getRole().equals("ADMIN")) {
            loadVehiclesForCombo(vehicleReportCombo);
        }
    }

    private void loadVehiclesForCombo(ComboBox<Vehicle> combo) {
        List<Vehicle> vehicles = VehicleService.getAllVehicles(currentUser);
        combo.setItems(FXCollections.observableArrayList(vehicles));
        combo.setCellFactory(lv -> new ListCell<Vehicle>() {
            @Override
            protected void updateItem(Vehicle item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getRegistrationNumber() + " - " + item.getMake() + " " + item.getModel());
            }
        });
    }

    private List<PoliceReport> getCustomerPoliceReports() {
        List<PoliceReport> reports = new ArrayList<>();

        String sql = """
            SELECT pr.report_id, pr.vehicle_id, v.registration_number, 
                   pr.report_date, pr.report_type, pr.description, pr.officer_name
            FROM police_report pr
            JOIN vehicle v ON pr.vehicle_id = v.vehicle_id
            WHERE v.owner_id = (SELECT customer_id FROM customer WHERE email LIKE ? OR name LIKE ?)
            ORDER BY pr.report_date DESC
        """;

        try (PreparedStatement pstmt = currentUser.getDbConnection().prepareStatement(sql)) {
            String searchPattern = "%" + currentUser.getUsername().replace("_user", "") + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                reports.add(new PoliceReport(
                        rs.getInt("report_id"),
                        rs.getInt("vehicle_id"),
                        rs.getString("registration_number"),
                        rs.getDate("report_date").toLocalDate(),
                        rs.getString("report_type"),
                        rs.getString("description"),
                        rs.getString("officer_name")
                ));
            }
        } catch (SQLException e) {
            AlertUtils.showError("Database Error", "Failed to load police reports: " + e.getMessage());
        }

        return reports;
    }

    private void loadViolations() {
        List<Violation> violations;

        if (currentUser.getRole().equals("CUSTOMER")) {
            violations = getCustomerViolations();
        } else {
            violations = PoliceService.getAllViolations(currentUser);
        }

        violationTable.setItems(FXCollections.observableArrayList(violations));

        if (!currentUser.getRole().equals("ADMIN")) {
            loadVehiclesForCombo(vehicleViolationCombo);
        }
    }

    private List<Violation> getCustomerViolations() {
        List<Violation> violations = new ArrayList<>();

        String sql = """
            SELECT v.violation_id, v.vehicle_id, ve.registration_number,
                   v.violation_date, v.violation_type, v.fine_amount, v.officer_name
            FROM violation v
            JOIN vehicle ve ON v.vehicle_id = ve.vehicle_id
            WHERE ve.owner_id = (SELECT customer_id FROM customer WHERE email LIKE ? OR name LIKE ?)
            ORDER BY v.violation_date DESC
        """;

        try (PreparedStatement pstmt = currentUser.getDbConnection().prepareStatement(sql)) {
            String searchPattern = "%" + currentUser.getUsername().replace("_user", "") + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                violations.add(new Violation(
                        rs.getInt("violation_id"),
                        rs.getInt("vehicle_id"),
                        rs.getString("registration_number"),
                        rs.getDate("violation_date").toLocalDate(),
                        rs.getString("violation_type"),
                        rs.getDouble("fine_amount"),
                        rs.getString("officer_name")
                ));
            }
        } catch (SQLException e) {
            AlertUtils.showError("Database Error", "Failed to load violations: " + e.getMessage());
        }

        return violations;
    }

    private void handleAddReport() {
        try {
            PoliceReport report = new PoliceReport();

            int vehicleId;
            String vehicleReg;
            String officerName;

            if (currentUser.getRole().equals("ADMIN")) {
                Vehicle selectedVehicle = vehicleReportSearchCombo.getValue();
                if (selectedVehicle == null) {
                    AlertUtils.showWarning("Missing Data", "Please search and select a vehicle.");
                    return;
                }
                vehicleId = selectedVehicle.getVehicleId();
                vehicleReg = selectedVehicle.getRegistrationNumber();

                User selectedOfficer = officerReportSearchCombo.getValue();
                if (selectedOfficer == null) {
                    AlertUtils.showWarning("Missing Data", "Please search and select an officer.");
                    return;
                }
                officerName = selectedOfficer.getFullName();
            } else {
                Vehicle selectedVehicle = vehicleReportCombo.getValue();
                if (selectedVehicle == null) {
                    AlertUtils.showWarning("Missing Data", "Please select a vehicle.");
                    return;
                }
                vehicleId = selectedVehicle.getVehicleId();
                vehicleReg = selectedVehicle.getRegistrationNumber();

                officerName = currentUser.getFullName();
            }

            report.setVehicleId(vehicleId);
            report.setVehicleReg(vehicleReg);
            report.setReportType(reportTypeCombo.getValue());
            report.setDescription(txtReportDesc.getText());
            report.setOfficerName(officerName);
            report.setReportDate(LocalDate.now());

            if (PoliceService.addPoliceReport(currentUser, report)) {
                txtReportDesc.clear();
                reportTypeCombo.setValue(null);

                if (currentUser.getRole().equals("ADMIN")) {
                    txtSearchVehicleReport.clear();
                    if (vehicleReportSearchCombo != null) {
                        vehicleReportSearchCombo.setItems(null);
                        vehicleReportSearchCombo.setVisible(false);
                    }
                    selectedVehicleReportLabel.setText("No vehicle selected");
                    txtSearchOfficerReport.clear();
                    if (officerReportSearchCombo != null) {
                        officerReportSearchCombo.setItems(null);
                        officerReportSearchCombo.setVisible(false);
                    }
                    selectedOfficerReportLabel.setText("No officer selected");
                } else {
                    vehicleReportCombo.setValue(null);
                }

                loadReports();
            }
        } catch (Exception e) {
            AlertUtils.showError("Error", "Failed to add report: " + e.getMessage());
        }
    }

    private void handleAddViolation() {
        try {
            Violation violation = new Violation();

            int vehicleId;
            String vehicleReg;
            String officerName;

            if (currentUser.getRole().equals("ADMIN")) {
                Vehicle selectedVehicle = vehicleViolationSearchCombo.getValue();
                if (selectedVehicle == null) {
                    AlertUtils.showWarning("Missing Data", "Please search and select a vehicle.");
                    return;
                }
                vehicleId = selectedVehicle.getVehicleId();
                vehicleReg = selectedVehicle.getRegistrationNumber();

                User selectedOfficer = officerViolationSearchCombo.getValue();
                if (selectedOfficer == null) {
                    AlertUtils.showWarning("Missing Data", "Please search and select an officer.");
                    return;
                }
                officerName = selectedOfficer.getFullName();
            } else {
                Vehicle selectedVehicle = vehicleViolationCombo.getValue();
                if (selectedVehicle == null) {
                    AlertUtils.showWarning("Missing Data", "Please select a vehicle.");
                    return;
                }
                vehicleId = selectedVehicle.getVehicleId();
                vehicleReg = selectedVehicle.getRegistrationNumber();

                officerName = currentUser.getFullName();
            }

            violation.setVehicleId(vehicleId);
            violation.setVehicleReg(vehicleReg);
            violation.setViolationType(violationTypeCombo.getValue());
            violation.setFineAmount(Double.parseDouble(txtFineAmount.getText()));
            violation.setViolationDate(LocalDate.now());
            violation.setOfficerName(officerName);

            if (PoliceService.addViolation(currentUser, violation)) {
                txtFineAmount.clear();
                violationTypeCombo.setValue(null);

                if (currentUser.getRole().equals("ADMIN")) {
                    txtSearchVehicleViolation.clear();
                    if (vehicleViolationSearchCombo != null) {
                        vehicleViolationSearchCombo.setItems(null);
                        vehicleViolationSearchCombo.setVisible(false);
                    }
                    selectedVehicleViolationLabel.setText("No vehicle selected");
                    txtSearchOfficerViolation.clear();
                    if (officerViolationSearchCombo != null) {
                        officerViolationSearchCombo.setItems(null);
                        officerViolationSearchCombo.setVisible(false);
                    }
                    selectedOfficerViolationLabel.setText("No officer selected");
                } else {
                    vehicleViolationCombo.setValue(null);
                }

                loadViolations();
            }
        } catch (NumberFormatException e) {
            AlertUtils.showError("Invalid Input", "Please enter a valid fine amount.");
        } catch (Exception e) {
            AlertUtils.showError("Error", "Failed to add violation: " + e.getMessage());
        }
    }
}