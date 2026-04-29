package com.example.vehicleidentificationsystem.models;

import java.sql.Connection;

public class PoliceUser extends User {

    public PoliceUser(int userId, String username, String fullName, Connection dbConnection) {
        super(userId, username, fullName, dbConnection);
    }

    @Override
    public String getRole() {
        return "POLICE";
    }

    @Override
    public String getRoleDisplayName() {
        return "Police Officer";
    }

    @Override
    public boolean canViewVehicles() { return true; }

    @Override
    public boolean canAddVehicles() { return false; }

    @Override
    public boolean canEditVehicles() { return false; }

    @Override
    public boolean canDeleteVehicles() { return false; }

    @Override
    public boolean canViewServiceRecords() { return false; }

    @Override
    public boolean canAddServiceRecords() { return false; }

    @Override
    public boolean canEditServiceRecords() { return false; }

    @Override
    public boolean canViewPoliceReports() { return true; }

    @Override
    public boolean canAddPoliceReports() { return true; }

    @Override
    public boolean canViewViolations() { return true; }

    @Override
    public boolean canAddViolations() { return true; }

    @Override
    public boolean canViewInsurance() { return false; }

    @Override
    public boolean canAddInsurance() { return false; }

    @Override
    public boolean canEditInsurance() { return false; }

    @Override
    public boolean canViewCustomerQueries() { return false; }

    @Override
    public boolean canAddCustomerQueries() { return false; }

    @Override
    public boolean canRespondToQueries() { return false; }

    @Override
    public boolean canManageUsers() { return false; }
}