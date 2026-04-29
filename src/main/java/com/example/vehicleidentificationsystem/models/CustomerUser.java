package com.example.vehicleidentificationsystem.models;

import java.sql.Connection;

public class CustomerUser extends User {

    public CustomerUser(int userId, String username, String fullName, Connection dbConnection) {
        super(userId, username, fullName, dbConnection);
    }

    @Override
    public String getRole() {
        return "CUSTOMER";
    }

    @Override
    public String getRoleDisplayName() {
        return "Customer";
    }

    @Override
    public boolean canViewVehicles() { return true; } // Only own vehicles (filtered in service)

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
    public boolean canViewInsurance() { return false; }

    @Override
    public boolean canAddInsurance() { return false; }

    @Override
    public boolean canEditInsurance() { return false; }

    @Override
    public boolean canViewCustomerQueries() { return true; } // Only own queries

    @Override
    public boolean canAddCustomerQueries() { return true; }

    @Override
    public boolean canRespondToQueries() { return false; }

    @Override
    public boolean canManageUsers() { return false; }
}