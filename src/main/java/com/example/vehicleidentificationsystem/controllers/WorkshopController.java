package com.example.vehicleidentificationsystem.controllers;

import com.example.vehicleidentificationsystem.models.*;
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

public class WorkshopController {

    private TableView<ServiceRecord> serviceTable;
    private TextField txtServiceType, txtCost;
    private TextArea txtDescription;
    private User currentUser;

    // For ADMIN - search fields
    private TextField txtSearchVehicle;
    private ComboBox<Vehicle> vehicleSearchCombo;
    private Label selectedVehicleLabel;
    private TextField txtSearchWorkshop;
    private ComboBox<User> workshopSearchCombo;
    private Label selectedWorkshopLabel;

    // For regular users - simple combo
    private ComboBox<Vehicle> vehicleCombo;

    public VBox createView(User user) {
        this.currentUser = user;

        // Main container with 40/60 split
        HBox mainContainer = new HBox(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setAlignment(Pos.TOP_CENTER);

        // LEFT SIDE (40%) - Form
        VBox leftPanel = createLeftPanel();

        // RIGHT SIDE (60%) - Table
        VBox rightPanel = createRightPanel();

        // Set percentage widths
        leftPanel.setPrefWidth(400);
        rightPanel.setPrefWidth(600);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        mainContainer.getChildren().addAll(leftPanel, rightPanel);

        loadData();

        return new VBox(mainContainer);
    }

    private VBox createLeftPanel() {
        VBox leftPanel = new VBox(15);
        leftPanel.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");
        leftPanel.setPadding(new Insets(20));

        Label formTitle = new Label("Add New Service Record");
        formTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

        VBox formPanel = createFormPanel();

        leftPanel.getChildren().addAll(formTitle, formPanel);

        return leftPanel;
    }

    private VBox createRightPanel() {
        VBox rightPanel = new VBox(10);
        rightPanel.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");
        rightPanel.setPadding(new Insets(15));

        Label tableTitle = new Label("Service Records");
        tableTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

        serviceTable = new TableView<>();
        serviceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        serviceTable.setPlaceholder(new Label("No service records found"));
        createTableColumns();

        VBox.setVgrow(serviceTable, Priority.ALWAYS);

        rightPanel.getChildren().addAll(tableTitle, serviceTable);

        return rightPanel;
    }

    private VBox createFormPanel() {
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
            txtSearchVehicle = new TextField();
            txtSearchVehicle.setPromptText("Enter registration number");
            txtSearchVehicle.setPrefWidth(180);
            Button btnSearchVehicle = new Button("Search");
            btnSearchVehicle.setStyle("-fx-background-color: #1d3557; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 6;");
            btnSearchVehicle.setOnAction(e -> searchVehicle());
            vehicleSearchBox.getChildren().addAll(txtSearchVehicle, btnSearchVehicle);
            formGrid.add(vehicleSearchBox, 1, row);
            row++;

            formGrid.add(new Label("Select:"), 0, row);
            vehicleSearchCombo = new ComboBox<>();
            vehicleSearchCombo.setPromptText("Select from results");
            vehicleSearchCombo.setPrefWidth(250);
            vehicleSearchCombo.setVisible(false);
            vehicleSearchCombo.setOnAction(e -> {
                if (vehicleSearchCombo.getValue() != null) {
                    selectedVehicleLabel.setText("Selected: " + vehicleSearchCombo.getValue().getRegistrationNumber());
                }
            });
            formGrid.add(vehicleSearchCombo, 1, row);
            row++;

            selectedVehicleLabel = new Label("No vehicle selected");
            selectedVehicleLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");
            formGrid.add(selectedVehicleLabel, 1, row);
            row++;

            // Workshop Search Section
            formGrid.add(new Label("Workshop:"), 0, row);
            HBox workshopSearchBox = new HBox(8);
            txtSearchWorkshop = new TextField();
            txtSearchWorkshop.setPromptText("Enter workshop name");
            txtSearchWorkshop.setPrefWidth(180);
            Button btnSearchWorkshop = new Button("Search");
            btnSearchWorkshop.setStyle("-fx-background-color: #1d3557; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 6;");
            btnSearchWorkshop.setOnAction(e -> searchWorkshop());
            workshopSearchBox.getChildren().addAll(txtSearchWorkshop, btnSearchWorkshop);
            formGrid.add(workshopSearchBox, 1, row);
            row++;

            formGrid.add(new Label("Select:"), 0, row);
            workshopSearchCombo = new ComboBox<>();
            workshopSearchCombo.setPromptText("Select from results");
            workshopSearchCombo.setPrefWidth(250);
            workshopSearchCombo.setVisible(false);
            workshopSearchCombo.setOnAction(e -> {
                if (workshopSearchCombo.getValue() != null) {
                    selectedWorkshopLabel.setText("Selected: " + workshopSearchCombo.getValue().getFullName());
                }
            });
            formGrid.add(workshopSearchCombo, 1, row);
            row++;

            selectedWorkshopLabel = new Label("No workshop selected");
            selectedWorkshopLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");
            formGrid.add(selectedWorkshopLabel, 1, row);
            row++;
        } else {
            // For regular WORKSHOP user
            formGrid.add(new Label("Vehicle:"), 0, row);
            vehicleCombo = new ComboBox<>();
            vehicleCombo.setPromptText("Select vehicle");
            vehicleCombo.setPrefWidth(250);
            formGrid.add(vehicleCombo, 1, row);
            row++;
        }

        // Separator
        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));
        formGrid.add(separator, 0, row, 2, 1);
        row++;

        // Service Type
        formGrid.add(new Label("Service Type:"), 0, row);
        txtServiceType = new TextField();
        txtServiceType.setPromptText("e.g., Oil Change, Brake Service");
        txtServiceType.setPrefWidth(200);
        formGrid.add(txtServiceType, 1, row);
        row++;

        // Cost
        formGrid.add(new Label("Cost (M):"), 0, row);
        txtCost = new TextField();
        txtCost.setPromptText("Enter amount");
        txtCost.setPrefWidth(150);
        formGrid.add(txtCost, 1, row);
        row++;

        // Description
        formGrid.add(new Label("Description:"), 0, row);
        txtDescription = new TextArea();
        txtDescription.setPromptText("Enter service details...");
        txtDescription.setPrefRowCount(3);
        txtDescription.setPrefWidth(280);
        txtDescription.setWrapText(true);
        formGrid.add(txtDescription, 1, row);
        row++;

        // Submit Button
        Button btnSubmit = new Button("Add Service Record");
        btnSubmit.setStyle("-fx-background-color: #1d3557; -fx-text-fill: white; -fx-font-weight: 500; -fx-padding: 10 20; -fx-background-radius: 6; -fx-cursor: hand;");
        btnSubmit.setOnMouseEntered(e -> btnSubmit.setStyle("-fx-background-color: #457b9d; -fx-text-fill: white; -fx-font-weight: 500; -fx-padding: 10 20; -fx-background-radius: 6; -fx-cursor: hand;"));
        btnSubmit.setOnMouseExited(e -> btnSubmit.setStyle("-fx-background-color: #1d3557; -fx-text-fill: white; -fx-font-weight: 500; -fx-padding: 10 20; -fx-background-radius: 6; -fx-cursor: hand;"));
        btnSubmit.setOnAction(e -> handleAddService());

        HBox buttonBox = new HBox(btnSubmit);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 5, 0));
        formGrid.add(buttonBox, 0, row, 2, 1);

        formPanel.getChildren().add(formGrid);
        return formPanel;
    }

    private void createTableColumns() {
        TableColumn<ServiceRecord, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("serviceId"));
        idCol.setPrefWidth(50);

        TableColumn<ServiceRecord, String> vehicleCol = new TableColumn<>("Vehicle");
        vehicleCol.setCellValueFactory(new PropertyValueFactory<>("vehicleReg"));
        vehicleCol.setPrefWidth(120);

        TableColumn<ServiceRecord, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("serviceDate"));
        dateCol.setPrefWidth(100);

        TableColumn<ServiceRecord, String> typeCol = new TableColumn<>("Service Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("serviceType"));
        typeCol.setPrefWidth(120);

        TableColumn<ServiceRecord, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(200);

        TableColumn<ServiceRecord, Double> costCol = new TableColumn<>("Cost");
        costCol.setCellValueFactory(new PropertyValueFactory<>("cost"));
        costCol.setPrefWidth(80);

        TableColumn<ServiceRecord, String> workshopCol = new TableColumn<>("Workshop");
        workshopCol.setCellValueFactory(new PropertyValueFactory<>("workshopName"));
        workshopCol.setPrefWidth(120);

        serviceTable.getColumns().addAll(idCol, vehicleCol, dateCol, typeCol, descCol, costCol, workshopCol);
    }

    private void searchVehicle() {
        String searchText = txtSearchVehicle.getText().trim();
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
            vehicleSearchCombo.setVisible(false);
        } else {
            vehicleSearchCombo.setItems(FXCollections.observableArrayList(vehicles));
            vehicleSearchCombo.setCellFactory(lv -> new ListCell<Vehicle>() {
                @Override
                protected void updateItem(Vehicle item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getRegistrationNumber() + " - " + item.getMake() + " " + item.getModel());
                }
            });
            vehicleSearchCombo.setVisible(true);
            vehicleSearchCombo.setPromptText("Select vehicle (" + vehicles.size() + " found)");
        }
    }

    private void searchWorkshop() {
        String searchText = txtSearchWorkshop.getText().trim();
        if (searchText.isEmpty()) {
            AlertUtils.showWarning("Missing Data", "Please enter workshop name to search.");
            return;
        }

        List<User> workshops = new ArrayList<>();
        String sql = "SELECT user_id, username, full_name FROM app_user WHERE user_type = 'WORKSHOP' AND (username LIKE ? OR full_name LIKE ?)";

        try (PreparedStatement pstmt = currentUser.getDbConnection().prepareStatement(sql)) {
            String pattern = "%" + searchText + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                User workshop = new WorkshopUser(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("full_name"),
                        currentUser.getDbConnection()
                );
                workshops.add(workshop);
            }
        } catch (SQLException e) {
            AlertUtils.showError("Database Error", "Failed to search workshops: " + e.getMessage());
        }

        if (workshops.isEmpty()) {
            AlertUtils.showWarning("No Results", "No workshops found matching: " + searchText);
            workshopSearchCombo.setVisible(false);
        } else {
            workshopSearchCombo.setItems(FXCollections.observableArrayList(workshops));
            workshopSearchCombo.setCellFactory(lv -> new ListCell<User>() {
                @Override
                protected void updateItem(User item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getFullName() + " (" + item.getUsername() + ")");
                }
            });
            workshopSearchCombo.setVisible(true);
            workshopSearchCombo.setPromptText("Select workshop (" + workshops.size() + " found)");
        }
    }

    private void loadData() {
        List<ServiceRecord> records = getAllServiceRecords();
        serviceTable.setItems(FXCollections.observableArrayList(records));

        if (!currentUser.getRole().equals("ADMIN")) {
            List<Vehicle> vehicles = VehicleService.getAllVehicles(currentUser);
            vehicleCombo.setItems(FXCollections.observableArrayList(vehicles));
            vehicleCombo.setCellFactory(lv -> new ListCell<Vehicle>() {
                @Override
                protected void updateItem(Vehicle item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getRegistrationNumber() + " - " + item.getMake() + " " + item.getModel());
                }
            });
        }
    }

    private List<ServiceRecord> getAllServiceRecords() {
        List<ServiceRecord> records = new ArrayList<>();

        String sql;

        if (currentUser.getRole().equals("WORKSHOP")) {
            sql = """
                SELECT s.service_id, s.vehicle_id, v.registration_number,
                       s.service_date, s.service_type, s.description, s.cost,
                       s.workshop_id, u.full_name as workshop_name
                FROM service_record s
                JOIN vehicle v ON s.vehicle_id = v.vehicle_id
                LEFT JOIN app_user u ON s.workshop_id = u.user_id
                WHERE s.workshop_id = ?
                ORDER BY s.service_date DESC
            """;
        } else if (currentUser.getRole().equals("ADMIN")) {
            sql = """
                SELECT s.service_id, s.vehicle_id, v.registration_number,
                       s.service_date, s.service_type, s.description, s.cost,
                       s.workshop_id, u.full_name as workshop_name
                FROM service_record s
                JOIN vehicle v ON s.vehicle_id = v.vehicle_id
                LEFT JOIN app_user u ON s.workshop_id = u.user_id
                ORDER BY s.service_date DESC
            """;
        } else {
            sql = """
                SELECT s.service_id, s.vehicle_id, v.registration_number,
                       s.service_date, s.service_type, s.description, s.cost,
                       s.workshop_id, u.full_name as workshop_name
                FROM service_record s
                JOIN vehicle v ON s.vehicle_id = v.vehicle_id
                LEFT JOIN app_user u ON s.workshop_id = u.user_id
                WHERE v.owner_id = (SELECT customer_id FROM customer WHERE email LIKE ? OR name LIKE ?)
                ORDER BY s.service_date DESC
            """;
        }

        try (PreparedStatement pstmt = currentUser.getDbConnection().prepareStatement(sql)) {
            if (currentUser.getRole().equals("WORKSHOP")) {
                pstmt.setInt(1, currentUser.getUserId());
            } else if (currentUser.getRole().equals("CUSTOMER")) {
                String searchPattern = "%" + currentUser.getUsername().replace("_user", "") + "%";
                pstmt.setString(1, searchPattern);
                pstmt.setString(2, searchPattern);
            }
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                records.add(new ServiceRecord(
                        rs.getInt("service_id"),
                        rs.getInt("vehicle_id"),
                        rs.getString("registration_number"),
                        rs.getDate("service_date").toLocalDate(),
                        rs.getString("service_type"),
                        rs.getString("description"),
                        rs.getDouble("cost"),
                        rs.getInt("workshop_id"),
                        rs.getString("workshop_name") != null ? rs.getString("workshop_name") : "Unknown Workshop"
                ));
            }
        } catch (SQLException e) {
            AlertUtils.showError("Database Error", "Failed to load service records: " + e.getMessage());
        }

        return records;
    }

    private void handleAddService() {
        if (!currentUser.canAddServiceRecords() && !currentUser.getRole().equals("ADMIN")) {
            AlertUtils.showError("Permission Denied", "You don't have permission to add service records.");
            return;
        }

        try {
            int vehicleId;
            String vehicleReg;

            if (currentUser.getRole().equals("ADMIN")) {
                Vehicle selected = vehicleSearchCombo.getValue();
                if (selected == null) {
                    AlertUtils.showWarning("Missing Data", "Please search and select a vehicle.");
                    return;
                }
                vehicleId = selected.getVehicleId();
                vehicleReg = selected.getRegistrationNumber();
            } else {
                Vehicle selected = vehicleCombo.getValue();
                if (selected == null) {
                    AlertUtils.showWarning("Missing Data", "Please select a vehicle.");
                    return;
                }
                vehicleId = selected.getVehicleId();
                vehicleReg = selected.getRegistrationNumber();
            }

            int workshopId;
            String workshopName;

            if (currentUser.getRole().equals("ADMIN")) {
                User selectedWorkshop = workshopSearchCombo.getValue();
                if (selectedWorkshop == null) {
                    AlertUtils.showWarning("Missing Data", "Please search and select a workshop.");
                    return;
                }
                workshopId = selectedWorkshop.getUserId();
                workshopName = selectedWorkshop.getFullName();
            } else {
                workshopId = currentUser.getUserId();
                workshopName = currentUser.getFullName();
            }

            String sql = "INSERT INTO service_record (vehicle_id, service_type, description, cost, service_date, workshop_id, workshop_name) VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = currentUser.getDbConnection().prepareStatement(sql)) {
                pstmt.setInt(1, vehicleId);
                pstmt.setString(2, txtServiceType.getText());
                pstmt.setString(3, txtDescription.getText());
                pstmt.setDouble(4, Double.parseDouble(txtCost.getText()));
                pstmt.setDate(5, Date.valueOf(LocalDate.now()));
                pstmt.setInt(6, workshopId);
                pstmt.setString(7, workshopName);
                pstmt.executeUpdate();

                AlertUtils.showInfo("Success", "Service record added successfully!");

                txtServiceType.clear();
                txtDescription.clear();
                txtCost.clear();

                if (currentUser.getRole().equals("ADMIN")) {
                    txtSearchVehicle.clear();
                    if (vehicleSearchCombo != null) {
                        vehicleSearchCombo.setItems(null);
                        vehicleSearchCombo.setVisible(false);
                    }
                    selectedVehicleLabel.setText("No vehicle selected");
                    txtSearchWorkshop.clear();
                    if (workshopSearchCombo != null) {
                        workshopSearchCombo.setItems(null);
                        workshopSearchCombo.setVisible(false);
                    }
                    selectedWorkshopLabel.setText("No workshop selected");
                } else {
                    vehicleCombo.setValue(null);
                }

                loadData();
            }
        } catch (NumberFormatException e) {
            AlertUtils.showError("Invalid Input", "Please enter a valid cost amount.");
        } catch (SQLException e) {
            AlertUtils.showError("Database Error", "Failed to add service record: " + e.getMessage());
        }
    }
}