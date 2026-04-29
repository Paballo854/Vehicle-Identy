package com.example.vehicleidentificationsystem.controllers;

import com.example.vehicleidentificationsystem.models.*;
import com.example.vehicleidentificationsystem.services.VehicleService;
import com.example.vehicleidentificationsystem.utils.AlertUtils;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
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

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        Label title = new Label("Vehicle Management");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        vehicleTable = new TableView<>();
        vehicleTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        vehicleTable.setPlaceholder(new Label("No vehicles found"));

        createTableColumns();

        VBox formPanel = createFormPanel();
        HBox buttonPanel = createButtonPanel();

        root.getChildren().addAll(title, buttonPanel, formPanel, vehicleTable);

        loadData();

        return root;
    }

    private void createTableColumns() {
        TableColumn<Vehicle, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("vehicleId"));

        TableColumn<Vehicle, String> regCol = new TableColumn<>("Registration");
        regCol.setCellValueFactory(new PropertyValueFactory<>("registrationNumber"));

        TableColumn<Vehicle, String> makeCol = new TableColumn<>("Make");
        makeCol.setCellValueFactory(new PropertyValueFactory<>("make"));

        TableColumn<Vehicle, String> modelCol = new TableColumn<>("Model");
        modelCol.setCellValueFactory(new PropertyValueFactory<>("model"));

        TableColumn<Vehicle, Integer> yearCol = new TableColumn<>("Year");
        yearCol.setCellValueFactory(new PropertyValueFactory<>("year"));

        TableColumn<Vehicle, String> ownerCol = new TableColumn<>("Owner");
        ownerCol.setCellValueFactory(new PropertyValueFactory<>("ownerName"));

        vehicleTable.getColumns().addAll(idCol, regCol, makeCol, modelCol, yearCol, ownerCol);

        vehicleTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                populateForm(selected);
            }
        });
    }

    private VBox createFormPanel() {
        VBox formPanel = new VBox(10);
        formPanel.setPadding(new Insets(10));

        Label formTitle = new Label("Vehicle Details");
        formTitle.setStyle("-fx-font-weight: bold;");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(8);

        int row = 0;

        formGrid.add(new Label("Registration:"), 0, row);
        txtReg = new TextField();
        formGrid.add(txtReg, 1, row++);

        formGrid.add(new Label("Make:"), 0, row);
        txtMake = new TextField();
        formGrid.add(txtMake, 1, row++);

        formGrid.add(new Label("Model:"), 0, row);
        txtModel = new TextField();
        formGrid.add(txtModel, 1, row++);

        formGrid.add(new Label("Year:"), 0, row);
        txtYear = new TextField();
        formGrid.add(txtYear, 1, row++);

        formGrid.add(new Label("Owner:"), 0, row);

        if (currentUser.getRole().equals("ADMIN")) {
            // For ADMIN: Search customer by name
            HBox searchBox = new HBox(5);
            txtSearchCustomer = new TextField();
            txtSearchCustomer.setPromptText("Enter customer name to search");
            txtSearchCustomer.setPrefWidth(180);
            Button btnSearchCustomer = new Button("Search");
            btnSearchCustomer.setOnAction(e -> searchCustomer());
            searchBox.getChildren().addAll(txtSearchCustomer, btnSearchCustomer);
            formGrid.add(searchBox, 1, row++);

            formGrid.add(new Label("Select Customer:"), 0, row);
            customerSearchCombo = new ComboBox<>();
            customerSearchCombo.setPromptText("Search results will appear here");
            customerSearchCombo.setPrefWidth(250);
            customerSearchCombo.setVisible(false);
            formGrid.add(customerSearchCombo, 1, row++);

            selectedCustomerLabel = new Label("No customer selected");
            formGrid.add(selectedCustomerLabel, 1, row++);
        } else {
            customerComboBox = new ComboBox<>();
            customerComboBox.setPrefWidth(200);
            formGrid.add(customerComboBox, 1, row++);
        }

        formPanel.getChildren().addAll(formTitle, formGrid);

        return formPanel;
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
                Customer customer = new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("phone"),
                        rs.getString("email")
                );
                customers.add(customer);
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

            customerSearchCombo.setOnAction(e -> {
                Customer selected = customerSearchCombo.getValue();
                if (selected != null) {
                    selectedCustomerLabel.setText("Selected: " + selected.getName());
                }
            });
        }
    }

    private HBox createButtonPanel() {
        HBox buttonPanel = new HBox(10);
        buttonPanel.setPadding(new Insets(0, 0, 10, 0));

        btnAdd = new Button("Add Vehicle");
        btnEdit = new Button("Edit Vehicle");
        btnDelete = new Button("Delete Vehicle");
        btnRefresh = new Button("Refresh");

        btnAdd.setOnAction(e -> handleAdd());
        btnEdit.setOnAction(e -> handleEdit());
        btnDelete.setOnAction(e -> handleDelete());
        btnRefresh.setOnAction(e -> loadData());

        buttonPanel.getChildren().addAll(btnAdd, btnEdit, btnDelete, btnRefresh);

        return buttonPanel;
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
            selectedCustomerLabel.setText("Selected: " + vehicle.getOwnerName());
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
            if (selectedCustomerLabel != null) {
                selectedCustomerLabel.setText("No customer selected");
            }
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