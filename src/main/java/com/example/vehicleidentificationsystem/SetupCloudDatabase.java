package com.example.vehicleidentificationsystem;

import java.sql.Connection;
import java.sql.Statement;
import com.example.vehicleidentificationsystem.services.DatabaseService;

public class SetupCloudDatabase {
    public static void main(String[] args) {
        System.out.println("☁️ Setting up Cloud Database...");

        try (Connection conn = DatabaseService.getSuperUserConnection();
             Statement stmt = conn.createStatement()) {

            // Create users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "username VARCHAR(50) PRIMARY KEY," +
                    "password VARCHAR(255) NOT NULL," +
                    "role VARCHAR(20) NOT NULL," +
                    "full_name VARCHAR(100)" +
                    ")");
            System.out.println("✓ users table created");

            // Create vehicles table
            stmt.execute("CREATE TABLE IF NOT EXISTS vehicles (" +
                    "vehicle_id SERIAL PRIMARY KEY," +
                    "registration_number VARCHAR(50)," +
                    "make VARCHAR(50)," +
                    "model VARCHAR(50)," +
                    "year INT," +
                    "owner_username VARCHAR(50)" +
                    ")");
            System.out.println("✓ vehicles table created");

            // Insert test users (plain passwords for simplicity)
            stmt.execute("DELETE FROM users");
            stmt.execute("INSERT INTO users VALUES " +
                    "('admin_user', 'admin123', 'ADMIN', 'Administrator')," +
                    "('thabo', 'customer123', 'CUSTOMER', 'Thabo Molefe')," +
                    "('theko', 'workshop123', 'WORKSHOP', 'Theko Workshop')");
            System.out.println("✓ Test users added");

            // Insert test vehicles
            stmt.execute("DELETE FROM vehicles");
            stmt.execute("INSERT INTO vehicles (registration_number, make, model, year, owner_username) VALUES " +
                    "('ABC123', 'Toyota', 'Corolla', 2020, 'thabo')," +
                    "('XYZ789', 'Volkswagen', 'Golf', 2022, 'thabo')");
            System.out.println("✓ Test vehicles added");

            System.out.println("\n✅ Cloud Database Setup Complete!");
            System.out.println("=================================");
            System.out.println("Test Logins:");
            System.out.println("  Admin:    admin_user / admin123");
            System.out.println("  Customer: thabo / customer123");
            System.out.println("  Workshop: theko / workshop123");

        } catch (Exception e) {
            System.err.println("❌ Error setting up cloud database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}