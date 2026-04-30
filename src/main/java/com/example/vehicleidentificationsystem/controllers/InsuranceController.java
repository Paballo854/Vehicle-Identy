package com.example.vehicleidentificationsystem.controllers;

import com.example.vehicleidentificationsystem.models.*;
import com.example.vehicleidentificationsystem.services.InsuranceService;
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

public class InsuranceController {

    private TableView<Insurance> insuranceTable;
    private TextField txtPolicyNumber, txtProvider, txtPremiumAmount;
    private DatePicker startDatePicker, expiryDatePicker;
    private ComboBox<String> statusCombo;
    private ComboBox<String> insuranceTypeCombo;
    private User currentUser;

    // For ADMIN - search vehicle fields
    private TextField txtSearchVehicle;
    private ComboBox<Vehicle> vehicleSearchCombo;
    private Label selectedVehicleLabel;

    // For ADMIN - search insurance company fields
    private TextField txtSearchCompany;
    private ComboBox<InsuranceCompany> companySearchCombo;
    private Label selectedCompanyLabel;

    // For regular users - simple combo
    private ComboBox<Vehicle> vehicleCombo;

    public VBox createView(User user) {
        this.currentUser = user;

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f4f6f9;");

        // Form Panel at the top
        TitledPane formPane = createFormPane();

        // Table Panel below
        VBox tableContainer = createTableContainer();

        root.getChildren().addAll(formPane, tableContainer);

        loadData();

        return root;
    }

    private TitledPane createFormPane() {
        TitledPane titledPane = new TitledPane();
        titledPane.setText("Add New Insurance Policy");
        titledPane.setCollapsible(true);
        titledPane.setExpanded(true);
        titledPane.setStyle("-fx-font-weight: bold; -fx-background-color: white; -fx-background-radius: 12;");

        // Main container for left and right sections
        HBox mainContainer = new HBox(30);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");

        // LEFT SIDE - Search sections (Vehicle + Insurance Company)
        VBox leftSection = createLeftSection();

        // RIGHT SIDE - Policy Details
        VBox rightSection = createRightSection();

        // Set widths
        leftSection.setPrefWidth(350);
        rightSection.setPrefWidth(400);
        HBox.setHgrow(rightSection, Priority.ALWAYS);

        mainContainer.getChildren().addAll(leftSection, rightSection);

        titledPane.setContent(mainContainer);
        return titledPane;
    }

    private VBox createLeftSection() {
        VBox leftSection = new VBox(15);
        leftSection.setPadding(new Insets(0, 15, 0, 0));

        if (currentUser.getRole().equals("ADMIN")) {
            // Vehicle Search Section
            VBox vehicleSection = new VBox(8);
            Label vehicleLabel = new Label("Vehicle Information");
            vehicleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1f2937;");
            vehicleSection.getChildren().add(vehicleLabel);

            // Search row
            HBox searchRow = new HBox(8);
            txtSearchVehicle = new TextField();
            txtSearchVehicle.setPromptText("Enter registration number");
            txtSearchVehicle.setPrefWidth(180);
            Button btnSearchVehicle = new Button("Search");
            btnSearchVehicle.setStyle("-fx-background-color: #1d3557; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 6; -fx-cursor: hand;");
            btnSearchVehicle.setOnAction(e -> searchVehicle());
            searchRow.getChildren().addAll(txtSearchVehicle, btnSearchVehicle);
            vehicleSection.getChildren().add(searchRow);

            // Select combo
            vehicleSearchCombo = new ComboBox<>();
            vehicleSearchCombo.setPromptText("Select from results");
            vehicleSearchCombo.setPrefWidth(280);
            vehicleSearchCombo.setVisible(false);
            vehicleSearchCombo.setOnAction(e -> {
                if (vehicleSearchCombo.getValue() != null) {
                    selectedVehicleLabel.setText("Selected: " + vehicleSearchCombo.getValue().getRegistrationNumber());
                }
            });
            vehicleSection.getChildren().add(vehicleSearchCombo);

            selectedVehicleLabel = new Label("No vehicle selected");
            selectedVehicleLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");
            vehicleSection.getChildren().add(selectedVehicleLabel);

            leftSection.getChildren().add(vehicleSection);

            // Separator
            Separator separator = new Separator();
            separator.setPadding(new Insets(10, 0, 10, 0));
            leftSection.getChildren().add(separator);

            // Insurance Company Search Section
            VBox companySection = new VBox(8);
            Label companyLabel = new Label("Insurance Company");
            companyLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1f2937;");
            companySection.getChildren().add(companyLabel);

            HBox companySearchRow = new HBox(8);
            txtSearchCompany = new TextField();
            txtSearchCompany.setPromptText("Enter company name");
            txtSearchCompany.setPrefWidth(180);
            Button btnSearchCompany = new Button("Search");
            btnSearchCompany.setStyle("-fx-background-color: #1d3557; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 6; -fx-cursor: hand;");
            btnSearchCompany.setOnAction(e -> searchCompany());
            companySearchRow.getChildren().addAll(txtSearchCompany, btnSearchCompany);
            companySection.getChildren().add(companySearchRow);

            companySearchCombo = new ComboBox<>();
            companySearchCombo.setPromptText("Select from results");
            companySearchCombo.setPrefWidth(280);
            companySearchCombo.setVisible(false);
            companySearchCombo.setOnAction(e -> {
                if (companySearchCombo.getValue() != null) {
                    selectedCompanyLabel.setText("Selected: " + companySearchCombo.getValue().getName());
                }
            });
            companySection.getChildren().add(companySearchCombo);

            selectedCompanyLabel = new Label("No company selected");
            selectedCompanyLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");
            companySection.getChildren().add(selectedCompanyLabel);

            leftSection.getChildren().add(companySection);
        } else {
            // For regular INSURANCE user
            VBox vehicleSection = new VBox(8);
            Label vehicleLabel = new Label("Vehicle Information");
            vehicleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1f2937;");
            vehicleSection.getChildren().add(vehicleLabel);

            vehicleCombo = new ComboBox<>();
            vehicleCombo.setPromptText("Select vehicle");
            vehicleCombo.setPrefWidth(280);
            vehicleSection.getChildren().add(vehicleCombo);

            leftSection.getChildren().add(vehicleSection);
        }

        return leftSection;
    }

    private VBox createRightSection() {
        VBox rightSection = new VBox(12);
        rightSection.setPadding(new Insets(0, 0, 0, 15));
        rightSection.setStyle("-fx-border-color: #e5e7eb; -fx-border-width: 0 0 0 1;");

        Label policyLabel = new Label("Policy Details");
        policyLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1f2937;");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(12);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(10, 0, 0, 0));

        int row = 0;

        // Policy Number
        formGrid.add(new Label("Policy Number:"), 0, row);
        txtPolicyNumber = new TextField();
        txtPolicyNumber.setPromptText("Enter policy number");
        txtPolicyNumber.setPrefWidth(220);
        formGrid.add(txtPolicyNumber, 1, row);
        row++;

        // Provider
        formGrid.add(new Label("Provider:"), 0, row);
        txtProvider = new TextField();
        txtProvider.setPromptText("Enter provider name");
        txtProvider.setPrefWidth(220);
        formGrid.add(txtProvider, 1, row);
        row++;

        // Insurance Type
        formGrid.add(new Label("Insurance Type:"), 0, row);
        insuranceTypeCombo = new ComboBox<>();
        insuranceTypeCombo.getItems().addAll("Comprehensive", "Third Party", "Extended Warranty", "Roadside", "Gap");
        insuranceTypeCombo.setValue("Comprehensive");
        insuranceTypeCombo.setPrefWidth(220);
        formGrid.add(insuranceTypeCombo, 1, row);
        row++;

        // Premium Amount
        formGrid.add(new Label("Premium (M):"), 0, row);
        txtPremiumAmount = new TextField();
        txtPremiumAmount.setPromptText("Enter amount");
        txtPremiumAmount.setPrefWidth(150);
        formGrid.add(txtPremiumAmount, 1, row);
        row++;

        // Start Date
        formGrid.add(new Label("Start Date:"), 0, row);
        startDatePicker = new DatePicker(LocalDate.now());
        startDatePicker.setPrefWidth(150);
        formGrid.add(startDatePicker, 1, row);
        row++;

        // Expiry Date
        formGrid.add(new Label("Expiry Date:"), 0, row);
        expiryDatePicker = new DatePicker(LocalDate.now().plusYears(1));
        expiryDatePicker.setPrefWidth(150);
        formGrid.add(expiryDatePicker, 1, row);
        row++;

        // Status
        formGrid.add(new Label("Status:"), 0, row);
        statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Active", "Expired", "Cancelled");
        statusCombo.setValue("Active");
        statusCombo.setPrefWidth(150);
        formGrid.add(statusCombo, 1, row);
        row++;

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(15, 0, 5, 0));

        Button btnAdd = new Button("Add Insurance");
        btnAdd.setStyle("-fx-background-color: #1d3557; -fx-text-fill: white; -fx-font-weight: 500; -fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand;");
        btnAdd.setOnMouseEntered(e -> btnAdd.setStyle("-fx-background-color: #457b9d; -fx-text-fill: white; -fx-font-weight: 500; -fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand;"));
        btnAdd.setOnMouseExited(e -> btnAdd.setStyle("-fx-background-color: #1d3557; -fx-text-fill: white; -fx-font-weight: 500; -fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand;"));
        btnAdd.setOnAction(e -> handleAdd());

        Button btnRefresh = new Button("Refresh");
        btnRefresh.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #333; -fx-font-weight: 500; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
        btnRefresh.setOnMouseEntered(e -> btnRefresh.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333; -fx-font-weight: 500; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;"));
        btnRefresh.setOnMouseExited(e -> btnRefresh.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #333; -fx-font-weight: 500; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;"));
        btnRefresh.setOnAction(e -> loadData());

        buttonBox.getChildren().addAll(btnAdd, btnRefresh);
        formGrid.add(buttonBox, 0, row, 2, 1);

        rightSection.getChildren().addAll(policyLabel, formGrid);

        return rightSection;
    }

    private VBox createTableContainer() {
        VBox tableContainer = new VBox(10);
        tableContainer.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");
        tableContainer.setPadding(new Insets(15));

        Label tableTitle = new Label("Insurance Policies");
        tableTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

        insuranceTable = new TableView<>();
        insuranceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        insuranceTable.setPlaceholder(new Label("No insurance policies found"));
        createTableColumns();

        VBox.setVgrow(insuranceTable, Priority.ALWAYS);

        tableContainer.getChildren().addAll(tableTitle, insuranceTable);

        return tableContainer;
    }

    private void createTableColumns() {
        TableColumn<Insurance, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("insuranceId"));
        idCol.setPrefWidth(50);

        TableColumn<Insurance, String> vehicleCol = new TableColumn<>("Vehicle");
        vehicleCol.setCellValueFactory(new PropertyValueFactory<>("vehicleReg"));
        vehicleCol.setPrefWidth(120);

        TableColumn<Insurance, String> policyCol = new TableColumn<>("Policy Number");
        policyCol.setCellValueFactory(new PropertyValueFactory<>("policyNumber"));
        policyCol.setPrefWidth(150);

        TableColumn<Insurance, String> providerCol = new TableColumn<>("Provider");
        providerCol.setCellValueFactory(new PropertyValueFactory<>("provider"));
        providerCol.setPrefWidth(150);

        TableColumn<Insurance, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("insuranceType"));
        typeCol.setPrefWidth(100);

        TableColumn<Insurance, LocalDate> startCol = new TableColumn<>("Start Date");
        startCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        startCol.setPrefWidth(100);

        TableColumn<Insurance, LocalDate> expiryCol = new TableColumn<>("Expiry Date");
        expiryCol.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
        expiryCol.setPrefWidth(100);

        TableColumn<Insurance, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(80);

        TableColumn<Insurance, Double> premiumCol = new TableColumn<>("Premium (M)");
        premiumCol.setCellValueFactory(new PropertyValueFactory<>("premiumAmount"));
        premiumCol.setPrefWidth(100);

        TableColumn<Insurance, String> companyCol = new TableColumn<>("Insurance Company");
        companyCol.setCellValueFactory(new PropertyValueFactory<>("companyName"));
        companyCol.setPrefWidth(150);

        insuranceTable.getColumns().addAll(idCol, vehicleCol, policyCol, providerCol, typeCol,
                startCol, expiryCol, statusCol, premiumCol, companyCol);
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

    private void searchCompany() {
        String searchText = txtSearchCompany.getText().trim();
        if (searchText.isEmpty()) {
            AlertUtils.showWarning("Missing Data", "Please enter company name to search.");
            return;
        }

        List<InsuranceCompany> companies = new ArrayList<>();
        String sql = "SELECT company_id, name, registration_number, phone, email, address FROM insurance_company WHERE name ILIKE ?";

        try (PreparedStatement pstmt = currentUser.getDbConnection().prepareStatement(sql)) {
            String pattern = "%" + searchText + "%";
            pstmt.setString(1, pattern);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                InsuranceCompany company = new InsuranceCompany(
                        rs.getInt("company_id"),
                        rs.getString("name"),
                        rs.getString("registration_number"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("address")
                );
                companies.add(company);
            }
        } catch (SQLException e) {
            AlertUtils.showError("Database Error", "Failed to search companies: " + e.getMessage());
        }

        if (companies.isEmpty()) {
            AlertUtils.showWarning("No Results", "No insurance companies found matching: " + searchText);
            companySearchCombo.setVisible(false);
        } else {
            companySearchCombo.setItems(FXCollections.observableArrayList(companies));
            companySearchCombo.setCellFactory(lv -> new ListCell<InsuranceCompany>() {
                @Override
                protected void updateItem(InsuranceCompany item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getName() + " (Reg: " + item.getRegistrationNumber() + ")");
                }
            });
            companySearchCombo.setVisible(true);
            companySearchCombo.setPromptText("Select company (" + companies.size() + " found)");
        }
    }

    private void loadData() {
        List<Insurance> insuranceList = InsuranceService.getAllInsurance(currentUser);
        insuranceTable.setItems(FXCollections.observableArrayList(insuranceList));

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

    private void handleAdd() {
        try {
            int vehicleId;
            String vehicleReg;
            int companyId = -1;
            String companyName = null;

            if (currentUser.getRole().equals("ADMIN")) {
                Vehicle selectedVehicle = vehicleSearchCombo.getValue();
                if (selectedVehicle == null) {
                    AlertUtils.showWarning("Missing Data", "Please search and select a vehicle.");
                    return;
                }
                vehicleId = selectedVehicle.getVehicleId();
                vehicleReg = selectedVehicle.getRegistrationNumber();

                InsuranceCompany selectedCompany = companySearchCombo.getValue();
                if (selectedCompany == null) {
                    AlertUtils.showWarning("Missing Data", "Please search and select an insurance company.");
                    return;
                }
                companyId = selectedCompany.getCompanyId();
                companyName = selectedCompany.getName();
            } else {
                Vehicle selectedVehicle = vehicleCombo.getValue();
                if (selectedVehicle == null) {
                    AlertUtils.showWarning("Missing Data", "Please select a vehicle.");
                    return;
                }
                vehicleId = selectedVehicle.getVehicleId();
                vehicleReg = selectedVehicle.getRegistrationNumber();
            }

            Insurance insurance = new Insurance();
            insurance.setVehicleId(vehicleId);
            insurance.setVehicleReg(vehicleReg);
            insurance.setPolicyNumber(txtPolicyNumber.getText());
            insurance.setProvider(txtProvider.getText());
            insurance.setInsuranceType(insuranceTypeCombo.getValue());
            insurance.setStartDate(startDatePicker.getValue());
            insurance.setExpiryDate(expiryDatePicker.getValue());
            insurance.setStatus(statusCombo.getValue());
            insurance.setPremiumAmount(Double.parseDouble(txtPremiumAmount.getText()));

            if (companyId > 0) {
                insurance.setCompanyId(companyId);
                insurance.setCompanyName(companyName);
            }

            if (InsuranceService.addInsurance(currentUser, insurance)) {
                clearForm();
                loadData();
            }
        } catch (NumberFormatException e) {
            AlertUtils.showError("Invalid Input", "Please enter a valid premium amount.");
        } catch (Exception e) {
            AlertUtils.showError("Error", "Failed to add insurance: " + e.getMessage());
        }
    }

    private void clearForm() {
        txtPolicyNumber.clear();
        txtProvider.clear();
        txtPremiumAmount.clear();
        insuranceTypeCombo.setValue("Comprehensive");
        startDatePicker.setValue(LocalDate.now());
        expiryDatePicker.setValue(LocalDate.now().plusYears(1));
        statusCombo.setValue("Active");

        if (currentUser.getRole().equals("ADMIN")) {
            txtSearchVehicle.clear();
            if (vehicleSearchCombo != null) {
                vehicleSearchCombo.setItems(null);
                vehicleSearchCombo.setVisible(false);
            }
            selectedVehicleLabel.setText("No vehicle selected");
            txtSearchCompany.clear();
            if (companySearchCombo != null) {
                companySearchCombo.setItems(null);
                companySearchCombo.setVisible(false);
            }
            selectedCompanyLabel.setText("No company selected");
        } else {
            vehicleCombo.setValue(null);
        }
    }

    // Inner class for InsuranceCompany
    public static class InsuranceCompany {
        private final int companyId;
        private final String name;
        private final String registrationNumber;
        private final String phone;
        private final String email;
        private final String address;

        public InsuranceCompany(int companyId, String name, String registrationNumber,
                                String phone, String email, String address) {
            this.companyId = companyId;
            this.name = name;
            this.registrationNumber = registrationNumber;
            this.phone = phone;
            this.email = email;
            this.address = address;
        }

        public int getCompanyId() { return companyId; }
        public String getName() { return name; }
        public String getRegistrationNumber() { return registrationNumber; }
        public String getPhone() { return phone; }
        public String getEmail() { return email; }
        public String getAddress() { return address; }
    }
}