package com.example.vehicleidentificationsystem.models;

import java.sql.Connection;

public class WorkshopUser extends User {

    public WorkshopUser(int userId, String username, String fullName, Connection dbConnection) {
        super(userId, username, fullName, dbConnection);
    }

    @Override
    public String getRole() {
        return "WORKSHOP";
    }

    @Override
    public String getRoleDisplayName() {
        return "Workshop Manager";
    }

    @Override
    public boolean canViewVehicles() { return true; }

    @Override
    public boolean canAddVehicles() { return true; }

    @Override
    public boolean canEditVehicles() { return true; }

    @Override
    public boolean canDeleteVehicles() { return false; }

    @Override
    public boolean canViewServiceRecords() { return true; }

    @Override
    public boolean canAddServiceRecords() { return true; }

    @Override
    public boolean canEditServiceRecords() { return true; }

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
    public boolean canViewCustomerQueries() { return false; }

    @Override
    public boolean canAddCustomerQueries() { return false; }

    @Override
    public boolean canRespondToQueries() { return true; } // Workshop can respond to workshop queries

    @Override
    public boolean canManageUsers() { return false; }
}