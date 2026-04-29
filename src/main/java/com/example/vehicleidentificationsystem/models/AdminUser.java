package com.example.vehicleidentificationsystem.models;

import java.sql.Connection;

public class AdminUser extends User {

    public AdminUser(int userId, String username, String fullName, Connection dbConnection) {
        super(userId, username, fullName, dbConnection);
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }

    @Override
    public String getRoleDisplayName() {
        return "System Administrator";
    }

    @Override
    public boolean canViewVehicles() { return true; }

    @Override
    public boolean canAddVehicles() { return true; }

    @Override
    public boolean canEditVehicles() { return true; }

    @Override
    public boolean canDeleteVehicles() { return true; }

    @Override
    public boolean canViewServiceRecords() { return true; }

    @Override
    public boolean canAddServiceRecords() { return true; }

    @Override
    public boolean canEditServiceRecords() { return true; }

    @Override
    public boolean canViewPoliceReports() { return true; }

    @Override
    public boolean canAddPoliceReports() { return true; }

    @Override
    public boolean canViewViolations() { return true; }

    @Override
    public boolean canAddViolations() { return true; }

    @Override
    public boolean canViewInsurance() { return true; }

    @Override
    public boolean canAddInsurance() { return true; }

    @Override
    public boolean canEditInsurance() { return true; }

    @Override
    public boolean canViewCustomerQueries() { return true; }

    @Override
    public boolean canAddCustomerQueries() { return true; }

    @Override
    public boolean canRespondToQueries() { return true; }

    @Override
    public boolean canManageUsers() { return true; }
}