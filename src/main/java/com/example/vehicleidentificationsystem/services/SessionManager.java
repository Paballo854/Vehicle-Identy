package com.example.vehicleidentificationsystem.services;

import com.example.vehicleidentificationsystem.models.User;
import java.sql.Connection;
import java.sql.SQLException;

public class SessionManager {

    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Connection getCurrentConnection() {
        if (currentUser != null) {
            return currentUser.getDbConnection();
        }
        return null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public void logout() {
        if (currentUser != null && currentUser.getDbConnection() != null) {
            try {
                currentUser.getDbConnection().close();
                System.out.println("✅ Database connection closed for user: " + currentUser.getUsername());
            } catch (SQLException e) {
                System.err.println("❌ Error closing connection: " + e.getMessage());
            }
        }
        currentUser = null;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.getRole().equals("ADMIN");
    }

    public boolean isPolice() {
        return currentUser != null && currentUser.getRole().equals("POLICE");
    }

    public boolean isWorkshop() {
        return currentUser != null && currentUser.getRole().equals("WORKSHOP");
    }

    public boolean isInsurance() {
        return currentUser != null && currentUser.getRole().equals("INSURANCE");
    }

    public boolean isCustomer() {
        return currentUser != null && currentUser.getRole().equals("CUSTOMER");
    }
}