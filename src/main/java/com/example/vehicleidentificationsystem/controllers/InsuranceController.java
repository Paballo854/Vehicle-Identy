package com.example.vehicleidentificationsystem.controllers;

import com.example.vehicleidentificationsystem.models.*;
import com.example.vehicleidentificationsystem.services.InsuranceService;
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

        Label title = new Label("Insurance Management");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        insuranceTable = new TableView<>();
        insuranceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        createTableColumns();

        VBox formPanel = createFormPanel();

        root.getChildren().addAll(title, formPanel, insuranceTable);

        loadData();

        return root;
    }

    private void createTableColumns() {
        TableColumn<Insurance, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("insuranceId"));

        TableColumn<Insurance, String> vehicleCol = new TableColumn<>("Vehicle");
        vehicleCol.setCellValueFactory(new PropertyValueFactory<>("vehicleReg"));

        TableColumn<Insurance, String> policyCol = new TableColumn<>("Policy Number");
        policyCol.setCellValueFactory(new PropertyValueFactory<>("policyNumber"));

        TableColumn<Insurance, String> providerCol = new TableColumn<>("Provider");
        providerCol.setCellValueFactory(new PropertyValueFactory<>("provider"));

        TableColumn<Insurance, String> typeCol = new TableColumn<>("Insurance Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("insuranceType"));

        TableColumn<Insurance, LocalDate> startCol = new TableColumn<>("Start Date");
        startCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));

        TableColumn<Insurance, LocalDate> expiryCol = new TableColumn<>("Expiry Date");
        expiryCol.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));

        TableColumn<Insurance, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<Insurance, Double> premiumCol = new TableColumn<>("Premium (M)");
        premiumCol.setCellValueFactory(new PropertyValueFactory<>("premiumAmount"));

        TableColumn<Insurance, String> companyCol = new TableColumn<>("Insurance Company");
        companyCol.setCellValueFactory(new PropertyValueFactory<>("companyName"));

        insuranceTable.getColumns().addAll(idCol, vehicleCol, policyCol, providerCol, typeCol,
                startCol, expiryCol, statusCol, premiumCol, companyCol);
    }

    private VBox createFormPanel() {
        VBox formPanel = new VBox(10);
        formPanel.setPadding(new Insets(10));

        Label formTitle = new Label("Insurance Record");
        formTitle.setStyle("-fx-font-weight: bold;");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(8);

        int row = 0;

        if (currentUser.getRole().equals("ADMIN")) {
            // Vehicle search for ADMIN
            formGrid.add(new Label("Vehicle (Search):"), 0, row);
            txtSearchVehicle = new TextField();
            txtSearchVehicle.setPromptText("Enter registration number to search");
            txtSearchVehicle.setPrefWidth(180);
            Button btnSearchVehicle = new Button("Search");
            btnSearchVehicle.setOnAction(e -> searchVehicle());
            HBox searchVehicleBox = new HBox(5, txtSearchVehicle, btnSearchVehicle);
            formGrid.add(searchVehicleBox, 1, row++);

            formGrid.add(new Label("Select Vehicle:"), 0, row);
            vehicleSearchCombo = new ComboBox<>();
            vehicleSearchCombo.setPromptText("Search results will appear here");
            vehicleSearchCombo.setPrefWidth(300);
            vehicleSearchCombo.setVisible(false);
            formGrid.add(vehicleSearchCombo, 1, row++);

            selectedVehicleLabel = new Label("No vehicle selected");
            formGrid.add(selectedVehicleLabel, 1, row++);

            // Company search for ADMIN
            formGrid.add(new Label("Insurance Company (Search):"), 0, row);
            txtSearchCompany = new TextField();
            txtSearchCompany.setPromptText("Enter company name to search");
            txtSearchCompany.setPrefWidth(180);
            Button btnSearchCompany = new Button("Search");
            btnSearchCompany.setOnAction(e -> searchCompany());
            HBox searchCompanyBox = new HBox(5, txtSearchCompany, btnSearchCompany);
            formGrid.add(searchCompanyBox, 1, row++);

            formGrid.add(new Label("Select Company:"), 0, row);
            companySearchCombo = new ComboBox<>();
            companySearchCombo.setPromptText("Search results will appear here");
            companySearchCombo.setPrefWidth(300);
            companySearchCombo.setVisible(false);
            formGrid.add(companySearchCombo, 1, row++);

            selectedCompanyLabel = new Label("No company selected");
            formGrid.add(selectedCompanyLabel, 1, row++);
        } else {
            // For regular INSURANCE user
            formGrid.add(new Label("Vehicle:"), 0, row);
            vehicleCombo = new ComboBox<>();
            vehicleCombo.setPrefWidth(250);
            formGrid.add(vehicleCombo, 1, row++);
        }

        formGrid.add(new Label("Policy Number:"), 0, row);
        txtPolicyNumber = new TextField();
        formGrid.add(txtPolicyNumber, 1, row++);

        formGrid.add(new Label("Provider:"), 0, row);
        txtProvider = new TextField();
        formGrid.add(txtProvider, 1, row++);

        formGrid.add(new Label("Insurance Type:"), 0, row);
        insuranceTypeCombo = new ComboBox<>();
        insuranceTypeCombo.getItems().addAll("Comprehensive", "Third Party", "Extended Warranty", "Roadside", "Gap");
        insuranceTypeCombo.setValue("Comprehensive");
        formGrid.add(insuranceTypeCombo, 1, row++);

        formGrid.add(new Label("Premium (M):"), 0, row);
        txtPremiumAmount = new TextField();
        formGrid.add(txtPremiumAmount, 1, row++);

        formGrid.add(new Label("Start Date:"), 0, row);
        startDatePicker = new DatePicker(LocalDate.now());
        formGrid.add(startDatePicker, 1, row++);

        formGrid.add(new Label("Expiry Date:"), 0, row);
        expiryDatePicker = new DatePicker(LocalDate.now().plusYears(1));
        formGrid.add(expiryDatePicker, 1, row++);

        formGrid.add(new Label("Status:"), 0, row);
        statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Active", "Expired", "Cancelled");
        statusCombo.setValue("Active");
        formGrid.add(statusCombo, 1, row++);

        HBox buttonBox = new HBox(10);
        Button btnAdd = new Button("Add Insurance");
        btnAdd.setOnAction(e -> handleAdd());
        Button btnRefresh = new Button("Refresh");
        btnRefresh.setOnAction(e -> loadData());
        buttonBox.getChildren().addAll(btnAdd, btnRefresh);

        formPanel.getChildren().addAll(formTitle, formGrid, buttonBox);

        return formPanel;
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

            vehicleSearchCombo.setOnAction(e -> {
                Vehicle selected = vehicleSearchCombo.getValue();
                if (selected != null) {
                    selectedVehicleLabel.setText("Selected: " + selected.getRegistrationNumber());
                }
            });
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

            companySearchCombo.setOnAction(e -> {
                InsuranceCompany selected = companySearchCombo.getValue();
                if (selected != null) {
                    selectedCompanyLabel.setText("Selected: " + selected.getName());
                }
            });
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