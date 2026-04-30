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
import java.util.ArrayList;
import java.util.List;

public class VehicleController {

    private TableView<Vehicle> vehicleTable;
    private Button btnAdd, btnEdit, btnDelete, btnRefresh;
    private TextField txtReg, txtMake, txtModel, txtYear;
    private User currentUser;

    // For ADMIN - search customer fields
    private TextField txtSearchCustomer;
    private ComboBox<Customer> customerSearchCombo;
    private Label selectedCustomerLabel;

    // For regular users - simple combo
    private ComboBox<Customer> customerComboBox;

    public VBox createView(User user) {
        this.currentUser = user;

        // Main container with 40/60 split
        HBox mainContainer = new HBox(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setAlignment(Pos.TOP_CENTER);

        // LEFT SIDE (40%) - Form with buttons
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

        // Button Panel at the top of left side
        HBox buttonPanel = createButtonPanel();

        // Form Panel
        VBox formPanel = createFormPanel();

        leftPanel.getChildren().addAll(buttonPanel, formPanel);

        return leftPanel;
    }

    private VBox createRightPanel() {
        VBox rightPanel = new VBox(10);
        rightPanel.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");
        rightPanel.setPadding(new Insets(15));

        Label tableTitle = new Label("Vehicles");
        tableTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

        vehicleTable = new TableView<>();
        vehicleTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        vehicleTable.setPlaceholder(new Label("No vehicles found"));
        createTableColumns();

        VBox.setVgrow(vehicleTable, Priority.ALWAYS);

        rightPanel.getChildren().addAll(tableTitle, vehicleTable);

        return rightPanel;
    }

    private HBox createButtonPanel() {
        HBox buttonPanel = new HBox(8);
        buttonPanel.setAlignment(Pos.CENTER_LEFT);
        buttonPanel.setPadding(new Insets(0, 0, 15, 0));

        btnAdd = new Button("Add");
        btnAdd.setStyle("-fx-background-color: #1d3557; -fx-text-fill: white; -fx-font-weight: 500; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
        btnAdd.setOnMouseEntered(e -> btnAdd.setStyle("-fx-background-color: #457b9d; -fx-text-fill: white; -fx-font-weight: 500; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;"));
        btnAdd.setOnMouseExited(e -> btnAdd.setStyle("-fx-background-color: #1d3557; -fx-text-fill: white; -fx-font-weight: 500; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;"));

        btnEdit = new Button("Edit");
        btnEdit.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #333; -fx-font-weight: 500; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
        btnEdit.setOnMouseEntered(e -> btnEdit.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333; -fx-font-weight: 500; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;"));
        btnEdit.setOnMouseExited(e -> btnEdit.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #333; -fx-font-weight: 500; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;"));

        btnDelete = new Button("Delete");
        btnDelete.setStyle("-fx-background-color: #e63946; -fx-text-fill: white; -fx-font-weight: 500; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
        btnDelete.setOnMouseEntered(e -> btnDelete.setStyle("-fx-background-color: #c1121f; -fx-text-fill: white; -fx-font-weight: 500; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;"));
        btnDelete.setOnMouseExited(e -> btnDelete.setStyle("-fx-background-color: #e63946; -fx-text-fill: white; -fx-font-weight: 500; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;"));

        btnRefresh = new Button("Refresh");
        btnRefresh.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #333; -fx-font-weight: 500; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
        btnRefresh.setOnMouseEntered(e -> btnRefresh.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333; -fx-font-weight: 500; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;"));
        btnRefresh.setOnMouseExited(e -> btnRefresh.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #333; -fx-font-weight: 500; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;"));

        btnAdd.setOnAction(e -> handleAdd());
        btnEdit.setOnAction(e -> handleEdit());
        btnDelete.setOnAction(e -> handleDelete());
        btnRefresh.setOnAction(e -> loadData());

        buttonPanel.getChildren().addAll(btnAdd, btnEdit, btnDelete, btnRefresh);

        return buttonPanel;
    }

    private VBox createFormPanel() {
        VBox formPanel = new VBox(12);

        Label formTitle = new Label("Vehicle Details");
        formTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(12);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(10, 0, 0, 0));

        int row = 0;

        // Registration
        formGrid.add(new Label("Registration:"), 0, row);
        txtReg = new TextField();
        txtReg.setPromptText("e.g., LES-001-TT");
        txtReg.setPrefWidth(200);
        formGrid.add(txtReg, 1, row);
        row++;

        // Make
        formGrid.add(new Label("Make:"), 0, row);
        txtMake = new TextField();
        txtMake.setPromptText("e.g., Toyota");
        txtMake.setPrefWidth(200);
        formGrid.add(txtMake, 1, row);
        row++;

        // Model
        formGrid.add(new Label("Model:"), 0, row);
        txtModel = new TextField();
        txtModel.setPromptText("e.g., Hilux");
        txtModel.setPrefWidth(200);
        formGrid.add(txtModel, 1, row);
        row++;

        // Year
        formGrid.add(new Label("Year:"), 0, row);
        txtYear = new TextField();
        txtYear.setPromptText("e.g., 2020");
        txtYear.setPrefWidth(120);
        formGrid.add(txtYear, 1, row);
        row++;

        // Owner Section
        formGrid.add(new Label("Owner:"), 0, row);

        if (currentUser.getRole().equals("ADMIN")) {
            // For ADMIN: Search customer by name
            HBox searchBox = new HBox(8);
            txtSearchCustomer = new TextField();
            txtSearchCustomer.setPromptText("Enter customer name");
            txtSearchCustomer.setPrefWidth(180);
            Button btnSearchCustomer = new Button("Search");
            btnSearchCustomer.setStyle("-fx-background-color: #1d3557; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 6;");
            btnSearchCustomer.setOnAction(e -> searchCustomer());
            searchBox.getChildren().addAll(txtSearchCustomer, btnSearchCustomer);
            formGrid.add(searchBox, 1, row);
            row++;

            formGrid.add(new Label("Select:"), 0, row);
            customerSearchCombo = new ComboBox<>();
            customerSearchCombo.setPromptText("Select from results");
            customerSearchCombo.setPrefWidth(250);
            customerSearchCombo.setVisible(false);
            customerSearchCombo.setOnAction(e -> {
                if (customerSearchCombo.getValue() != null) {
                    selectedCustomerLabel.setText("Selected: " + customerSearchCombo.getValue().getName());
                }
            });
            formGrid.add(customerSearchCombo, 1, row);
            row++;

            selectedCustomerLabel = new Label("No customer selected");
            selectedCustomerLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");
            formGrid.add(selectedCustomerLabel, 1, row);
        } else {
            // For regular user
            customerComboBox = new ComboBox<>();
            customerComboBox.setPromptText("Select owner");
            customerComboBox.setPrefWidth(200);
            formGrid.add(customerComboBox, 1, row);
        }

        formPanel.getChildren().addAll(formTitle, formGrid);

        return formPanel;
    }

    private void createTableColumns() {
        TableColumn<Vehicle, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("vehicleId"));
        idCol.setPrefWidth(50);

        TableColumn<Vehicle, String> regCol = new TableColumn<>("Registration");
        regCol.setCellValueFactory(new PropertyValueFactory<>("registrationNumber"));
        regCol.setPrefWidth(120);

        TableColumn<Vehicle, String> makeCol = new TableColumn<>("Make");
        makeCol.setCellValueFactory(new PropertyValueFactory<>("make"));
        makeCol.setPrefWidth(100);

        TableColumn<Vehicle, String> modelCol = new TableColumn<>("Model");
        modelCol.setCellValueFactory(new PropertyValueFactory<>("model"));
        modelCol.setPrefWidth(100);

        TableColumn<Vehicle, Integer> yearCol = new TableColumn<>("Year");
        yearCol.setCellValueFactory(new PropertyValueFactory<>("year"));
        yearCol.setPrefWidth(80);

        TableColumn<Vehicle, String> ownerCol = new TableColumn<>("Owner");
        ownerCol.setCellValueFactory(new PropertyValueFactory<>("ownerName"));
        ownerCol.setPrefWidth(120);

        vehicleTable.getColumns().addAll(idCol, regCol, makeCol, modelCol, yearCol, ownerCol);

        vehicleTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                populateForm(selected);
            }
        });
    }

    private void searchCustomer() {
        String searchText = txtSearchCustomer.getText().trim();
        if (searchText.isEmpty()) {
            AlertUtils.showWarning("Missing Data", "Please enter customer name to search.");
            return;
        }

        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT customer_id, name, address, phone, email FROM customer WHERE name ILIKE ?";

        try (PreparedStatement pstmt = currentUser.getDbConnection().prepareStatement(sql)) {
            String pattern = "%" + searchText + "%";
            pstmt.setString(1, pattern);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                customers.add(new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("phone"),
                        rs.getString("email")
                ));
            }
        } catch (SQLException e) {
            AlertUtils.showError("Database Error", "Failed to search customers: " + e.getMessage());
        }

        if (customers.isEmpty()) {
            AlertUtils.showWarning("No Results", "No customers found matching: " + searchText);
            customerSearchCombo.setVisible(false);
        } else {
            customerSearchCombo.setItems(FXCollections.observableArrayList(customers));
            customerSearchCombo.setCellFactory(lv -> new ListCell<Customer>() {
                @Override
                protected void updateItem(Customer item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getName());
                }
            });
            customerSearchCombo.setVisible(true);
            customerSearchCombo.setPromptText("Select customer (" + customers.size() + " found)");
        }
    }

    private void loadData() {
        List<Vehicle> vehicles = VehicleService.getAllVehicles(currentUser);
        vehicleTable.setItems(FXCollections.observableArrayList(vehicles));

        if (!currentUser.getRole().equals("ADMIN")) {
            List<Customer> customers = VehicleService.getAllCustomers(currentUser);
            customerComboBox.setItems(FXCollections.observableArrayList(customers));
            customerComboBox.setCellFactory(lv -> new ListCell<Customer>() {
                @Override
                protected void updateItem(Customer item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getName());
                }
            });
        }
    }

    private void populateForm(Vehicle vehicle) {
        txtReg.setText(vehicle.getRegistrationNumber());
        txtMake.setText(vehicle.getMake());
        txtModel.setText(vehicle.getModel());
        txtYear.setText(String.valueOf(vehicle.getYear()));

        if (currentUser.getRole().equals("ADMIN")) {
            if (vehicle.getOwnerName() != null && !vehicle.getOwnerName().isEmpty()) {
                selectedCustomerLabel.setText("Selected: " + vehicle.getOwnerName());
            } else {
                selectedCustomerLabel.setText("No customer selected");
            }
        } else {
            for (Customer c : customerComboBox.getItems()) {
                if (c.getCustomerId() == vehicle.getOwnerId()) {
                    customerComboBox.setValue(c);
                    break;
                }
            }
        }
    }

    private void clearForm() {
        txtReg.clear();
        txtMake.clear();
        txtModel.clear();
        txtYear.clear();

        if (currentUser.getRole().equals("ADMIN")) {
            txtSearchCustomer.clear();
            if (customerSearchCombo != null) {
                customerSearchCombo.setItems(null);
                customerSearchCombo.setVisible(false);
            }
            selectedCustomerLabel.setText("No customer selected");
        } else {
            customerComboBox.setValue(null);
        }

        vehicleTable.getSelectionModel().clearSelection();
    }

    private void handleAdd() {
        try {
            Vehicle vehicle = new Vehicle();
            vehicle.setRegistrationNumber(txtReg.getText());
            vehicle.setMake(txtMake.getText());
            vehicle.setModel(txtModel.getText());
            vehicle.setYear(Integer.parseInt(txtYear.getText()));

            int ownerId = -1;

            if (currentUser.getRole().equals("ADMIN")) {
                Customer selected = customerSearchCombo.getValue();
                if (selected == null) {
                    AlertUtils.showWarning("Missing Data", "Please search and select a customer.");
                    return;
                }
                ownerId = selected.getCustomerId();
            } else {
                Customer selected = customerComboBox.getValue();
                if (selected != null) {
                    ownerId = selected.getCustomerId();
                }
            }

            if (ownerId > 0) {
                vehicle.setOwnerId(ownerId);
            }

            if (VehicleService.addVehicle(currentUser, vehicle)) {
                clearForm();
                loadData();
            }
        } catch (NumberFormatException e) {
            AlertUtils.showError("Invalid Input", "Please enter a valid year.");
        } catch (Exception e) {
            AlertUtils.showError("Error", "Failed to add vehicle: " + e.getMessage());
        }
    }

    private void handleEdit() {
        Vehicle selected = vehicleTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("No Selection", "Please select a vehicle to edit.");
            return;
        }

        try {
            selected.setRegistrationNumber(txtReg.getText());
            selected.setMake(txtMake.getText());
            selected.setModel(txtModel.getText());
            selected.setYear(Integer.parseInt(txtYear.getText()));

            int ownerId = -1;

            if (currentUser.getRole().equals("ADMIN")) {
                Customer selectedCustomer = customerSearchCombo.getValue();
                if (selectedCustomer != null) {
                    ownerId = selectedCustomer.getCustomerId();
                }
            } else {
                Customer selectedCustomer = customerComboBox.getValue();
                if (selectedCustomer != null) {
                    ownerId = selectedCustomer.getCustomerId();
                }
            }

            if (ownerId > 0) {
                selected.setOwnerId(ownerId);
            }

            if (VehicleService.updateVehicle(currentUser, selected)) {
                clearForm();
                loadData();
            }
        } catch (NumberFormatException e) {
            AlertUtils.showError("Invalid Input", "Please enter a valid year.");
        } catch (Exception e) {
            AlertUtils.showError("Error", "Failed to edit vehicle: " + e.getMessage());
        }
    }

    private void handleDelete() {
        Vehicle selected = vehicleTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("No Selection", "Please select a vehicle to delete.");
            return;
        }

        if (AlertUtils.showConfirmation("Confirm Delete", "Delete vehicle " + selected.getRegistrationNumber() + "?")) {
            if (VehicleService.deleteVehicle(currentUser, selected.getVehicleId())) {
                clearForm();
                loadData();
            }
        }
    }
}