package com.example.vehicleidentificationsystem.controllers;

import com.example.vehicleidentificationsystem.models.*;
import com.example.vehicleidentificationsystem.services.PoliceService;
import com.example.vehicleidentificationsystem.services.VehicleService;
import com.example.vehicleidentificationsystem.utils.AlertUtils;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
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

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label title = new Label("Police Module");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        tabPane = new TabPane();

        Tab reportsTab = new Tab("Police Reports");
        reportsTab.setContent(createReportsTab());
        reportsTab.setClosable(false);

        Tab violationsTab = new Tab("Violations");
        violationsTab.setContent(createViolationsTab());
        violationsTab.setClosable(false);

        tabPane.getTabs().addAll(reportsTab, violationsTab);

        root.getChildren().addAll(title, tabPane);

        return root;
    }

    private VBox createReportsTab() {
        VBox container = new VBox(15);

        // FORM FIRST (at the top)
        VBox formPanel = createReportForm();

        // TABLE SECOND (below)
        reportTable = new TableView<>();
        reportTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        createReportColumns();

        container.getChildren().addAll(formPanel, reportTable);

        loadReports();

        return container;
    }

    private void createReportColumns() {
        TableColumn<PoliceReport, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("reportId"));

        TableColumn<PoliceReport, String> vehicleCol = new TableColumn<>("Vehicle");
        vehicleCol.setCellValueFactory(new PropertyValueFactory<>("vehicleReg"));

        TableColumn<PoliceReport, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("reportDate"));

        TableColumn<PoliceReport, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("reportType"));

        TableColumn<PoliceReport, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<PoliceReport, String> officerCol = new TableColumn<>("Officer");
        officerCol.setCellValueFactory(new PropertyValueFactory<>("officerName"));

        reportTable.getColumns().addAll(idCol, vehicleCol, dateCol, typeCol, descCol, officerCol);
    }

    private VBox createReportForm() {
        VBox formPanel = new VBox(10);
        formPanel.setPadding(new Insets(10));
        formPanel.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-color: #fafafa;");

        Label formTitle = new Label("Add New Police Report");
        formTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(8);

        int row = 0;

        if (currentUser.getRole().equals("ADMIN")) {
            // Vehicle search for ADMIN
            formGrid.add(new Label("Vehicle (Search):"), 0, row);
            txtSearchVehicleReport = new TextField();
            txtSearchVehicleReport.setPromptText("Enter registration number to search");
            txtSearchVehicleReport.setPrefWidth(180);
            Button btnSearchVehicle = new Button("Search");
            btnSearchVehicle.setOnAction(e -> searchVehicleForReport());
            HBox searchVehicleBox = new HBox(5, txtSearchVehicleReport, btnSearchVehicle);
            formGrid.add(searchVehicleBox, 1, row++);

            formGrid.add(new Label("Select Vehicle:"), 0, row);
            vehicleReportSearchCombo = new ComboBox<>();
            vehicleReportSearchCombo.setPromptText("Search results will appear here");
            vehicleReportSearchCombo.setPrefWidth(300);
            vehicleReportSearchCombo.setVisible(false);
            formGrid.add(vehicleReportSearchCombo, 1, row++);

            selectedVehicleReportLabel = new Label("No vehicle selected");
            selectedVehicleReportLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");
            formGrid.add(selectedVehicleReportLabel, 1, row++);

            // Officer search for ADMIN
            formGrid.add(new Label("Officer (Search):"), 0, row);
            txtSearchOfficerReport = new TextField();
            txtSearchOfficerReport.setPromptText("Enter officer name to search");
            txtSearchOfficerReport.setPrefWidth(180);
            Button btnSearchOfficer = new Button("Search");
            btnSearchOfficer.setOnAction(e -> searchOfficerForReport());
            HBox searchOfficerBox = new HBox(5, txtSearchOfficerReport, btnSearchOfficer);
            formGrid.add(searchOfficerBox, 1, row++);

            formGrid.add(new Label("Select Officer:"), 0, row);
            officerReportSearchCombo = new ComboBox<>();
            officerReportSearchCombo.setPromptText("Search results will appear here");
            officerReportSearchCombo.setPrefWidth(300);
            officerReportSearchCombo.setVisible(false);
            formGrid.add(officerReportSearchCombo, 1, row++);

            selectedOfficerReportLabel = new Label("No officer selected");
            selectedOfficerReportLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");
            formGrid.add(selectedOfficerReportLabel, 1, row++);
        } else {
            // For regular POLICE user
            formGrid.add(new Label("Vehicle:"), 0, row);
            vehicleReportCombo = new ComboBox<>();
            vehicleReportCombo.setPrefWidth(250);
            formGrid.add(vehicleReportCombo, 1, row++);
        }

        formGrid.add(new Label("Report Type:"), 0, row);
        reportTypeCombo = new ComboBox<>();
        reportTypeCombo.getItems().addAll("Accident", "Theft", "Stolen Vehicle");
        formGrid.add(reportTypeCombo, 1, row++);

        formGrid.add(new Label("Description:"), 0, row);
        txtReportDesc = new TextArea();
        txtReportDesc.setPrefRowCount(3);
        txtReportDesc.setPrefWidth(450);
        formGrid.add(txtReportDesc, 1, row, 2, 1);
        row++;

        Button btnSubmit = new Button("Add Report");
        btnSubmit.setOnAction(e -> handleAddReport());

        formPanel.getChildren().addAll(formTitle, formGrid, btnSubmit);

        return formPanel;
    }

    private VBox createViolationsTab() {
        VBox container = new VBox(15);

        // FORM FIRST (at the top)
        VBox formPanel = createViolationForm();

        // TABLE SECOND (below)
        violationTable = new TableView<>();
        violationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        createViolationColumns();

        container.getChildren().addAll(formPanel, violationTable);

        loadViolations();

        return container;
    }

    private void createViolationColumns() {
        TableColumn<Violation, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("violationId"));

        TableColumn<Violation, String> vehicleCol = new TableColumn<>("Vehicle");
        vehicleCol.setCellValueFactory(new PropertyValueFactory<>("vehicleReg"));

        TableColumn<Violation, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("violationDate"));

        TableColumn<Violation, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("violationType"));

        TableColumn<Violation, Double> fineCol = new TableColumn<>("Fine Amount");
        fineCol.setCellValueFactory(new PropertyValueFactory<>("fineAmount"));

        TableColumn<Violation, String> officerCol = new TableColumn<>("Officer");
        officerCol.setCellValueFactory(new PropertyValueFactory<>("officerName"));

        violationTable.getColumns().addAll(idCol, vehicleCol, dateCol, typeCol, fineCol, officerCol);
    }

    private VBox createViolationForm() {
        VBox formPanel = new VBox(10);
        formPanel.setPadding(new Insets(10));
        formPanel.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-color: #fafafa;");

        Label formTitle = new Label("Add New Violation");
        formTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(8);

        int row = 0;

        if (currentUser.getRole().equals("ADMIN")) {
            // Vehicle search for ADMIN
            formGrid.add(new Label("Vehicle (Search):"), 0, row);
            txtSearchVehicleViolation = new TextField();
            txtSearchVehicleViolation.setPromptText("Enter registration number to search");
            txtSearchVehicleViolation.setPrefWidth(180);
            Button btnSearchVehicle = new Button("Search");
            btnSearchVehicle.setOnAction(e -> searchVehicleForViolation());
            HBox searchVehicleBox = new HBox(5, txtSearchVehicleViolation, btnSearchVehicle);
            formGrid.add(searchVehicleBox, 1, row++);

            formGrid.add(new Label("Select Vehicle:"), 0, row);
            vehicleViolationSearchCombo = new ComboBox<>();
            vehicleViolationSearchCombo.setPromptText("Search results will appear here");
            vehicleViolationSearchCombo.setPrefWidth(300);
            vehicleViolationSearchCombo.setVisible(false);
            formGrid.add(vehicleViolationSearchCombo, 1, row++);

            selectedVehicleViolationLabel = new Label("No vehicle selected");
            selectedVehicleViolationLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");
            formGrid.add(selectedVehicleViolationLabel, 1, row++);

            // Officer search for ADMIN
            formGrid.add(new Label("Officer (Search):"), 0, row);
            txtSearchOfficerViolation = new TextField();
            txtSearchOfficerViolation.setPromptText("Enter officer name to search");
            txtSearchOfficerViolation.setPrefWidth(180);
            Button btnSearchOfficer = new Button("Search");
            btnSearchOfficer.setOnAction(e -> searchOfficerForViolation());
            HBox searchOfficerBox = new HBox(5, txtSearchOfficerViolation, btnSearchOfficer);
            formGrid.add(searchOfficerBox, 1, row++);

            formGrid.add(new Label("Select Officer:"), 0, row);
            officerViolationSearchCombo = new ComboBox<>();
            officerViolationSearchCombo.setPromptText("Search results will appear here");
            officerViolationSearchCombo.setPrefWidth(300);
            officerViolationSearchCombo.setVisible(false);
            formGrid.add(officerViolationSearchCombo, 1, row++);

            selectedOfficerViolationLabel = new Label("No officer selected");
            selectedOfficerViolationLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");
            formGrid.add(selectedOfficerViolationLabel, 1, row++);
        } else {
            // For regular POLICE user
            formGrid.add(new Label("Vehicle:"), 0, row);
            vehicleViolationCombo = new ComboBox<>();
            vehicleViolationCombo.setPrefWidth(250);
            formGrid.add(vehicleViolationCombo, 1, row++);
        }

        formGrid.add(new Label("Violation Type:"), 0, row);
        violationTypeCombo = new ComboBox<>();
        violationTypeCombo.getItems().addAll("Speeding", "No seatbelt", "Running red light", "Drunk driving", "No license", "Overloading");
        formGrid.add(violationTypeCombo, 1, row++);

        formGrid.add(new Label("Fine Amount:"), 0, row);
        txtFineAmount = new TextField();
        formGrid.add(txtFineAmount, 1, row++);

        Button btnSubmit = new Button("Add Violation");
        btnSubmit.setOnAction(e -> handleAddViolation());

        formPanel.getChildren().addAll(formTitle, formGrid, btnSubmit);

        return formPanel;
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

            vehicleReportSearchCombo.setOnAction(e -> {
                Vehicle selected = vehicleReportSearchCombo.getValue();
                if (selected != null) {
                    selectedVehicleReportLabel.setText("Selected: " + selected.getRegistrationNumber());
                }
            });
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

            officerReportSearchCombo.setOnAction(e -> {
                User selected = officerReportSearchCombo.getValue();
                if (selected != null) {
                    selectedOfficerReportLabel.setText("Selected: " + selected.getFullName());
                }
            });
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

            vehicleViolationSearchCombo.setOnAction(e -> {
                Vehicle selected = vehicleViolationSearchCombo.getValue();
                if (selected != null) {
                    selectedVehicleViolationLabel.setText("Selected: " + selected.getRegistrationNumber());
                }
            });
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

            officerViolationSearchCombo.setOnAction(e -> {
                User selected = officerViolationSearchCombo.getValue();
                if (selected != null) {
                    selectedOfficerViolationLabel.setText("Selected: " + selected.getFullName());
                }
            });
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