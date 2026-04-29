package com.example.vehicleidentificationsystem.controllers;

import com.example.vehicleidentificationsystem.models.*;
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

public class QueryManagementController {

    private User currentUser;

    public VBox createView(User user) {
        this.currentUser = user;

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label title = new Label("Queries");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TabPane innerTabPane = new TabPane();
        innerTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Tab 1: All Queries - VIEW ONLY
        Tab allQueriesTab = new Tab("All Queries");
        allQueriesTab.setClosable(false);
        allQueriesTab.setContent(createTableViewOnly(null, null));
        innerTabPane.getTabs().add(allQueriesTab);

        // Tab 2: Workshop Queries - For current workshop user
        if (currentUser.getRole().equals("WORKSHOP")) {
            Tab workshopQueriesTab = new Tab("My Workshop Queries");
            workshopQueriesTab.setClosable(false);
            workshopQueriesTab.setContent(createTableViewOnly("WORKSHOP", currentUser.getUserId()));
            innerTabPane.getTabs().add(workshopQueriesTab);
        } else if (currentUser.getRole().equals("ADMIN")) {
            Tab workshopQueriesTab = new Tab("Workshop Queries");
            workshopQueriesTab.setClosable(false);
            workshopQueriesTab.setContent(createTableViewOnly("WORKSHOP", null));
            innerTabPane.getTabs().add(workshopQueriesTab);
        }

        // Tab 3: Insurance Queries - For current insurance company
        if (currentUser.getRole().equals("INSURANCE")) {
            Tab insuranceQueriesTab = new Tab("My Insurance Queries");
            insuranceQueriesTab.setClosable(false);
            insuranceQueriesTab.setContent(createTableViewOnly("INSURANCE", currentUser.getCompanyId()));
            innerTabPane.getTabs().add(insuranceQueriesTab);
        } else if (currentUser.getRole().equals("ADMIN")) {
            Tab insuranceQueriesTab = new Tab("Insurance Queries");
            insuranceQueriesTab.setClosable(false);
            insuranceQueriesTab.setContent(createTableViewOnly("INSURANCE", null));
            innerTabPane.getTabs().add(insuranceQueriesTab);
        }

        // Tab 4: Police Queries
        Tab policeQueriesTab = new Tab("Police Queries");
        policeQueriesTab.setClosable(false);
        policeQueriesTab.setContent(createTableViewOnly("POLICE", null));
        innerTabPane.getTabs().add(policeQueriesTab);

        // Tab 5: Respond to Query
        Tab respondTab = new Tab("Respond to Query");
        respondTab.setClosable(false);
        respondTab.setContent(createRespondView());
        innerTabPane.getTabs().add(respondTab);

        root.getChildren().addAll(title, innerTabPane);

        return root;
    }

    private VBox createTableViewOnly(String department, Integer targetId) {
        VBox container = new VBox(15);
        container.setPadding(new Insets(10));

        TableView<CustomerQuery> queryTable = new TableView<>();
        queryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        createTableColumns(queryTable);

        container.getChildren().addAll(queryTable);

        loadQueries(queryTable, department, targetId);

        return container;
    }

    private VBox createRespondView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setAlignment(Pos.TOP_CENTER);

        Label headerLabel = new Label("Respond to Customer Query by ID");
        headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        HBox queryIdBox = new HBox(10);
        queryIdBox.setAlignment(Pos.CENTER_LEFT);
        Label queryIdLabel = new Label("Query ID:");
        queryIdLabel.setPrefWidth(100);
        TextField txtQueryId = new TextField();
        txtQueryId.setPromptText("Enter query ID");
        txtQueryId.setPrefWidth(200);
        Button btnLoad = new Button("Load Query");
        queryIdBox.getChildren().addAll(queryIdLabel, txtQueryId, btnLoad);

        VBox detailsBox = new VBox(8);
        detailsBox.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-color: #f9f9f9; -fx-padding: 15;");
        detailsBox.setVisible(false);

        Label queryDetailsLabel = new Label();
        queryDetailsLabel.setWrapText(true);
        queryDetailsLabel.setStyle("-fx-font-size: 13px;");

        Label responseLabel = new Label("Your Response:");
        TextArea txtResponse = new TextArea();
        txtResponse.setPromptText("Type your response here...");
        txtResponse.setPrefRowCount(5);
        txtResponse.setPrefWidth(500);
        txtResponse.setWrapText(true);

        Button btnSubmit = new Button("Submit Response");
        btnSubmit.setDisable(true);

        detailsBox.getChildren().addAll(queryDetailsLabel, responseLabel, txtResponse, btnSubmit);

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

        container.getChildren().addAll(headerLabel, queryIdBox, detailsBox);

        return container;
    }

    private void loadQueryDetails(int queryId, Label queryDetailsLabel, TextArea txtResponse, Button btnSubmit) {
        String sql = """
            SELECT q.query_id, c.name as customer_name, v.registration_number,
                   q.query_text, q.response_text, q.query_type, q.target_name
            FROM customer_query q
            JOIN vehicle v ON q.vehicle_id = v.vehicle_id
            JOIN customer c ON q.customer_id = c.customer_id
            WHERE q.query_id = ?
        """;

        try (PreparedStatement pstmt = currentUser.getDbConnection().prepareStatement(sql)) {
            pstmt.setInt(1, queryId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String customerName = rs.getString("customer_name");
                String vehicleReg = rs.getString("registration_number");
                String queryText = rs.getString("query_text");
                String existingResponse = rs.getString("response_text");
                String queryType = rs.getString("query_type");
                String targetName = rs.getString("target_name");

                boolean canRespond = false;
                if (currentUser.getRole().equals("ADMIN")) {
                    canRespond = true;
                } else if (currentUser.getRole().equals("WORKSHOP") && queryType.equals("WORKSHOP")) {
                    canRespond = true;
                } else if (currentUser.getRole().equals("INSURANCE") && queryType.equals("INSURANCE")) {
                    canRespond = true;
                } else if (currentUser.getRole().equals("POLICE") && queryType.equals("POLICE")) {
                    canRespond = true;
                }

                if (existingResponse != null && !existingResponse.isEmpty()) {
                    queryDetailsLabel.setText(
                            "Customer: " + customerName + "\n" +
                                    "Vehicle: " + vehicleReg + "\n" +
                                    "Department: " + queryType + "\n" +
                                    "Sent To: " + targetName + "\n" +
                                    "Question: " + queryText + "\n\n" +
                                    "⚠️ This query has already been responded to!\n" +
                                    "Existing Response: " + existingResponse
                    );
                    txtResponse.setEditable(false);
                    btnSubmit.setDisable(true);
                } else if (canRespond) {
                    queryDetailsLabel.setText(
                            "Customer: " + customerName + "\n" +
                                    "Vehicle: " + vehicleReg + "\n" +
                                    "Department: " + queryType + "\n" +
                                    "Sent To: " + targetName + "\n" +
                                    "Question: " + queryText + "\n\n" +
                                    "✅ You can respond to this query."
                    );
                    txtResponse.setEditable(true);
                    txtResponse.clear();
                    btnSubmit.setDisable(false);
                } else {
                    queryDetailsLabel.setText(
                            "Customer: " + customerName + "\n" +
                                    "Vehicle: " + vehicleReg + "\n" +
                                    "Department: " + queryType + "\n" +
                                    "Sent To: " + targetName + "\n" +
                                    "Question: " + queryText + "\n\n" +
                                    "❌ You don't have permission to respond to this query.\n" +
                                    "This query belongs to the " + queryType + " department."
                    );
                    txtResponse.setEditable(false);
                    btnSubmit.setDisable(true);
                }
            } else {
                queryDetailsLabel.setText("❌ Query ID " + queryId + " not found.");
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
        String sql = "UPDATE customer_query SET response_text = ? WHERE query_id = ?";

        try (PreparedStatement pstmt = currentUser.getDbConnection().prepareStatement(sql)) {
            pstmt.setString(1, response);
            pstmt.setInt(2, queryId);
            int updated = pstmt.executeUpdate();

            if (updated > 0) {
                AlertUtils.showInfo("Success", "Response submitted to customer!");

                txtQueryId.clear();
                txtResponse.clear();
                queryDetailsLabel.setText("");
                detailsBox.setVisible(false);
                btnSubmit.setDisable(true);
            } else {
                AlertUtils.showError("Error", "Failed to submit response. Query ID not found.");
            }
        } catch (SQLException e) {
            AlertUtils.showError("Database Error", "Failed to submit response: " + e.getMessage());
        }
    }

    private void createTableColumns(TableView<CustomerQuery> queryTable) {
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

        TableColumn<CustomerQuery, String> typeCol = new TableColumn<>("Department");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("queryType"));
        typeCol.setPrefWidth(100);

        TableColumn<CustomerQuery, String> targetCol = new TableColumn<>("Sent To");
        targetCol.setCellValueFactory(new PropertyValueFactory<>("targetName"));
        targetCol.setPrefWidth(150);

        TableColumn<CustomerQuery, String> queryCol = new TableColumn<>("Question");
        queryCol.setCellValueFactory(new PropertyValueFactory<>("queryText"));
        queryCol.setPrefWidth(250);

        TableColumn<CustomerQuery, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> {
            String response = cellData.getValue().getResponseText();
            if (response != null && !response.equals("Awaiting response...")) {
                return new javafx.beans.property.SimpleStringProperty("Responded");
            }
            return new javafx.beans.property.SimpleStringProperty("Pending");
        });
        statusCol.setPrefWidth(80);

        queryTable.getColumns().addAll(idCol, customerCol, vehicleCol, dateCol, typeCol, targetCol, queryCol, statusCol);
    }

    private void loadQueries(TableView<CustomerQuery> queryTable, String department, Integer targetId) {
        List<CustomerQuery> queries = new ArrayList<>();

        String sql;
        if (department == null) {
            sql = """
                SELECT q.query_id, q.customer_id, c.name as customer_name, 
                       q.vehicle_id, v.registration_number,
                       q.query_date, q.query_text, q.response_text, q.query_type,
                       q.target_id, q.target_type, q.target_name
                FROM customer_query q
                JOIN vehicle v ON q.vehicle_id = v.vehicle_id
                JOIN customer c ON q.customer_id = c.customer_id
                ORDER BY 
                    CASE WHEN q.response_text IS NULL THEN 0 ELSE 1 END,
                    q.query_date DESC
            """;
        } else if (targetId != null) {
            sql = """
                SELECT q.query_id, q.customer_id, c.name as customer_name, 
                       q.vehicle_id, v.registration_number,
                       q.query_date, q.query_text, q.response_text, q.query_type,
                       q.target_id, q.target_type, q.target_name
                FROM customer_query q
                JOIN vehicle v ON q.vehicle_id = v.vehicle_id
                JOIN customer c ON q.customer_id = c.customer_id
                WHERE q.query_type = ? AND q.target_id = ?
                ORDER BY 
                    CASE WHEN q.response_text IS NULL THEN 0 ELSE 1 END,
                    q.query_date DESC
            """;
        } else {
            sql = """
                SELECT q.query_id, q.customer_id, c.name as customer_name, 
                       q.vehicle_id, v.registration_number,
                       q.query_date, q.query_text, q.response_text, q.query_type,
                       q.target_id, q.target_type, q.target_name
                FROM customer_query q
                JOIN vehicle v ON q.vehicle_id = v.vehicle_id
                JOIN customer c ON q.customer_id = c.customer_id
                WHERE q.query_type = ?
                ORDER BY 
                    CASE WHEN q.response_text IS NULL THEN 0 ELSE 1 END,
                    q.query_date DESC
            """;
        }

        try (PreparedStatement pstmt = currentUser.getDbConnection().prepareStatement(sql)) {
            if (department != null && targetId != null) {
                pstmt.setString(1, department);
                pstmt.setInt(2, targetId);
            } else if (department != null) {
                pstmt.setString(1, department);
            }
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
}