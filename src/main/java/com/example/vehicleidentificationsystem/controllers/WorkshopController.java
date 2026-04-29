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

    // For ADMIN - workshop selector for queries
    private ComboBox<User> workshopSelectorCombo;
    private Integer selectedWorkshopIdForQueries;

    // For regular users - simple combo
    private ComboBox<Vehicle> vehicleCombo;

    public VBox createView(User user) {
        this.currentUser = user;

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label title = new Label("Workshop Module");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Service Records Tab
        Tab serviceTab = new Tab("Service Records");
        serviceTab.setClosable(false);
        serviceTab.setContent(createServiceRecordsContent());
        tabPane.getTabs().add(serviceTab);

        // View Queries Tab (display only, no response)
        Tab viewQueriesTab = new Tab("View Queries");
        viewQueriesTab.setClosable(false);
        viewQueriesTab.setContent(createViewQueriesContent());
        tabPane.getTabs().add(viewQueriesTab);

        // Respond to Query Tab (enter ID and respond)
        Tab respondTab = new Tab("Respond to Query");
        respondTab.setClosable(false);
        respondTab.setContent(createRespondContent());
        tabPane.getTabs().add(respondTab);

        root.getChildren().addAll(title, tabPane);

        return root;
    }

    private VBox createServiceRecordsContent() {
        VBox container = new VBox(15);

        serviceTable = new TableView<>();
        serviceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        createTableColumns();

        VBox formPanel = createFormPanel();

        container.getChildren().addAll(formPanel, serviceTable);

        loadData();

        return container;
    }

    // VIEW ONLY - Display queries sent to workshops (with workshop selector for ADMIN)
    private VBox createViewQueriesContent() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(10));

        Label headerLabel = new Label("Queries Sent to Workshops");
        headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Workshop selector for ADMIN
        if (currentUser.getRole().equals("ADMIN")) {
            HBox selectorBox = new HBox(10);
            selectorBox.setAlignment(Pos.CENTER_LEFT);
            selectorBox.setPadding(new Insets(5, 0, 10, 0));

            Label selectorLabel = new Label("Select Workshop:");
            selectorLabel.setStyle("-fx-font-weight: bold;");

            workshopSelectorCombo = new ComboBox<>();
            workshopSelectorCombo.setPromptText("Select workshop to view queries");
            workshopSelectorCombo.setPrefWidth(250);

            Button btnRefresh = new Button("Refresh");
            btnRefresh.setOnAction(e -> {
                if (workshopSelectorCombo.getValue() != null) {
                    selectedWorkshopIdForQueries = workshopSelectorCombo.getValue().getUserId();
                } else {
                    selectedWorkshopIdForQueries = null;
                }
                refreshQueriesTable();
            });

            selectorBox.getChildren().addAll(selectorLabel, workshopSelectorCombo, btnRefresh);
            container.getChildren().add(selectorBox);

            loadWorkshopsForSelector();
        } else {
            Label infoLabel = new Label("These are the questions customers have sent to your workshop.");
            infoLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
            container.getChildren().add(infoLabel);
            selectedWorkshopIdForQueries = currentUser.getUserId();
        }

        TableView<CustomerQuery> queryTable = new TableView<>();
        queryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        createQueryTableColumns(queryTable);

        container.getChildren().add(queryTable);

        // Store reference to refresh
        refreshQueriesTable = () -> loadWorkshopQueries(queryTable, selectedWorkshopIdForQueries);
        refreshQueriesTable.run();

        return container;
    }

    private Runnable refreshQueriesTable;

    private void refreshQueriesTable() {
        if (refreshQueriesTable != null) {
            refreshQueriesTable.run();
        }
    }

    private void loadWorkshopsForSelector() {
        List<User> workshops = new ArrayList<>();
        String sql = "SELECT user_id, username, full_name FROM app_user WHERE user_type = 'WORKSHOP' ORDER BY full_name";

        try (PreparedStatement pstmt = currentUser.getDbConnection().prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

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
            AlertUtils.showError("Database Error", "Failed to load workshops: " + e.getMessage());
        }

        workshopSelectorCombo.setItems(FXCollections.observableArrayList(workshops));
        workshopSelectorCombo.setCellFactory(lv -> new ListCell<User>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getFullName());
            }
        });
        workshopSelectorCombo.setButtonCell(new ListCell<User>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item != null ? item.getFullName() : "Select workshop");
            }
        });
    }

    // RESPOND TAB - Enter Query ID and respond (with workshop selector for ADMIN)
    private VBox createRespondContent() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setAlignment(Pos.TOP_CENTER);

        Label headerLabel = new Label("Respond to Customer Query");
        headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label infoLabel = new Label("Enter the Query ID and your response below.");
        infoLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        // Workshop selector for ADMIN (so they can respond on behalf of a workshop)
        if (currentUser.getRole().equals("ADMIN")) {
            HBox selectorBox = new HBox(10);
            selectorBox.setAlignment(Pos.CENTER_LEFT);
            selectorBox.setPadding(new Insets(5, 0, 10, 0));

            Label selectorLabel = new Label("Respond As Workshop:");
            selectorLabel.setStyle("-fx-font-weight: bold;");

            ComboBox<User> respondWorkshopCombo = new ComboBox<>();
            respondWorkshopCombo.setPromptText("Select workshop to respond as");
            respondWorkshopCombo.setPrefWidth(250);

            selectorBox.getChildren().addAll(selectorLabel, respondWorkshopCombo);
            container.getChildren().add(selectorBox);

            // Load workshops
            List<User> workshops = new ArrayList<>();
            String sql = "SELECT user_id, username, full_name FROM app_user WHERE user_type = 'WORKSHOP' ORDER BY full_name";
            try (PreparedStatement pstmt = currentUser.getDbConnection().prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
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
                AlertUtils.showError("Database Error", "Failed to load workshops: " + e.getMessage());
            }
            respondWorkshopCombo.setItems(FXCollections.observableArrayList(workshops));
            respondWorkshopCombo.setCellFactory(lv -> new ListCell<User>() {
                @Override
                protected void updateItem(User item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getFullName());
                }
            });

            // Query ID Field
            HBox queryIdBox = new HBox(10);
            queryIdBox.setAlignment(Pos.CENTER_LEFT);
            Label queryIdLabel = new Label("Query ID:");
            queryIdLabel.setPrefWidth(100);
            TextField txtQueryId = new TextField();
            txtQueryId.setPromptText("Enter query ID");
            txtQueryId.setPrefWidth(200);
            Button btnLoad = new Button("Load Query");
            queryIdBox.getChildren().addAll(queryIdLabel, txtQueryId, btnLoad);
            container.getChildren().add(queryIdBox);

            VBox detailsBox = new VBox(8);
            detailsBox.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-color: #f9f9f9; -fx-padding: 15;");
            detailsBox.setVisible(false);

            Label queryDetailsLabel = new Label();
            queryDetailsLabel.setWrapText(true);
            queryDetailsLabel.setStyle("-fx-font-size: 13px;");

            Label responseLabel = new Label("Your Response:");
            TextArea txtResponse = new TextArea();
            txtResponse.setPromptText("Type your response here...");
            txtResponse.setPrefRowCount(4);
            txtResponse.setPrefWidth(500);
            txtResponse.setWrapText(true);

            Button btnSubmit = new Button("Submit Response");
            btnSubmit.setDisable(true);

            detailsBox.getChildren().addAll(queryDetailsLabel, responseLabel, txtResponse, btnSubmit);
            container.getChildren().add(detailsBox);

            // Load Query Action
            btnLoad.setOnAction(e -> {
                String queryIdText = txtQueryId.getText().trim();
                User selectedWorkshop = respondWorkshopCombo.getValue();
                if (queryIdText.isEmpty()) {
                    AlertUtils.showWarning("Missing Data", "Please enter a Query ID.");
                    return;
                }
                if (selectedWorkshop == null && currentUser.getRole().equals("ADMIN")) {
                    AlertUtils.showWarning("Missing Data", "Please select a workshop to respond as.");
                    return;
                }

                int queryId;
                try {
                    queryId = Integer.parseInt(queryIdText);
                } catch (NumberFormatException ex) {
                    AlertUtils.showError("Invalid Input", "Query ID must be a number.");
                    return;
                }

                int workshopId = selectedWorkshop != null ? selectedWorkshop.getUserId() : currentUser.getUserId();
                loadQueryDetailsForAdmin(queryId, workshopId, queryDetailsLabel, txtResponse, btnSubmit);
                detailsBox.setVisible(true);
            });

            btnSubmit.setOnAction(e -> {
                String queryIdText = txtQueryId.getText().trim();
                String response = txtResponse.getText().trim();
                User selectedWorkshop = respondWorkshopCombo.getValue();

                if (response.isEmpty()) {
                    AlertUtils.showWarning("Empty Response", "Please enter a response.");
                    return;
                }

                int queryId = Integer.parseInt(queryIdText);
                int workshopId = selectedWorkshop != null ? selectedWorkshop.getUserId() : currentUser.getUserId();
                submitResponseAsWorkshop(queryId, response, workshopId, txtQueryId, txtResponse, queryDetailsLabel, btnSubmit, detailsBox);
            });

        } else {
            // For regular WORKSHOP user (no selector)
            selectedWorkshopIdForQueries = currentUser.getUserId();

            HBox queryIdBox = new HBox(10);
            queryIdBox.setAlignment(Pos.CENTER_LEFT);
            Label queryIdLabel = new Label("Query ID:");
            queryIdLabel.setPrefWidth(100);
            TextField txtQueryId = new TextField();
            txtQueryId.setPromptText("Enter query ID");
            txtQueryId.setPrefWidth(200);
            Button btnLoad = new Button("Load Query");
            queryIdBox.getChildren().addAll(queryIdLabel, txtQueryId, btnLoad);
            container.getChildren().add(queryIdBox);

            VBox detailsBox = new VBox(8);
            detailsBox.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-color: #f9f9f9; -fx-padding: 15;");
            detailsBox.setVisible(false);

            Label queryDetailsLabel = new Label();
            queryDetailsLabel.setWrapText(true);
            queryDetailsLabel.setStyle("-fx-font-size: 13px;");

            Label responseLabel = new Label("Your Response:");
            TextArea txtResponse = new TextArea();
            txtResponse.setPromptText("Type your response here...");
            txtResponse.setPrefRowCount(4);
            txtResponse.setPrefWidth(500);
            txtResponse.setWrapText(true);

            Button btnSubmit = new Button("Submit Response");
            btnSubmit.setDisable(true);

            detailsBox.getChildren().addAll(queryDetailsLabel, responseLabel, txtResponse, btnSubmit);
            container.getChildren().add(detailsBox);

            btnLoad.setOnAction(e -> {
                String queryIdText = txtQueryId.getText().trim();
                if (queryIdText.isEmpty()) {
                    AlertUtils.showWarning("Missing Data", "Please enter a Query ID.");
                    return;
                }

                int queryId;
                try {
                    queryId = Integer.parseInt(queryIdText);
                } catch (NumberFormatException ex) {
                    AlertUtils.showError("Invalid Input", "Query ID must be a number.");
                    return;
                }

                loadQueryDetails(queryId, queryDetailsLabel, txtResponse, btnSubmit);
                detailsBox.setVisible(true);
            });

            btnSubmit.setOnAction(e -> {
                String queryIdText = txtQueryId.getText().trim();
                String response = txtResponse.getText().trim();

                if (response.isEmpty()) {
                    AlertUtils.showWarning("Empty Response", "Please enter a response.");
                    return;
                }

                int queryId = Integer.parseInt(queryIdText);
                submitResponse(queryId, response, txtQueryId, txtResponse, queryDetailsLabel, btnSubmit, detailsBox);
            });
        }

        return container;
    }

    private void loadQueryDetailsForAdmin(int queryId, int workshopId, Label queryDetailsLabel, TextArea txtResponse, Button btnSubmit) {
        String sql = """
            SELECT q.query_id, c.name as customer_name, v.registration_number,
                   q.query_text, q.response_text, q.query_type, q.target_id, q.target_name
            FROM customer_query q
            JOIN vehicle v ON q.vehicle_id = v.vehicle_id
            JOIN customer c ON q.customer_id = c.customer_id
            WHERE q.query_id = ? AND q.target_id = ? AND q.query_type = 'WORKSHOP'
        """;

        try (PreparedStatement pstmt = currentUser.getDbConnection().prepareStatement(sql)) {
            pstmt.setInt(1, queryId);
            pstmt.setInt(2, workshopId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String customerName = rs.getString("customer_name");
                String vehicleReg = rs.getString("registration_number");
                String queryText = rs.getString("query_text");
                String existingResponse = rs.getString("response_text");
                String targetName = rs.getString("target_name");

                if (existingResponse != null && !existingResponse.isEmpty()) {
                    queryDetailsLabel.setText(
                            "Customer: " + customerName + "\n" +
                                    "Vehicle: " + vehicleReg + "\n" +
                                    "Workshop: " + targetName + "\n" +
                                    "Question: " + queryText + "\n\n" +
                                    "⚠️ This query has already been responded to!\n" +
                                    "Existing Response: " + existingResponse
                    );
                    txtResponse.setText(existingResponse);
                    txtResponse.setEditable(false);
                    btnSubmit.setDisable(true);
                } else {
                    queryDetailsLabel.setText(
                            "Customer: " + customerName + "\n" +
                                    "Vehicle: " + vehicleReg + "\n" +
                                    "Workshop: " + targetName + "\n" +
                                    "Question: " + queryText + "\n\n" +
                                    "✅ You can respond to this query."
                    );
                    txtResponse.clear();
                    txtResponse.setEditable(true);
                    btnSubmit.setDisable(false);
                }
            } else {
                queryDetailsLabel.setText("❌ Query ID " + queryId + " not found or not sent to selected workshop.");
                txtResponse.setEditable(false);
                btnSubmit.setDisable(true);
            }
        } catch (SQLException e) {
            AlertUtils.showError("Database Error", "Failed to load query: " + e.getMessage());
            queryDetailsLabel.setText("Error loading query details.");
            txtResponse.setEditable(false);
            btnSubmit.setDisable(true);
        }
    }

    private void submitResponseAsWorkshop(int queryId, String response, int workshopId, TextField txtQueryId,
                                          TextArea txtResponse, Label queryDetailsLabel,
                                          Button btnSubmit, VBox detailsBox) {
        String sql = "UPDATE customer_query SET response_text = ? WHERE query_id = ? AND target_id = ?";

        try (PreparedStatement pstmt = currentUser.getDbConnection().prepareStatement(sql)) {
            pstmt.setString(1, response);
            pstmt.setInt(2, queryId);
            pstmt.setInt(3, workshopId);
            int updated = pstmt.executeUpdate();

            if (updated > 0) {
                AlertUtils.showInfo("Success", "Response submitted to customer as workshop!");

                txtQueryId.clear();
                txtResponse.clear();
                queryDetailsLabel.setText("");
                detailsBox.setVisible(false);
                btnSubmit.setDisable(true);
            } else {
                AlertUtils.showError("Error", "Failed to submit response. Query ID not found or not authorized.");
            }
        } catch (SQLException e) {
            AlertUtils.showError("Database Error", "Failed to submit response: " + e.getMessage());
        }
    }

    private void loadQueryDetails(int queryId, Label queryDetailsLabel, TextArea txtResponse, Button btnSubmit) {
        String sql = """
            SELECT q.query_id, c.name as customer_name, v.registration_number,
                   q.query_text, q.response_text, q.query_type, q.target_id, q.target_name
            FROM customer_query q
            JOIN vehicle v ON q.vehicle_id = v.vehicle_id
            JOIN customer c ON q.customer_id = c.customer_id
            WHERE q.query_id = ? AND q.target_id = ? AND q.query_type = 'WORKSHOP'
        """;

        try (PreparedStatement pstmt = currentUser.getDbConnection().prepareStatement(sql)) {
            pstmt.setInt(1, queryId);
            pstmt.setInt(2, currentUser.getUserId());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String customerName = rs.getString("customer_name");
                String vehicleReg = rs.getString("registration_number");
                String queryText = rs.getString("query_text");
                String existingResponse = rs.getString("response_text");
                String targetName = rs.getString("target_name");

                if (existingResponse != null && !existingResponse.isEmpty()) {
                    queryDetailsLabel.setText(
                            "Customer: " + customerName + "\n" +
                                    "Vehicle: " + vehicleReg + "\n" +
                                    "Question: " + queryText + "\n\n" +
                                    "⚠️ This query has already been responded to!\n" +
                                    "Existing Response: " + existingResponse
                    );
                    txtResponse.setText(existingResponse);
                    txtResponse.setEditable(false);
                    btnSubmit.setDisable(true);
                } else {
                    queryDetailsLabel.setText(
                            "Customer: " + customerName + "\n" +
                                    "Vehicle: " + vehicleReg + "\n" +
                                    "Question: " + queryText + "\n\n" +
                                    "✅ You can respond to this query."
                    );
                    txtResponse.clear();
                    txtResponse.setEditable(true);
                    btnSubmit.setDisable(false);
                }
            } else {
                queryDetailsLabel.setText("❌ Query ID " + queryId + " not found or not sent to your workshop.");
                txtResponse.setEditable(false);
                btnSubmit.setDisable(true);
            }
        } catch (SQLException e) {
            AlertUtils.showError("Database Error", "Failed to load query: " + e.getMessage());
            queryDetailsLabel.setText("Error loading query details.");
            txtResponse.setEditable(false);
            btnSubmit.setDisable(true);
        }
    }

    private void submitResponse(int queryId, String response, TextField txtQueryId,
                                TextArea txtResponse, Label queryDetailsLabel,
                                Button btnSubmit, VBox detailsBox) {
        String sql = "UPDATE customer_query SET response_text = ? WHERE query_id = ? AND target_id = ?";

        try (PreparedStatement pstmt = currentUser.getDbConnection().prepareStatement(sql)) {
            pstmt.setString(1, response);
            pstmt.setInt(2, queryId);
            pstmt.setInt(3, currentUser.getUserId());
            int updated = pstmt.executeUpdate();

            if (updated > 0) {
                AlertUtils.showInfo("Success", "Response submitted to customer!");

                txtQueryId.clear();
                txtResponse.clear();
                queryDetailsLabel.setText("");
                detailsBox.setVisible(false);
                btnSubmit.setDisable(true);
            } else {
                AlertUtils.showError("Error", "Failed to submit response. Query ID not found or not authorized.");
            }
        } catch (SQLException e) {
            AlertUtils.showError("Database Error", "Failed to submit response: " + e.getMessage());
        }
    }

    private void createQueryTableColumns(TableView<CustomerQuery> queryTable) {
        TableColumn<CustomerQuery, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("queryId"));
        idCol.setPrefWidth(50);

        TableColumn<CustomerQuery, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        customerCol.setPrefWidth(150);

        TableColumn<CustomerQuery, String> vehicleCol = new TableColumn<>("Vehicle");
        vehicleCol.setCellValueFactory(new PropertyValueFactory<>("vehicleReg"));
        vehicleCol.setPrefWidth(120);

        TableColumn<CustomerQuery, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("queryDate"));
        dateCol.setPrefWidth(100);

        TableColumn<CustomerQuery, String> queryCol = new TableColumn<>("Question");
        queryCol.setCellValueFactory(new PropertyValueFactory<>("queryText"));
        queryCol.setPrefWidth(350);

        TableColumn<CustomerQuery, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> {
            String response = cellData.getValue().getResponseText();
            if (response != null && !response.equals("Awaiting response...")) {
                return new javafx.beans.property.SimpleStringProperty("Responded");
            }
            return new javafx.beans.property.SimpleStringProperty("Pending");
        });
        statusCol.setPrefWidth(80);

        queryTable.getColumns().addAll(idCol, customerCol, vehicleCol, dateCol, queryCol, statusCol);
    }

    private void loadWorkshopQueries(TableView<CustomerQuery> queryTable, Integer workshopId) {
        if (workshopId == null) {
            queryTable.setItems(FXCollections.observableArrayList());
            return;
        }

        List<CustomerQuery> queries = new ArrayList<>();

        String sql = """
            SELECT q.query_id, q.customer_id, c.name as customer_name, 
                   q.vehicle_id, v.registration_number,
                   q.query_date, q.query_text, q.response_text, q.query_type,
                   q.target_id, q.target_type, q.target_name
            FROM customer_query q
            JOIN vehicle v ON q.vehicle_id = v.vehicle_id
            JOIN customer c ON q.customer_id = c.customer_id
            WHERE q.target_id = ? AND q.query_type = 'WORKSHOP'
            ORDER BY 
                CASE WHEN q.response_text IS NULL THEN 0 ELSE 1 END,
                q.query_date DESC
        """;

        try (PreparedStatement pstmt = currentUser.getDbConnection().prepareStatement(sql)) {
            pstmt.setInt(1, workshopId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String response = rs.getString("response_text");
                queries.add(new CustomerQuery(
                        rs.getInt("query_id"),
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
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

    private void createTableColumns() {
        TableColumn<ServiceRecord, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("serviceId"));

        TableColumn<ServiceRecord, String> vehicleCol = new TableColumn<>("Vehicle");
        vehicleCol.setCellValueFactory(new PropertyValueFactory<>("vehicleReg"));

        TableColumn<ServiceRecord, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("serviceDate"));

        TableColumn<ServiceRecord, String> typeCol = new TableColumn<>("Service Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("serviceType"));

        TableColumn<ServiceRecord, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<ServiceRecord, Double> costCol = new TableColumn<>("Cost");
        costCol.setCellValueFactory(new PropertyValueFactory<>("cost"));

        TableColumn<ServiceRecord, String> workshopCol = new TableColumn<>("Workshop");
        workshopCol.setCellValueFactory(new PropertyValueFactory<>("workshopName"));

        serviceTable.getColumns().addAll(idCol, vehicleCol, dateCol, typeCol, descCol, costCol, workshopCol);
    }

    private VBox createFormPanel() {
        VBox formPanel = new VBox(10);
        formPanel.setPadding(new Insets(10));

        Label formTitle = new Label("Add Service Record");
        formTitle.setStyle("-fx-font-weight: bold;");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(8);

        int row = 0;

        if (currentUser.getRole().equals("ADMIN")) {
            // For ADMIN: Search vehicle by registration
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

            // Workshop search
            formGrid.add(new Label("Workshop (Search):"), 0, row);
            txtSearchWorkshop = new TextField();
            txtSearchWorkshop.setPromptText("Enter workshop name to search");
            txtSearchWorkshop.setPrefWidth(180);
            Button btnSearchWorkshop = new Button("Search");
            btnSearchWorkshop.setOnAction(e -> searchWorkshop());
            HBox searchWorkshopBox = new HBox(5, txtSearchWorkshop, btnSearchWorkshop);
            formGrid.add(searchWorkshopBox, 1, row++);

            formGrid.add(new Label("Select Workshop:"), 0, row);
            workshopSearchCombo = new ComboBox<>();
            workshopSearchCombo.setPromptText("Search results will appear here");
            workshopSearchCombo.setPrefWidth(300);
            workshopSearchCombo.setVisible(false);
            formGrid.add(workshopSearchCombo, 1, row++);

            selectedWorkshopLabel = new Label("No workshop selected");
            formGrid.add(selectedWorkshopLabel, 1, row++);
        } else {
            formGrid.add(new Label("Vehicle:"), 0, row);
            vehicleCombo = new ComboBox<>();
            vehicleCombo.setPrefWidth(250);
            formGrid.add(vehicleCombo, 1, row++);
        }

        formGrid.add(new Label("Service Type:"), 0, row);
        txtServiceType = new TextField();
        formGrid.add(txtServiceType, 1, row++);

        formGrid.add(new Label("Cost (M):"), 0, row);
        txtCost = new TextField();
        formGrid.add(txtCost, 1, row++);

        formGrid.add(new Label("Description:"), 0, row);
        txtDescription = new TextArea();
        txtDescription.setPrefRowCount(3);
        formGrid.add(txtDescription, 1, row, 2, 1);

        Button btnSubmit = new Button("Add Service Record");
        btnSubmit.setOnAction(e -> handleAddService());

        formPanel.getChildren().addAll(formTitle, formGrid, btnSubmit);

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

            workshopSearchCombo.setOnAction(e -> {
                User selected = workshopSearchCombo.getValue();
                if (selected != null) {
                    selectedWorkshopLabel.setText("Selected: " + selected.getFullName());
                }
            });
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