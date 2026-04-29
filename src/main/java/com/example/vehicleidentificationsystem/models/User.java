package com.example.vehicleidentificationsystem.models;

import java.sql.Connection;

public abstract class User {
    protected int userId;
    protected String username;
    protected String fullName;
    protected Connection dbConnection;
    protected int companyId;
    protected int workshopId;

    public User(int userId, String username, String fullName, Connection dbConnection) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.dbConnection = dbConnection;
        this.companyId = 0;
        this.workshopId = 0;
    }

    // Abstract methods - Each role implements differently (Polymorphism)
    public abstract String getRole();
    public abstract String getRoleDisplayName();

    // Permission methods
    public abstract boolean canViewVehicles();
    public abstract boolean canAddVehicles();
    public abstract boolean canEditVehicles();
    public abstract boolean canDeleteVehicles();

    public abstract boolean canViewServiceRecords();
    public abstract boolean canAddServiceRecords();
    public abstract boolean canEditServiceRecords();

    public abstract boolean canViewPoliceReports();
    public abstract boolean canAddPoliceReports();

    public abstract boolean canViewViolations();
    public abstract boolean canAddViolations();

    public abstract boolean canViewInsurance();
    public abstract boolean canAddInsurance();
    public abstract boolean canEditInsurance();

    public abstract boolean canViewCustomerQueries();
    public abstract boolean canAddCustomerQueries();
    public abstract boolean canRespondToQueries();

    public abstract boolean canManageUsers();

    // Getters
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public Connection getDbConnection() { return dbConnection; }
    public int getCompanyId() { return companyId; }
    public int getWorkshopId() { return workshopId; }

    // Setters
    public void setCompanyId(int companyId) { this.companyId = companyId; }
    public void setWorkshopId(int workshopId) { this.workshopId = workshopId; }

    @Override
    public String toString() {
        return fullName + " (" + getRoleDisplayName() + ")";
    }
}