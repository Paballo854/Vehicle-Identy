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

public class CustomerController {

    private TableView<Vehicle> vehicleTable;
    private TableView<CustomerQuery> queryTable;
    private TableView<ServiceRecord> serviceHistoryTable;
    private User currentUser;

    private TextField txtSearchRegistration;
    private ComboBox<Vehicle> searchResultCombo;
    private TextArea txtQueryText;
    private ComboBox<String> queryTypeCombo;

    private ComboBox<WorkshopInfo> workshopCombo;
    private Label workshopLabel;

    private ComboBox<InsuranceCompanyInfo> insuranceCombo;
    private Label insuranceLabel;

    private Label policeInfo;

    public VBox createView(User user) {
        this.currentUser = user;

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-font-size: 13px;");

        String vehiclesTabName = currentUser.getRole().equals("ADMIN") ? "Vehicles" : "My Vehicles";
        String queriesTabName = currentUser.getRole().equals("ADMIN") ? "Queries" : "My Queries";
        String serviceHistoryTabName = currentUser.getRole().equals("ADMIN") ? "Service History" : "My Service History";

        Tab vehiclesTab = new Tab(vehiclesTabName);
        vehiclesTab.setContent(createVehiclesTab());
        vehiclesTab.setClosable(false);

        Tab serviceHistoryTab = new Tab(serviceHistoryTabName);
        serviceHistoryTab.setContent(createServiceHistoryTab());
        serviceHistoryTab.setClosable(false);

        Tab queriesTab = new Tab(queriesTabName);
        queriesTab.setContent(createQueriesTab());
        queriesTab.setClosable(false);

        tabPane.getTabs().addAll(vehiclesTab, serviceHistoryTab, queriesTab);

        return new VBox(tabPane);
    }

    private VBox createVehiclesTab() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");

        Label welcomeLabel = new Label("Welcome " + currentUser.getFullName() + "!");
        welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

        String vehiclesTitle = currentUser.getRole().equals("ADMIN") ? "Vehicles" : "My Vehicles";
        String message = currentUser.getRole().equals("ADMIN") ? vehiclesTitle + " - All vehicles in the system" : vehiclesTitle + " - Vehicles registered under your name";
        Label infoLabel = new Label(message);
        infoLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");

        vehicleTable = new TableView<>();
        vehicleTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        vehicleTable.setPlaceholder(new Label("No vehicles found"));
        createVehicleColumns();

        VBox.setVgrow(vehicleTable, Priority.ALWAYS);

        container.getChildren().addAll(welcomeLabel, infoLabel, vehicleTable);

        loadCustomerVehicles();

        return container;
    }

    private void createVehicleColumns() {
        TableColumn<Vehicle, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("vehicleId"));
        idCol.setPrefWidth(50);

        TableColumn<Vehicle, String> regCol = new TableColumn<>("Registration");
        regCol.setCellValueFactory(new PropertyValueFactory<>("registrationNumber"));
        regCol.setPrefWidth(130);

        TableColumn<Vehicle, String> makeCol = new TableColumn<>("Make");
        makeCol.setCellValueFactory(new PropertyValueFactory<>("make"));
        makeCol.setPrefWidth(100);

        TableColumn<Vehicle, String> modelCol = new TableColumn<>("Model");
        modelCol.setCellValueFactory(new PropertyValueFactory<>("model"));
        modelCol.setPrefWidth(100);

        TableColumn<Vehicle, Integer> yearCol = new TableColumn<>("Year");
        yearCol.setCellValueFactory(new PropertyValueFactory<>("year"));
        yearCol.setPrefWidth(80);

        vehicleTable.getColumns().addAll(idCol, regCol, makeCol, modelCol, yearCol);
    }

    private VBox createServiceHistoryTab() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");

        Label titleLabel = new Label("Service History");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

        Label infoLabel = new Label("Complete service records for your vehicles");
        infoLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");

        serviceHistoryTable = new TableView<>();
        serviceHistoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        serviceHistoryTable.setPlaceholder(new Label("No service records found for your vehicles"));
        createServiceHistoryColumns();

        VBox.setVgrow(serviceHistoryTable, Priority.ALWAYS);

        container.getChildren().addAll(titleLabel, infoLabel, serviceHistoryTable);

        loadServiceHistory();

        return container;
    }

    private void createServiceHistoryColumns() {
        TableColumn<ServiceRecord, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("serviceId"));
        idCol.setPrefWidth(50);

        TableColumn<ServiceRecord, String> vehicleCol = new TableColumn<>("Vehicle");
        vehicleCol.setCellValueFactory(new PropertyValueFactory<>("vehicleReg"));
        vehicleCol.setPrefWidth(120);

        TableColumn<ServiceRecord, String> workshopCol = new TableColumn<>("Workshop");
        workshopCol.setCellValueFactory(new PropertyValueFactory<>("workshopName"));
        workshopCol.setPrefWidth(150);

        TableColumn<ServiceRecord, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("serviceDate"));
        dateCol.setPrefWidth(100);

        TableColumn<ServiceRecord, String> typeCol = new TableColumn<>("Service Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("serviceType"));
        typeCol.setPrefWidth(120);

        TableColumn<ServiceRecord, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(250);

        TableColumn<ServiceRecord, Double> costCol = new TableColumn<>("Cost (M)");
        costCol.setCellValueFactory(new PropertyValueFactory<>("cost"));
        costCol.setPrefWidth(80);

        serviceHistoryTable.getColumns().addAll(idCol, vehicleCol, workshopCol, dateCol, typeCol, descCol, costCol);
    }

    private VBox createQueriesTab() {
        // Main container with 40/60 split
        HBox mainContainer = new HBox(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setAlignment(Pos.TOP_CENTER);

        // LEFT SIDE (40%) - Submit Query Form
        VBox leftPanel = createLeftQueryPanel();

        // RIGHT SIDE (60%) - My Queries Table
        VBox rightPanel = createRightQueryPanel();

        leftPanel.setPrefWidth(400);
        rightPanel.setPrefWidth(600);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        mainContainer.getChildren().addAll(leftPanel, rightPanel);

        loadCustomerQueries();

        return new VBox(mainContainer);
    }

    private VBox createLeftQueryPanel() {
        VBox leftPanel = new VBox(15);
        leftPanel.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");
        leftPanel.setPadding(new Insets(20));

        Label formTitle = new Label("Submit New Query");
        formTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

        VBox formPanel = createQueryFormPanel();

        leftPanel.getChildren().addAll(formTitle, formPanel);

        return leftPanel;
    }

    private VBox createRightQueryPanel() {
        VBox rightPanel = new VBox(10);
        rightPanel.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");
        rightPanel.setPadding(new Insets(15));

        Label tableTitle = new Label("My Queries");
        tableTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

        queryTable = new TableView<>();
        queryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        queryTable.setPlaceholder(new Label("No queries submitted yet"));
        createQueryColumns();

        VBox.setVgrow(queryTable, Priority.ALWAYS);

        rightPanel.getChildren().addAll(tableTitle, queryTable);

        return rightPanel;
    }

    private VBox createQueryFormPanel() {
        VBox formPanel = new VBox(12);

        GridPane formGrid = new GridPane();
        formGrid.setHgap(12);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(10, 0, 0, 0));

        int row = 0;

        // Search Vehicle Section
        formGrid.add(new Label("Search Vehicle:"), 0, row);
        HBox searchBox = new HBox(8);
        txtSearchRegistration = new TextField();
        txtSearchRegistration.setPromptText("Enter registration number");
        txtSearchRegistration.setPrefWidth(180);
        Button btnSearch = new Button("Search");
        btnSearch.setStyle("-fx-background-color: #1d3557; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 6;");
        btnSearch.setOnAction(e -> searchVehicle());
        searchBox.getChildren().addAll(txtSearchRegistration, btnSearch);
        formGrid.add(searchBox, 1, row);
        row++;

        formGrid.add(new Label("Select:"), 0, row);
        searchResultCombo = new ComboBox<>();
        searchResultCombo.setPromptText("Select from results");
        searchResultCombo.setPrefWidth(250);
        searchResultCombo.setVisible(false);
        formGrid.add(searchResultCombo, 1, row);
        row++;

        // Separator
        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));
        formGrid.add(separator, 0, row, 2, 1);
        row++;

        // Query Type
        formGrid.add(new Label("Query Type:"), 0, row);
        queryTypeCombo = new ComboBox<>();
        queryTypeCombo.getItems().addAll("WORKSHOP", "INSURANCE", "POLICE");
        queryTypeCombo.setValue("WORKSHOP");
        queryTypeCombo.setPrefWidth(180);
        queryTypeCombo.setOnAction(e -> updateTargetSelection());
        formGrid.add(queryTypeCombo, 1, row);
        row++;

        // Workshop Selection (hidden by default)
        formGrid.add(new Label("Workshop:"), 0, row);
        workshopCombo = new ComboBox<>();
        workshopCombo.setPromptText("Select workshop");
        workshopCombo.setPrefWidth(250);
        workshopCombo.setVisible(false);
        formGrid.add(workshopCombo, 1, row);

        workshopLabel = new Label("");
        workshopLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");
        workshopLabel.setVisible(false);
        formGrid.add(workshopLabel, 1, ++row);
        row++;

        // Insurance Selection (hidden by default)
        formGrid.add(new Label("Insurance:"), 0, row);
        insuranceCombo = new ComboBox<>();
        insuranceCombo.setPromptText("Select insurance company");
        insuranceCombo.setPrefWidth(250);
        insuranceCombo.setVisible(false);
        formGrid.add(insuranceCombo, 1, row);

        insuranceLabel = new Label("");
        insuranceLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");
        insuranceLabel.setVisible(false);
        formGrid.add(insuranceLabel, 1, ++row);
        row++;

        // Police Info (hidden by default)
        policeInfo = new Label("Police queries go to the general police department.");
        policeInfo.setStyle("-fx-text-fill: #888; -fx-font-style: italic;");
        policeInfo.setVisible(false);
        formGrid.add(policeInfo, 1, row);
        row++;

        // Question
        formGrid.add(new Label("Question:"), 0, row);
        txtQueryText = new TextArea();
        txtQueryText.setPromptText("Enter your question or concern...");
        txtQueryText.setPrefRowCount(4);
        txtQueryText.setPrefWidth(280);
        txtQueryText.setWrapText(true);
        formGrid.add(txtQueryText, 1, row);
        row++;

        // Submit Button
        Button btnSubmit = new Button("Submit Query");
        btnSubmit.setStyle("-fx-background-color: #1d3557; -fx-text-fill: white; -fx-font-weight: 500; -fx-padding: 10 20; -fx-background-radius: 6; -fx-cursor: hand;");
        btnSubmit.setOnMouseEntered(e -> btnSubmit.setStyle("-fx-background-color: #457b9d; -fx-text-fill: white; -fx-font-weight: 500; -fx-padding: 10 20; -fx-background-radius: 6; -fx-cursor: hand;"));
        btnSubmit.setOnMouseExited(e -> btnSubmit.setStyle("-fx-background-color: #1d3557; -fx-text-fill: white; -fx-font-weight: 500; -fx-padding: 10 20; -fx-background-radius: 6; -fx-cursor: hand;"));
        btnSubmit.setOnAction(e -> handleSubmitQuery());

        HBox buttonBox = new HBox(btnSubmit);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 5, 0));
        formGrid.add(buttonBox, 0, row, 2, 1);

        formPanel.getChildren().add(formGrid);
        return formPanel;
    }

    private void updateTargetSelection() {
        String type = queryTypeCombo.getValue();

        workshopCombo.setVisible(false);
        workshopLabel.setVisible(false);
        insuranceCombo.setVisible(false);
        insuranceLabel.setVisible(false);
        policeInfo.setVisible(false);

        if (type.equals("WORKSHOP")) {
            workshopCombo.setVisible(true);
            workshopLabel.setVisible(true);
            loadWorkshopsForVehicle();
        } else if (type.equals("INSURANCE")) {
            insuranceCombo.setVisible(true);
            insuranceLabel.setVisible(true);
            loadInsuranceForVehicle();
        } else if (type.equals("POLICE")) {
            policeInfo.setVisible(true);
        }
    }

    private void loadWorkshopsForVehicle() {
        Vehicle selectedVehicle = searchResultCombo.getValue();
        if (selectedVehicle == null) {
            workshopCombo.setItems(FXCollections.observableArrayList());
            workshopCombo.setPromptText("Select a vehicle first");
            workshopLabel.setText("");
            return;
        }

        List<WorkshopInfo> workshops = new ArrayList<>();
        String sql = """
            SELECT DISTINCT u.user_id, u.full_name as workshop_name, u.username
            FROM service_record s
            JOIN app_user u ON s.workshop_id = u.user_id
            WHERE s.vehicle_id = ?
        """;

        try (PreparedStatement pstmt = currentUser.getDbConnection().prepareStatement(sql)) {
            pstmt.setInt(1, selectedVehicle.getVehicleId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                workshops.add(new WorkshopInfo(
                        rs.getInt("user_id"),
                        rs.getString("workshop_name"),
                        rs.getString("username")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Failed to load workshops: " + e.getMessage());
        }

        if (workshops.isEmpty()) {
            workshopCombo.setItems(FXCollections.observableArrayList());
            workshopCombo.setPromptText("No workshops have serviced this vehicle");
            workshopLabel.setText("No workshops found for this vehicle");
        } else {
            workshopCombo.setItems(FXCollections.observableArrayList(workshops));
            workshopCombo.setCellFactory(lv -> new ListCell<WorkshopInfo>() {
                @Override
                protected void updateItem(WorkshopInfo item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getName());
                }
            });
            workshopCombo.setPromptText("Select workshop");
            workshopLabel.setText("Select the workshop you want to contact");
        }
    }

    private void loadInsuranceForVehicle() {
        Vehicle selectedVehicle = searchResultCombo.getValue();
        if (selectedVehicle == null) {
            insuranceCombo.setItems(FXCollections.observableArrayList());
            insuranceCombo.setPromptText("Select a vehicle first");
            insuranceLabel.setText("");
            return;
        }

        List<InsuranceCompanyInfo> companies = new ArrayList<>();
        String sql = """
            SELECT DISTINCT ic.company_id, ic.name, ic.registration_number
            FROM insurance i
            JOIN insurance_company ic ON i.company_id = ic.company_id
            WHERE i.vehicle_id = ?
        """;

        try (PreparedStatement pstmt = currentUser.getDbConnection().prepareStatement(sql)) {
            pstmt.setInt(1, selectedVehicle.getVehicleId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                companies.add(new InsuranceCompanyInfo(
                        rs.getInt("company_id"),
                        rs.getString("name"),
                        rs.getString("registration_number")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Failed to load insurance companies: " + e.getMessage());
        }

        if (companies.isEmpty()) {
            insuranceCombo.setItems(FXCollections.observableArrayList());
            insuranceCombo.setPromptText("No insurance companies have insured this vehicle");
            insuranceLabel.setText("No insurance companies found for this vehicle");
        } else {
            insuranceCombo.setItems(FXCollections.observableArrayList(companies));
            insuranceCombo.setCellFactory(lv -> new ListCell<InsuranceCompanyInfo>() {
                @Override
                protected void updateItem(InsuranceCompanyInfo item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getName());
                }
            });
            insuranceCombo.setPromptText("Select insurance company");
            insuranceLabel.setText("Select the insurance company you want to contact");
        }
    }

    private void searchVehicle() {
        String searchText = txtSearchRegistration.getText().trim();
        if (searchText.isEmpty()) {
            AlertUtils.showWarning("Missing Data", "Please enter registration number.");
            return;
        }

        List<Vehicle> vehicles = new ArrayList<>();

        if (currentUser.getRole().equals("CUSTOMER")) {
            int customerId = getCustomerIdFromUsername();
            String sql = "SELECT v.vehicle_id, v.registration_number, v.make, v.model, v.year FROM vehicle v WHERE v.owner_id = ? AND v.registration_number ILIKE ?";
            try (PreparedStatement pstmt = currentUser.getDbConnection().prepareStatement(sql)) {
                pstmt.setInt(1, customerId);
                pstmt.setString(2, "%" + searchText + "%");
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    vehicles.add(new Vehicle(
                            rs.getInt("vehicle_id"),
                            rs.getString("registration_number"),
                            rs.getString("make"),
                            rs.getString("model"),
                            rs.getInt("year"),
                            0,
                            ""
                    ));
                }
            } catch (SQLException e) {
                AlertUtils.showError("Database Error", "Failed to search vehicles: " + e.getMessage());
            }
        } else {
            String sql = "SELECT v.vehicle_id, v.registration_number, v.make, v.model, v.year FROM vehicle v WHERE v.registration_number ILIKE ?";
            try (PreparedStatement pstmt = currentUser.getDbConnection().prepareStatement(sql)) {
                pstmt.setString(1, "%" + searchText + "%");
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    vehicles.add(new Vehicle(
                            rs.getInt("vehicle_id"),
                            rs.getString("registration_number"),
                            rs.getString("make"),
                            rs.getString("model"),
                            rs.getInt("year"),
                            0,
                            ""
                    ));
                }
            } catch (SQLException e) {
                AlertUtils.showError("Database Error", "Failed to search vehicles: " + e.getMessage());
            }
        }

        if (vehicles.isEmpty()) {
            AlertUtils.showWarning("No Results", "No vehicles found matching: " + searchText);
            searchResultCombo.setVisible(false);
        } else {
            searchResultCombo.setItems(FXCollections.observableArrayList(vehicles));
            searchResultCombo.setCellFactory(lv -> new ListCell<Vehicle>() {
                @Override
                protected void updateItem(Vehicle item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getRegistrationNumber() + " - " + item.getMake() + " " + item.getModel());
                }
            });
            searchResultCombo.setVisible(true);
            searchResultCombo.setPromptText("Select vehicle (" + vehicles.size() + " found)");
        }
    }

    private void createQueryColumns() {
        TableColumn<CustomerQuery, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("queryId"));
        idCol.setPrefWidth(50);

        TableColumn<CustomerQuery, String> vehicleCol = new TableColumn<>("Vehicle");
        vehicleCol.setCellValueFactory(new PropertyValueFactory<>("vehicleReg"));
        vehicleCol.setPrefWidth(120);

        TableColumn<CustomerQuery, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("queryDate"));
        dateCol.setPrefWidth(100);

        TableColumn<CustomerQuery, String> typeCol = new TableColumn<>("Department");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("queryType"));
        typeCol.setPrefWidth(100);

        TableColumn<CustomerQuery, String> targetCol = new TableColumn<>("Sent To");
        targetCol.setCellValueFactory(new PropertyValueFactory<>("targetName"));
        targetCol.setPrefWidth(150);

        TableColumn<CustomerQuery, String> queryCol = new TableColumn<>("Question");
        queryCol.setCellValueFactory(new PropertyValueFactory<>("queryText"));
        queryCol.setPrefWidth(250);

        TableColumn<CustomerQuery, String> responseCol = new TableColumn<>("Response");
        responseCol.setCellValueFactory(new PropertyValueFactory<>("responseText"));
        responseCol.setPrefWidth(250);

        queryTable.getColumns().addAll(idCol, vehicleCol, dateCol, typeCol, targetCol, queryCol, responseCol);
    }

    private void loadCustomerVehicles() {
        List<Vehicle> customerVehicles = VehicleService.getAllVehicles(currentUser);
        vehicleTable.setItems(FXCollections.observableArrayList(customerVehicles));
    }

    private void loadServiceHistory() {
        List<ServiceRecord> serviceRecords = new ArrayList<>();

        int customerId = getCustomerIdFromUsername();

        String sql = """
            SELECT s.service_id, s.vehicle_id, v.registration_number,
                   s.service_date, s.service_type, s.description, s.cost,
                   s.workshop_id, u.full_name as workshop_name
            FROM service_record s
            JOIN vehicle v ON s.vehicle_id = v.vehicle_id
            LEFT JOIN app_user u ON s.workshop_id = u.user_id
            WHERE v.owner_id = ?
            ORDER BY s.service_date DESC
        """;

        try (PreparedStatement pstmt = currentUser.getDbConnection().prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ServiceRecord record = new ServiceRecord(
                        rs.getInt("service_id"),
                        rs.getInt("vehicle_id"),
                        rs.getString("registration_number"),
                        rs.getDate("service_date").toLocalDate(),
                        rs.getString("service_type"),
                        rs.getString("description"),
                        rs.getDouble("cost"),
                        rs.getInt("workshop_id"),
                        rs.getString("workshop_name") != null ? rs.getString("workshop_name") : "Unknown Workshop"
                );
                serviceRecords.add(record);
            }
        } catch (SQLException e) {
            System.err.println("Service history load warning: " + e.getMessage());
        }

        serviceHistoryTable.setItems(FXCollections.observableArrayList(serviceRecords));
    }

    private void loadCustomerQueries() {
        List<CustomerQuery> queries = new ArrayList<>();

        int customerId = getCustomerIdFromUsername();

        String sql = """
            SELECT q.query_id, q.customer_id, q.vehicle_id, v.registration_number,
                   q.query_date, q.query_text, q.response_text, q.query_type,
                   q.target_id, q.target_type, q.target_name
            FROM customer_query q
            JOIN vehicle v ON q.vehicle_id = v.vehicle_id
            WHERE q.customer_id = ?
            ORDER BY q.query_date DESC
        """;

        try (PreparedStatement pstmt = currentUser.getDbConnection().prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String response = rs.getString("response_text");
                queries.add(new CustomerQuery(
                        rs.getInt("query_id"),
                        rs.getInt("customer_id"),
                        currentUser.getFullName(),
                        rs.getInt("vehicle_id"),
                        rs.getString("registration_number"),
                        rs.getDate("query_date").toLocalDate(),
                        rs.getString("query_text"),
                        response != null ? response : "Awaiting response...",
                        rs.getString("query_type"),
                        rs.getInt("target_id"),
                        rs.getString("target_type"),
                        rs.getString("target_name") != null ? rs.getString("target_name") : rs.getString("query_type")
                ));
            }
        } catch (SQLException e) {
            AlertUtils.showError("Database Error", "Failed to load queries: " + e.getMessage());
        }

        queryTable.setItems(FXCollections.observableArrayList(queries));
    }

    private void handleSubmitQuery() {
        Vehicle selectedVehicle = searchResultCombo.getValue();
        String queryText = txtQueryText.getText().trim();
        String queryType = queryTypeCombo.getValue();

        if (selectedVehicle == null) {
            AlertUtils.showWarning("Missing Data", "Please search and select a vehicle.");
            return;
        }

        if (queryText.isEmpty()) {
            AlertUtils.showWarning("Missing Data", "Please enter your question.");
            return;
        }

        int targetId = 0;
        String targetType = queryType;
        String targetName = queryType;

        if (queryType.equals("WORKSHOP")) {
            WorkshopInfo selectedWorkshop = workshopCombo.getValue();
            if (selectedWorkshop == null) {
                AlertUtils.showWarning("Missing Data", "Please select a workshop to send your query to.");
                return;
            }
            targetId = selectedWorkshop.getId();
            targetType = "WORKSHOP";
            targetName = selectedWorkshop.getName();
        } else if (queryType.equals("INSURANCE")) {
            InsuranceCompanyInfo selectedCompany = insuranceCombo.getValue();
            if (selectedCompany == null) {
                AlertUtils.showWarning("Missing Data", "Please select an insurance company to send your query to.");
                return;
            }
            targetId = selectedCompany.getId();
            targetType = "INSURANCE";
            targetName = selectedCompany.getName();
        } else if (queryType.equals("POLICE")) {
            targetId = 0;
            targetType = "POLICE";
            targetName = "General Police Department";
        }

        int customerId = getCustomerIdFromUsername();

        String sql = "INSERT INTO customer_query (customer_id, vehicle_id, query_text, query_type, target_id, target_type, target_name) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = currentUser.getDbConnection().prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            pstmt.setInt(2, selectedVehicle.getVehicleId());
            pstmt.setString(3, queryText);
            pstmt.setString(4, queryType);
            pstmt.setInt(5, targetId);
            pstmt.setString(6, targetType);
            pstmt.setString(7, targetName);
            pstmt.executeUpdate();

            AlertUtils.showInfo("Success", "Your query has been submitted to " + targetName + ".");

            // Reset form
            txtSearchRegistration.clear();
            searchResultCombo.setItems(null);
            searchResultCombo.setVisible(false);
            txtQueryText.clear();
            queryTypeCombo.setValue("WORKSHOP");
            workshopCombo.setItems(null);
            workshopCombo.setValue(null);
            insuranceCombo.setItems(null);
            insuranceCombo.setValue(null);
            workshopLabel.setText("");
            insuranceLabel.setText("");

            workshopCombo.setVisible(false);
            workshopLabel.setVisible(false);
            insuranceCombo.setVisible(false);
            insuranceLabel.setVisible(false);
            policeInfo.setVisible(false);

            loadCustomerQueries();

        } catch (SQLException e) {
            AlertUtils.showError("Database Error", "Failed to submit query: " + e.getMessage());
        }
    }

    private int getCustomerIdFromUsername() {
        String sql = "SELECT customer_id FROM customer WHERE email LIKE ? OR name LIKE ?";

        try (PreparedStatement pstmt = currentUser.getDbConnection().prepareStatement(sql)) {
            String searchPattern = "%" + currentUser.getUsername().replace("_user", "") + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("customer_id");
            }
        } catch (SQLException e) {
            return 1;
        }

        return 1;
    }

    public static class WorkshopInfo {
        private final int id;
        private final String name;
        private final String username;

        public WorkshopInfo(int id, String name, String username) {
            this.id = id;
            this.name = name;
            this.username = username;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getUsername() { return username; }
    }

    public static class InsuranceCompanyInfo {
        private final int id;
        private final String name;
        private final String registrationNumber;

        public InsuranceCompanyInfo(int id, String name, String registrationNumber) {
            this.id = id;
            this.name = name;
            this.registrationNumber = registrationNumber;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getRegistrationNumber() { return registrationNumber; }
    }
}