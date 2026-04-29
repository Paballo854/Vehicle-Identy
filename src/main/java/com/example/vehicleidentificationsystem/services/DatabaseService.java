package com.example.vehicleidentificationsystem.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseService {

    // ============ CLOUD DATABASE (Render) ============
    private static final String CLOUD_HOST = "dpg-d7ovr9beo5us738h8qk0-a.oregon-postgres.render.com";
    private static final String CLOUD_PORT = "5432";
    private static final String CLOUD_DATABASE = "vehicle_db_cqq4";
    private static final String CLOUD_USER = "vehicle_user";
    private static final String CLOUD_PASSWORD = "kSxyCKWiL8f5NNzKyG2PuAoJCA4pfvvA";
    private static final String CLOUD_URL = "jdbc:postgresql://" + CLOUD_HOST + ":" + CLOUD_PORT + "/" + CLOUD_DATABASE + "?sslmode=require";

    // ============ LOCAL DATABASE (Backup) ============
    private static final String LOCAL_HOST = "localhost";
    private static final String LOCAL_PORT = "5433";
    private static final String LOCAL_DATABASE = "vehicle_db";
    private static final String LOCAL_URL = "jdbc:postgresql://" + LOCAL_HOST + ":" + LOCAL_PORT + "/" + LOCAL_DATABASE;

    private static final String SUPER_USER = "postgres";
    private static final String SUPER_PASSWORD = "PABALLO123";

    // Which database to use? Set to true for cloud, false for local
    private static boolean USE_CLOUD = true;  // Change to false to use local database

    static {
        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("✅ PostgreSQL JDBC Driver registered successfully");
            System.out.println("🌐 Using " + (USE_CLOUD ? "CLOUD" : "LOCAL") + " database");
            if (USE_CLOUD) {
                System.out.println("   Cloud Host: " + CLOUD_HOST);
            } else {
                System.out.println("   Local Host: " + LOCAL_HOST + ":" + LOCAL_PORT);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("❌ PostgreSQL JDBC Driver not found!");
            e.printStackTrace();
        }
    }

    private static String getCurrentUrl() {
        return USE_CLOUD ? CLOUD_URL : LOCAL_URL;
    }

    public static Connection getConnection(String username, String password) throws SQLException {
        String url = getCurrentUrl();
        System.out.println("🔌 Attempting connection to: " + url);
        System.out.println("   Username: " + username);

        Connection conn;
        if (USE_CLOUD) {
            // Cloud database: use cloud credentials
            conn = DriverManager.getConnection(CLOUD_URL, CLOUD_USER, CLOUD_PASSWORD);
        } else {
            // Local database: use provided credentials
            conn = DriverManager.getConnection(LOCAL_URL, username, password);
        }

        System.out.println("✅ Connected to PostgreSQL as: " + (USE_CLOUD ? CLOUD_USER : username));
        return conn;
    }

    public static Connection getSuperUserConnection() throws SQLException {
        if (USE_CLOUD) {
            return getConnection(CLOUD_USER, CLOUD_PASSWORD);
        } else {
            return getConnection(SUPER_USER, SUPER_PASSWORD);
        }
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

    // Add this method to switch between cloud and local if needed
    public static void setUseCloud(boolean useCloud) {
        USE_CLOUD = useCloud;
        System.out.println("🔄 Switched to " + (USE_CLOUD ? "CLOUD" : "LOCAL") + " database mode");
    }

    public static boolean isUsingCloud() {
        return USE_CLOUD;
    }
}