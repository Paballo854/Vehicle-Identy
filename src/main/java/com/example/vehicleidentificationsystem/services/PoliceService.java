package com.example.vehicleidentificationsystem.services;

import com.example.vehicleidentificationsystem.models.*;
import com.example.vehicleidentificationsystem.utils.AlertUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PoliceService {

    public static List<PoliceReport> getAllPoliceReports(User currentUser) {
        List<PoliceReport> reports = new ArrayList<>();

        if (!currentUser.canViewPoliceReports()) {
            return reports;
        }

        Connection conn = currentUser.getDbConnection();
        String sql = """
            SELECT pr.report_id, pr.vehicle_id, v.registration_number, 
                   pr.report_date, pr.report_type, pr.description, pr.officer_name
            FROM police_report pr
            JOIN vehicle v ON pr.vehicle_id = v.vehicle_id
            ORDER BY pr.report_date DESC
        """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                reports.add(new PoliceReport(
                        rs.getInt("report_id"),
                        rs.getInt("vehicle_id"),
                        rs.getString("registration_number"),
                        rs.getDate("report_date").toLocalDate(),
                        rs.getString("report_type"),
                        rs.getString("description"),
                        rs.getString("officer_name")
                ));
            }
        } catch (SQLException e) {
            AlertUtils.showError("Database Error", "Failed to load police reports: " + e.getMessage());
        }

        return reports;
    }

    public static boolean addPoliceReport(User currentUser, PoliceReport report) {
        if (!currentUser.canAddPoliceReports()) {
            AlertUtils.showError("Permission Denied", "You don't have permission to add police reports.");
            return false;
        }

        Connection conn = currentUser.getDbConnection();
        String sql = "CALL add_police_report(?, ?, ?, ?)";

        try (CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, report.getVehicleId());
            cstmt.setString(2, report.getReportType());
            cstmt.setString(3, report.getDescription());
            cstmt.setString(4, report.getOfficerName());
            cstmt.execute();

            AlertUtils.showInfo("Success", "Police report added successfully!");
            return true;
        } catch (SQLException e) {
            AlertUtils.showError("Database Error", "Failed to add police report: " + e.getMessage());
            return false;
        }
    }

    public static List<Violation> getAllViolations(User currentUser) {
        List<Violation> violations = new ArrayList<>();

        if (!currentUser.canViewViolations()) {
            return violations;
        }

        Connection conn = currentUser.getDbConnection();
        String sql = """
            SELECT v.violation_id, v.vehicle_id, ve.registration_number,
                   v.violation_date, v.violation_type, v.fine_amount, v.officer_name
            FROM violation v
            JOIN vehicle ve ON v.vehicle_id = ve.vehicle_id
            ORDER BY v.violation_date DESC
        """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                violations.add(new Violation(
                        rs.getInt("violation_id"),
                        rs.getInt("vehicle_id"),
                        rs.getString("registration_number"),
                        rs.getDate("violation_date").toLocalDate(),
                        rs.getString("violation_type"),
                        rs.getDouble("fine_amount"),
                        rs.getString("officer_name") != null ? rs.getString("officer_name") : "Unknown Officer"
                ));
            }
        } catch (SQLException e) {
            AlertUtils.showError("Database Error", "Failed to load violations: " + e.getMessage());
        }

        return violations;
    }

    public static boolean addViolation(User currentUser, Violation violation) {
        if (!currentUser.canAddViolations()) {
            AlertUtils.showError("Permission Denied", "You don't have permission to add violations.");
            return false;
        }

        Connection conn = currentUser.getDbConnection();
        String sql = "INSERT INTO violation (vehicle_id, violation_date, violation_type, fine_amount, officer_name) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, violation.getVehicleId());
            pstmt.setDate(2, Date.valueOf(violation.getViolationDate()));
            pstmt.setString(3, violation.getViolationType());
            pstmt.setDouble(4, violation.getFineAmount());
            pstmt.setString(5, violation.getOfficerName());
            pstmt.executeUpdate();

            AlertUtils.showInfo("Success", "Violation added successfully!");
            return true;
        } catch (SQLException e) {
            AlertUtils.showError("Database Error", "Failed to add violation: " + e.getMessage());
            return false;
        }
    }
}