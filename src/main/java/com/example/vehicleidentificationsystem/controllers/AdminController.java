package com.example.vehicleidentificationsystem.controllers;

import com.example.vehicleidentificationsystem.models.User;
import com.example.vehicleidentificationsystem.utils.AlertUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.time.format.DateTimeFormatter;

public class AdminController {

    private User currentUser;
    private TableView<UserInfo> userTable;

    private TextField txtUsername, txtFullName, txtPhone, txtEmail;
    private PasswordField txtPassword;
    private ComboBox<String> roleCombo;

    private VBox dynamicFormContainer;
    private GridPane dynamicFormGrid;

    private TextField txtCompanyName, txtCompanyReg, txtCompanyPhone, txtCompanyEmail, txtCompanyAddress;
    private TextField txtWorkshopName, txtWorkshopPhone, txtWorkshopEmail, txtWorkshopAddress;
    private TextField txtCustomerAddress;

    public VBox createView(User user) {
        this.currentUser = user;

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label title = new Label("User Management");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        if (!currentUser.canManageUsers()) {
            Label noAccess = new Label("You don't have permission to access this module.");
            noAccess.setStyle("-fx-text-fill: #f44336; -fx-padding: 20;");
            root.getChildren().addAll(title, noAccess);
            return root;
        }

        userTable = new TableView<>();
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        createUserTable();

        VBox formPanel = createUserForm();

        root.getChildren().addAll(title, formPanel, userTable);

        loadUsers();

        return root;
    }

    private VBox createUserForm() {
        VBox formPanel = new VBox(15);
        formPanel.setStyle("-fx-border-color: #ddd; -fx-border-radius: 8; -fx-background-color: #fafafa; -fx-padding: 15;");

        Label formTitle = new Label("Create New User");
        formTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TitledPane basicInfoPane = createBasicInfoSection();

        TitledPane roleSpecificPane = new TitledPane();
        roleSpecificPane.setText("Role Specific Information");
        roleSpecificPane.setCollapsible(true);
        roleSpecificPane.setExpanded(true);

        dynamicFormContainer = new VBox(10);
        dynamicFormGrid = new GridPane();
        dynamicFormGrid.setHgap(10);
        dynamicFormGrid.setVgap(8);
        dynamicFormContainer.getChildren().add(dynamicFormGrid);
        roleSpecificPane.setContent(dynamicFormContainer);

        roleCombo.setOnAction(e -> updateRoleSpecificFields());

        Button btnCreate = new Button("Create User");
        btnCreate.setPrefWidth(120);
        btnCreate.setOnAction(e -> handleCreateUser());

        HBox buttonBox = new HBox(btnCreate);
        buttonBox.setAlignment(Pos.CENTER);

        formPanel.getChildren().addAll(formTitle, basicInfoPane, roleSpecificPane, buttonBox);

        return formPanel;
    }

    private TitledPane createBasicInfoSection() {
        GridPane basicGrid = new GridPane();
        basicGrid.setHgap(10);
        basicGrid.setVgap(8);
        basicGrid.setPadding(new Insets(10));

        basicGrid.add(new Label("Username:"), 0, 0);
        txtUsername = new TextField();
        txtUsername.setPromptText("Enter username");
        basicGrid.add(txtUsername, 1, 0);

        basicGrid.add(new Label("Password:"), 2, 0);
        txtPassword = new PasswordField();
        txtPassword.setPromptText("Enter password");
        basicGrid.add(txtPassword, 3, 0);

        basicGrid.add(new Label("Full Name:"), 0, 1);
        txtFullName = new TextField();
        txtFullName.setPromptText("Enter full name");
        basicGrid.add(txtFullName, 1, 1);

        basicGrid.add(new Label("Role:"), 2, 1);
        roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("ADMIN", "POLICE", "WORKSHOP", "INSURANCE", "CUSTOMER");
        roleCombo.setPromptText("Select role");
        basicGrid.add(roleCombo, 3, 1);

        basicGrid.add(new Label("Phone:"), 0, 2);
        txtPhone = new TextField();
        txtPhone.setPromptText("Enter phone number");
        basicGrid.add(txtPhone, 1, 2);

        basicGrid.add(new Label("Email:"), 2, 2);
        txtEmail = new TextField();
        txtEmail.setPromptText("Enter email address");
        basicGrid.add(txtEmail, 3, 2);

        TitledPane basicPane = new TitledPane("Basic Information", basicGrid);
        basicPane.setCollapsible(true);
        basicPane.setExpanded(true);

        return basicPane;
    }

    private void updateRoleSpecificFields() {
        dynamicFormGrid.getChildren().clear();
        String role = roleCombo.getValue();

        if (role == null) return;

        switch (role) {
            case "INSURANCE":
                createInsuranceFields();
                break;
            case "WORKSHOP":
                createWorkshopFields();
                break;
            case "CUSTOMER":
                createCustomerFields();
                break;
            default:
                Label infoLabel = new Label("No additional information required for this role.");
                infoLabel.setStyle("-fx-text-fill: #666; -fx-padding: 10;");
                dynamicFormGrid.add(infoLabel, 0, 0);
                break;
        }
    }

    private void createInsuranceFields() {
        int row = 0;

        Label sectionLabel = new Label("Insurance Company Details");
        sectionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        dynamicFormGrid.add(sectionLabel, 0, row++, 2, 1);

        dynamicFormGrid.add(new Label("Company Name:"), 0, row);
        txtCompanyName = new TextField();
        txtCompanyName.setPromptText("Enter insurance company name");
        dynamicFormGrid.add(txtCompanyName, 1, row++);

        dynamicFormGrid.add(new Label("Registration Number:"), 0, row);
        txtCompanyReg = new TextField();
        txtCompanyReg.setPromptText("Enter company registration number");
        dynamicFormGrid.add(txtCompanyReg, 1, row++);

        dynamicFormGrid.add(new Label("Company Phone:"), 0, row);
        txtCompanyPhone = new TextField();
        txtCompanyPhone.setPromptText("Enter company phone");
        dynamicFormGrid.add(txtCompanyPhone, 1, row++);

        dynamicFormGrid.add(new Label("Company Email:"), 0, row);
        txtCompanyEmail = new TextField();
        txtCompanyEmail.setPromptText("Enter company email");
        dynamicFormGrid.add(txtCompanyEmail, 1, row++);

        dynamicFormGrid.add(new Label("Company Address:"), 0, row);
        txtCompanyAddress = new TextField();
        txtCompanyAddress.setPromptText("Enter company address");
        dynamicFormGrid.add(txtCompanyAddress, 1, row++);
    }

    private void createWorkshopFields() {
        int row = 0;

        Label sectionLabel = new Label("Workshop Details");
        sectionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        dynamicFormGrid.add(sectionLabel, 0, row++, 2, 1);

        dynamicFormGrid.add(new Label("Workshop Name:"), 0, row);
        txtWorkshopName = new TextField();
        txtWorkshopName.setPromptText("Enter workshop name");
        dynamicFormGrid.add(txtWorkshopName, 1, row++);

        dynamicFormGrid.add(new Label("Workshop Phone:"), 0, row);
        txtWorkshopPhone = new TextField();
        txtWorkshopPhone.setPromptText("Enter workshop phone");
        dynamicFormGrid.add(txtWorkshopPhone, 1, row++);

        dynamicFormGrid.add(new Label("Workshop Email:"), 0, row);
        txtWorkshopEmail = new TextField();
        txtWorkshopEmail.setPromptText("Enter workshop email");
        dynamicFormGrid.add(txtWorkshopEmail, 1, row++);

        dynamicFormGrid.add(new Label("Workshop Address:"), 0, row);
        txtWorkshopAddress = new TextField();
        txtWorkshopAddress.setPromptText("Enter workshop address");
        dynamicFormGrid.add(txtWorkshopAddress, 1, row++);
    }

    private void createCustomerFields() {
        int row = 0;

        Label sectionLabel = new Label("Customer Details");
        sectionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        dynamicFormGrid.add(sectionLabel, 0, row++, 2, 1);

        dynamicFormGrid.add(new Label("Physical Address:"), 0, row);
        txtCustomerAddress = new TextField();
        txtCustomerAddress.setPromptText("Enter customer address");
        dynamicFormGrid.add(txtCustomerAddress, 1, row++);
    }

    private void createUserTable() {
        TableColumn<UserInfo, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(cellData -> cellData.getValue().usernameProperty());

        TableColumn<UserInfo, String> fullNameCol = new TableColumn<>("Full Name");
        fullNameCol.setCellValueFactory(cellData -> cellData.getValue().fullNameProperty());

        TableColumn<UserInfo, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(cellData -> cellData.getValue().roleProperty());

        TableColumn<UserInfo, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(cellData -> cellData.getValue().phoneProperty());

        TableColumn<UserInfo, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(cellData -> cellData.getValue().emailProperty());

        TableColumn<UserInfo, String> additionalCol = new TableColumn<>("Additional Info");
        additionalCol.setCellValueFactory(cellData -> cellData.getValue().additionalInfoProperty());

        TableColumn<UserInfo, String> createdByCol = new TableColumn<>("Created By");
        createdByCol.setCellValueFactory(cellData -> cellData.getValue().createdByProperty());

        TableColumn<UserInfo, String> createdAtCol = new TableColumn<>("Created At");
        createdAtCol.setCellValueFactory(cellData -> cellData.getValue().createdAtProperty());

        userTable.getColumns().addAll(usernameCol, fullNameCol, roleCol, phoneCol, emailCol, additionalCol, createdByCol, createdAtCol);
    }

    private void loadUsers() {
        userTable.getItems().clear();

        String sql = """
            SELECT u.user_id, u.username, u.full_name, u.phone, u.email, u.user_type as role, 
                   u.user_subtype, u.created_by, u.created_at,
                   ic.name as company_name, w.name as workshop_name,
                   c.name as customer_name, c.address as customer_address
            FROM app_user u
            LEFT JOIN insurance_company ic ON u.company_id = ic.company_id
            LEFT JOIN workshop w ON u.workshop_id = w.workshop_id
            LEFT JOIN customer c ON u.customer_id = c.customer_id
            ORDER BY u.created_at DESC
        """;

        try (PreparedStatement pstmt = currentUser.getDbConnection().prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String additionalInfo = "";
                String role = rs.getString("role");
                if ("INSURANCE".equals(role)) {
                    additionalInfo = "Company: " + (rs.getString("company_name") != null ? rs.getString("company_name") : "Not set");
                } else if ("WORKSHOP".equals(role)) {
                    additionalInfo = "Workshop: " + (rs.getString("workshop_name") != null ? rs.getString("workshop_name") : "Not set");
                } else if ("CUSTOMER".equals(role)) {
                    additionalInfo = "Address: " + (rs.getString("customer_address") != null ? rs.getString("customer_address") : "Not set");
                }

                userTable.getItems().add(new UserInfo(
                        rs.getString("username"),
                        rs.getString("full_name"),
                        role,
                        rs.getString("phone"),
                        rs.getString("email"),
                        additionalInfo,
                        rs.getString("created_by"),
                        rs.getTimestamp("created_at") != null ?
                                rs.getTimestamp("created_at").toString() : "N/A"
                ));
            }
        } catch (SQLException e) {
            AlertUtils.showError("Database Error", "Failed to load users: " + e.getMessage());
        }
    }

    private void handleCreateUser() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();
        String fullName = txtFullName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String role = roleCombo.getValue();

        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || role == null) {
            AlertUtils.showWarning("Missing Data", "Please fill in all required fields.");
            return;
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        try {
            currentUser.getDbConnection().setAutoCommit(false);

            Integer companyId = null;
            Integer workshopId = null;
            Integer customerId = null;

            switch (role) {
                case "INSURANCE":
                    if (txtCompanyName == null || txtCompanyName.getText().trim().isEmpty()) {
                        AlertUtils.showWarning("Missing Data", "Please fill in company details.");
                        return;
                    }
                    String companySql = "INSERT INTO insurance_company (name, registration_number, phone, email, address) VALUES (?, ?, ?, ?, ?) RETURNING company_id";
                    try (PreparedStatement pstmt = currentUser.getDbConnection().prepareStatement(companySql)) {
                        pstmt.setString(1, txtCompanyName.getText().trim());
                        pstmt.setString(2, txtCompanyReg.getText().trim());
                        pstmt.setString(3, txtCompanyPhone.getText().trim());
                        pstmt.setString(4, txtCompanyEmail.getText().trim());
                        pstmt.setString(5, txtCompanyAddress.getText().trim());
                        ResultSet rs = pstmt.executeQuery();
                        if (rs.next()) {
                            companyId = rs.getInt(1);
                        }
                    }
                    break;

                case "WORKSHOP":
                    if (txtWorkshopName == null || txtWorkshopName.getText().trim().isEmpty()) {
                        AlertUtils.showWarning("Missing Data", "Please fill in workshop details.");
                        return;
                    }
                    String workshopSql = "INSERT INTO workshop (name, phone, email, address) VALUES (?, ?, ?, ?) RETURNING workshop_id";
                    try (PreparedStatement pstmt = currentUser.getDbConnection().prepareStatement(workshopSql)) {
                        pstmt.setString(1, txtWorkshopName.getText().trim());
                        pstmt.setString(2, txtWorkshopPhone.getText().trim());
                        pstmt.setString(3, txtWorkshopEmail.getText().trim());
                        pstmt.setString(4, txtWorkshopAddress.getText().trim());
                        ResultSet rs = pstmt.executeQuery();
                        if (rs.next()) {
                            workshopId = rs.getInt(1);
                        }
                    }
                    break;

                case "CUSTOMER":
                    String customerSql = "INSERT INTO customer (name, phone, email, address) VALUES (?, ?, ?, ?) RETURNING customer_id";
                    try (PreparedStatement pstmt = currentUser.getDbConnection().prepareStatement(customerSql)) {
                        pstmt.setString(1, fullName);
                        pstmt.setString(2, phone.isEmpty() ? null : phone);
                        pstmt.setString(3, email.isEmpty() ? null : email);
                        pstmt.setString(4, txtCustomerAddress != null ? txtCustomerAddress.getText().trim() : "");
                        ResultSet rs = pstmt.executeQuery();
                        if (rs.next()) {
                            customerId = rs.getInt(1);
                        }
                    }
                    break;
            }

            String userSql = """
                INSERT INTO app_user (username, password_hash, full_name, phone, email, user_type, 
                                      company_id, workshop_id, customer_id, created_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

            try (PreparedStatement pstmt = currentUser.getDbConnection().prepareStatement(userSql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, hashedPassword);
                pstmt.setString(3, fullName);
                pstmt.setString(4, phone.isEmpty() ? null : phone);
                pstmt.setString(5, email.isEmpty() ? null : email);
                pstmt.setString(6, role);
                pstmt.setObject(7, companyId);
                pstmt.setObject(8, workshopId);
                pstmt.setObject(9, customerId);
                pstmt.setInt(10, currentUser.getUserId());
                pstmt.executeUpdate();
            }

            // ===== CREATE POSTGRESQL DATABASE USER WITH FULL PERMISSIONS =====
            try {
                // Create the PostgreSQL user
                String createUserSql = "CREATE USER " + username + " WITH PASSWORD '" + password + "'";
                try (Statement stmt = currentUser.getDbConnection().createStatement()) {
                    stmt.execute(createUserSql);
                    System.out.println("✅ PostgreSQL user created: " + username);
                }

                // Grant CONNECT permission
                String grantConnectSql = "GRANT CONNECT ON DATABASE vehicle_db TO " + username;
                try (Statement stmt = currentUser.getDbConnection().createStatement()) {
                    stmt.execute(grantConnectSql);
                }

                // Grant SELECT on app_user (CRITICAL for login)
                String grantAppUserSql = "GRANT SELECT ON app_user TO " + username;
                try (Statement stmt = currentUser.getDbConnection().createStatement()) {
                    stmt.execute(grantAppUserSql);
                    System.out.println("✅ Granted SELECT on app_user to: " + username);
                }

                // Grant USAGE on ALL sequences (FIXES ALL SEQUENCE ERRORS)
                String grantAllSequencesSql = "GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO " + username;
                try (Statement stmt = currentUser.getDbConnection().createStatement()) {
                    stmt.execute(grantAllSequencesSql);
                    System.out.println("✅ Granted ALL sequence permissions to: " + username);
                }

                // Grant role-specific permissions
                if (role.equals("ADMIN")) {
                    String grantAdminSql = "GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO " + username;
                    try (Statement stmt = currentUser.getDbConnection().createStatement()) {
                        stmt.execute(grantAdminSql);
                    }
                } else if (role.equals("POLICE")) {
                    String[] grants = {
                            "GRANT SELECT ON vehicle TO " + username,
                            "GRANT SELECT, INSERT ON police_report TO " + username,
                            "GRANT SELECT, INSERT ON violation TO " + username,
                            "GRANT SELECT ON customer TO " + username
                    };
                    try (Statement stmt = currentUser.getDbConnection().createStatement()) {
                        for (String grant : grants) {
                            stmt.execute(grant);
                        }
                    }
                } else if (role.equals("WORKSHOP")) {
                    String[] grants = {
                            "GRANT SELECT, INSERT, UPDATE ON vehicle TO " + username,
                            "GRANT SELECT, INSERT, UPDATE ON service_record TO " + username,
                            "GRANT SELECT ON customer TO " + username
                    };
                    try (Statement stmt = currentUser.getDbConnection().createStatement()) {
                        for (String grant : grants) {
                            stmt.execute(grant);
                        }
                    }
                } else if (role.equals("INSURANCE")) {
                    String[] grants = {
                            "GRANT SELECT ON vehicle TO " + username,
                            "GRANT SELECT, INSERT, UPDATE ON insurance TO " + username,
                            "GRANT SELECT ON customer TO " + username
                    };
                    try (Statement stmt = currentUser.getDbConnection().createStatement()) {
                        for (String grant : grants) {
                            stmt.execute(grant);
                        }
                    }
                } else if (role.equals("CUSTOMER")) {
                    String[] grants = {
                            "GRANT SELECT ON vehicle TO " + username,
                            "GRANT SELECT, INSERT ON customer_query TO " + username,
                            "GRANT SELECT ON customer TO " + username,
                            "GRANT SELECT ON service_record TO " + username
                    };
                    try (Statement stmt = currentUser.getDbConnection().createStatement()) {
                        for (String grant : grants) {
                            stmt.execute(grant);
                        }
                    }
                }

                System.out.println("✅ All PostgreSQL permissions granted for: " + username);

            } catch (SQLException e) {
                System.err.println("⚠️ Warning: " + e.getMessage());
                // Don't rollback - app_user already created
            }

            currentUser.getDbConnection().commit();
            currentUser.getDbConnection().setAutoCommit(true);

            AlertUtils.showInfo("Success", "User created successfully!\n\nUsername: " + username + "\nPassword: " + password + "\n\nThey can now login to the system.");

            clearForm();
            loadUsers();

        } catch (SQLException e) {
            try {
                currentUser.getDbConnection().rollback();
                currentUser.getDbConnection().setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            AlertUtils.showError("Database Error", "Failed to create user: " + e.getMessage());
        }
    }

    private void clearForm() {
        txtUsername.clear();
        txtPassword.clear();
        txtFullName.clear();
        txtPhone.clear();
        txtEmail.clear();
        roleCombo.setValue(null);
        dynamicFormGrid.getChildren().clear();
    }

    public static class UserInfo {
        private final javafx.beans.property.StringProperty username;
        private final javafx.beans.property.StringProperty fullName;
        private final javafx.beans.property.StringProperty role;
        private final javafx.beans.property.StringProperty phone;
        private final javafx.beans.property.StringProperty email;
        private final javafx.beans.property.StringProperty additionalInfo;
        private final javafx.beans.property.StringProperty createdBy;
        private final javafx.beans.property.StringProperty createdAt;

        public UserInfo(String username, String fullName, String role, String phone, String email,
                        String additionalInfo, String createdBy, String createdAt) {
            this.username = new javafx.beans.property.SimpleStringProperty(username);
            this.fullName = new javafx.beans.property.SimpleStringProperty(fullName);
            this.role = new javafx.beans.property.SimpleStringProperty(role);
            this.phone = new javafx.beans.property.SimpleStringProperty(phone != null && !phone.isEmpty() ? phone : "N/A");
            this.email = new javafx.beans.property.SimpleStringProperty(email != null && !email.isEmpty() ? email : "N/A");
            this.additionalInfo = new javafx.beans.property.SimpleStringProperty(additionalInfo);
            this.createdBy = new javafx.beans.property.SimpleStringProperty(createdBy != null ? createdBy : "system");
            this.createdAt = new javafx.beans.property.SimpleStringProperty(createdAt);
        }

        public javafx.beans.property.StringProperty usernameProperty() { return username; }
        public javafx.beans.property.StringProperty fullNameProperty() { return fullName; }
        public javafx.beans.property.StringProperty roleProperty() { return role; }
        public javafx.beans.property.StringProperty phoneProperty() { return phone; }
        public javafx.beans.property.StringProperty emailProperty() { return email; }
        public javafx.beans.property.StringProperty additionalInfoProperty() { return additionalInfo; }
        public javafx.beans.property.StringProperty createdByProperty() { return createdBy; }
        public javafx.beans.property.StringProperty createdAtProperty() { return createdAt; }
    }
}