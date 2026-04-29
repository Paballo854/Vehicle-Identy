package com.example.vehicleidentificationsystem.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseService {

    private static final String HOST = "localhost";
    private static final String PORT = "5433";
    private static final String DATABASE = "vehicle_db";
    private static final String URL = "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DATABASE;

    private static final String SUPER_USER = "postgres";
    private static final String SUPER_PASSWORD = "PABALLO123";

    static {
        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("✅ PostgreSQL JDBC Driver registered successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ PostgreSQL JDBC Driver not found!");
            e.printStackTrace();
        }
    }

    public static Connection getConnection(String username, String password) throws SQLException {
        System.out.println("🔌 Attempting connection to: " + URL);
        System.out.println("   Username: " + username);
        Connection conn = DriverManager.getConnection(URL, username, password);
        System.out.println("✅ Connected to PostgreSQL as: " + username);
        return conn;
    }

    public static Connection getSuperUserConnection() throws SQLException {
        return getConnection(SUPER_USER, SUPER_PASSWORD);
    }

    public static boolean testConnection(String username, String password) {
        try (Connection conn = getConnection(username, password)) {
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Connection failed: " + e.getMessage());
            return false;
        }
    }

    public static boolean testSuperUserConnection() {
        return testConnection(SUPER_USER, SUPER_PASSWORD);
    }
}