package com.example.vehicleidentificationsystem.models;

import java.sql.Connection;

public class InsuranceUser extends User {

    public InsuranceUser(int userId, String username, String fullName, Connection dbConnection) {
        super(userId, username, fullName, dbConnection);
    }

    @Override
    public String getRole() {
        return "INSURANCE";
    }

    @Override
    public String getRoleDisplayName() {
        return "Insurance Agent";
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
    public boolean canViewPoliceReports() { return false; }

    @Override
    public boolean canAddPoliceReports() { return false; }

    @Override
    public boolean canViewViolations() { return false; }

    @Override
    public boolean canAddViolations() { return false; }

    @Override
    public boolean canViewInsurance() { return true; }

    @Override
    public boolean canAddInsurance() { return true; }

    @Override
    public boolean canEditInsurance() { return true; }

    @Override
    public boolean canViewCustomerQueries() { return false; }

    @Override
    public boolean canAddCustomerQueries() { return false; }

    @Override
    public boolean canRespondToQueries() { return true; } // Insurance can respond to insurance queries

    @Override
    public boolean canManageUsers() { return false; }
}