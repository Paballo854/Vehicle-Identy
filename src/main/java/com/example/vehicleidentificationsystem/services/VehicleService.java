package com.example.vehicleidentificationsystem.services;

import com.example.vehicleidentificationsystem.models.*;
import com.example.vehicleidentificationsystem.utils.AlertUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehicleService {

    public static List<Vehicle> getAllVehicles(User currentUser) {
        List<Vehicle> vehicles = new ArrayList<>();

        if (!currentUser.canViewVehicles()) {
            return vehicles;
        }

        Connection conn = currentUser.getDbConnection();

        String sql;
        if (currentUser.getRole().equals("CUSTOMER")) {
            sql = """
                SELECT v.vehicle_id, v.registration_number, v.make, v.model, v.year, 
                       v.owner_id, c.name as owner_name
                FROM vehicle v
                LEFT JOIN customer c ON v.owner_id = c.customer_id
                WHERE v.owner_id = (SELECT customer_id FROM customer WHERE email LIKE ? OR name LIKE ?)
                ORDER BY v.vehicle_id
            """;
        } else {
            sql = """
                SELECT v.vehicle_id, v.registration_number, v.make, v.model, v.year, 
                       v.owner_id, c.name as owner_name
                FROM vehicle v
                LEFT JOIN customer c ON v.owner_id = c.customer_id
                ORDER BY v.vehicle_id
            """;
        }

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (currentUser.getRole().equals("CUSTOMER")) {
                String searchPattern = "%" + currentUser.getUsername().replace("_user", "") + "%";
                pstmt.setString(1, searchPattern);
                pstmt.setString(2, searchPattern);
            }
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                vehicles.add(new Vehicle(
                        rs.getInt("vehicle_id"),
                        rs.getString("registration_number"),
                        rs.getString("make"),
                        rs.getString("model"),
                        rs.getInt("year"),
                        rs.getInt("owner_id"),
                        rs.getString("owner_name")
                ));
            }
            System.out.println("✅ Loaded " + vehicles.size() + " vehicles for user: " + currentUser.getUsername());
        } catch (SQLException e) {
            AlertUtils.showError("Database Error", "Failed to load vehicles: " + e.getMessage());
        }

        return vehicles;
    }

    public static boolean addVehicle(User currentUser, Vehicle vehicle) {
        if (!currentUser.canAddVehicles()) {
            AlertUtils.showError("Permission Denied", "You don't have permission to add vehicles.");
            return false;
        }

        Connection conn = currentUser.getDbConnection();
        String sql = "CALL add_vehicle(?, ?, ?, ?, ?)";

        try (CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setString(1, vehicle.getRegistrationNumber());
            cstmt.setString(2, vehicle.getMake());
            cstmt.setString(3, vehicle.getModel());
            cstmt.setInt(4, vehicle.getYear());
            cstmt.setInt(5, vehicle.getOwnerId());
            cstmt.execute();

            AlertUtils.showInfo("Success", "Vehicle added successfully!");
            System.out.println("✅ Vehicle added by: " + currentUser.getUsername());
            return true;
        } catch (SQLException e) {
            AlertUtils.showError("Database Error", "Failed to add vehicle: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateVehicle(User currentUser, Vehicle vehicle) {
        if (!currentUser.canEditVehicles()) {
            AlertUtils.showError("Permission Denied", "You don't have permission to edit vehicles.");
            return false;
        }

        Connection conn = currentUser.getDbConnection();
        String sql = """
            UPDATE vehicle 
            SET registration_number = ?, make = ?, model = ?, year = ?, owner_id = ?
            WHERE vehicle_id = ?
        """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, vehicle.getRegistrationNumber());
            pstmt.setString(2, vehicle.getMake());
            pstmt.setString(3, vehicle.getModel());
            pstmt.setInt(4, vehicle.getYear());
            pstmt.setInt(5, vehicle.getOwnerId());
            pstmt.setInt(6, vehicle.getVehicleId());
            pstmt.executeUpdate();

            AlertUtils.showInfo("Success", "Vehicle updated successfully!");
            System.out.println("✅ Vehicle updated by: " + currentUser.getUsername());
            return true;
        } catch (SQLException e) {
            AlertUtils.showError("Database Error", "Failed to update vehicle: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteVehicle(User currentUser, int vehicleId) {
        if (!currentUser.canDeleteVehicles()) {
            AlertUtils.showError("Permission Denied", "You don't have permission to delete vehicles.");
            return false;
        }

        Connection conn = currentUser.getDbConnection();
        String sql = "DELETE FROM vehicle WHERE vehicle_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, vehicleId);
            int deleted = pstmt.executeUpdate();

            AlertUtils.showInfo("Success", "Vehicle deleted successfully!");
            System.out.println("✅ Vehicle deleted by: " + currentUser.getUsername());
            return true;
        } catch (SQLException e) {
            AlertUtils.showError("Database Error", "Failed to delete vehicle: " + e.getMessage());
            return false;
        }
    }

    public static List<Customer> getAllCustomers(User currentUser) {
        List<Customer> customers = new ArrayList<>();

        Connection conn = currentUser.getDbConnection();
        String sql = "SELECT customer_id, name, address, phone, email FROM customer ORDER BY name";

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                customers.add(new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("phone"),
                        rs.getString("email")
                ));
            }
        } catch (SQLException e) {
            AlertUtils.showError("Database Error", "Failed to load customers: " + e.getMessage());
        }

        return customers;
    }
}