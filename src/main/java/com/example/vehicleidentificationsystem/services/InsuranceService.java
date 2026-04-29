package com.example.vehicleidentificationsystem.services;

import com.example.vehicleidentificationsystem.models.*;
import com.example.vehicleidentificationsystem.utils.AlertUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InsuranceService {

    public static List<Insurance> getAllInsurance(User currentUser) {
        List<Insurance> insuranceList = new ArrayList<>();

        if (!currentUser.canViewInsurance()) {
            return insuranceList;
        }

        Connection conn = currentUser.getDbConnection();

        String sql;

        if (currentUser.getRole().equals("INSURANCE")) {
            sql = """
                SELECT i.insurance_id, i.vehicle_id, v.registration_number,
                       i.policy_number, i.provider, i.insurance_type,
                       i.start_date, i.expiry_date, i.status, i.premium_amount,
                       i.company_id, ic.name as company_name
                FROM insurance i
                JOIN vehicle v ON i.vehicle_id = v.vehicle_id
                LEFT JOIN insurance_company ic ON i.company_id = ic.company_id
                WHERE i.company_id = (SELECT company_id FROM app_user WHERE user_id = ?)
                ORDER BY i.expiry_date ASC
            """;
        } else if (currentUser.getRole().equals("ADMIN")) {
            sql = """
                SELECT i.insurance_id, i.vehicle_id, v.registration_number,
                       i.policy_number, i.provider, i.insurance_type,
                       i.start_date, i.expiry_date, i.status, i.premium_amount,
                       i.company_id, ic.name as company_name
                FROM insurance i
                JOIN vehicle v ON i.vehicle_id = v.vehicle_id
                LEFT JOIN insurance_company ic ON i.company_id = ic.company_id
                ORDER BY i.expiry_date ASC
            """;
        } else {
            sql = """
                SELECT i.insurance_id, i.vehicle_id, v.registration_number,
                       i.policy_number, i.provider, i.insurance_type,
                       i.start_date, i.expiry_date, i.status, i.premium_amount,
                       i.company_id, ic.name as company_name
                FROM insurance i
                JOIN vehicle v ON i.vehicle_id = v.vehicle_id
                LEFT JOIN insurance_company ic ON i.company_id = ic.company_id
                ORDER BY i.expiry_date ASC
            """;
        }

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (currentUser.getRole().equals("INSURANCE")) {
                pstmt.setInt(1, currentUser.getUserId());
            }
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                insuranceList.add(new Insurance(
                        rs.getInt("insurance_id"),
                        rs.getInt("vehicle_id"),
                        rs.getString("registration_number"),
                        rs.getString("policy_number"),
                        rs.getString("provider"),
                        rs.getString("insurance_type"),
                        rs.getDate("start_date").toLocalDate(),
                        rs.getDate("expiry_date").toLocalDate(),
                        rs.getString("status"),
                        rs.getDouble("premium_amount"),
                        rs.getInt("company_id"),
                        rs.getString("company_name") != null ? rs.getString("company_name") : "Unknown"
                ));
            }
        } catch (SQLException e) {
            AlertUtils.showError("Database Error", "Failed to load insurance records: " + e.getMessage());
        }

        return insuranceList;
    }

    public static boolean addInsurance(User currentUser, Insurance insurance) {
        if (!currentUser.canAddInsurance()) {
            AlertUtils.showError("Permission Denied", "You don't have permission to add insurance records.");
            return false;
        }

        Connection conn = currentUser.getDbConnection();

        try {
            String checkSql = "SELECT COUNT(*) FROM insurance WHERE vehicle_id = ? AND status = 'Active'";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, insurance.getVehicleId());
            ResultSet rs = checkStmt.executeQuery();
            rs.next();

            if (rs.getInt(1) > 0 && insurance.getStatus().equals("Active")) {
                AlertUtils.showError("Cannot Add", "This vehicle already has an active insurance policy. Please expire it first.");
                return false;
            }
        } catch (SQLException e) {
            AlertUtils.showError("Database Error", "Failed to check existing insurance: " + e.getMessage());
            return false;
        }

        String sql = """
            INSERT INTO insurance (vehicle_id, policy_number, provider, insurance_type, 
                                   start_date, expiry_date, status, premium_amount, company_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, insurance.getVehicleId());
            pstmt.setString(2, insurance.getPolicyNumber());
            pstmt.setString(3, insurance.getProvider());
            pstmt.setString(4, insurance.getInsuranceType());
            pstmt.setDate(5, Date.valueOf(insurance.getStartDate()));
            pstmt.setDate(6, Date.valueOf(insurance.getExpiryDate()));
            pstmt.setString(7, insurance.getStatus());
            pstmt.setDouble(8, insurance.getPremiumAmount());

            if (currentUser.getRole().equals("INSURANCE")) {
                String companySql = "SELECT company_id FROM app_user WHERE user_id = ?";
                PreparedStatement companyStmt = conn.prepareStatement(companySql);
                companyStmt.setInt(1, currentUser.getUserId());
                ResultSet companyRs = companyStmt.executeQuery();
                if (companyRs.next()) {
                    pstmt.setInt(9, companyRs.getInt("company_id"));
                } else {
                    pstmt.setNull(9, java.sql.Types.INTEGER);
                }
            } else {
                pstmt.setNull(9, java.sql.Types.INTEGER);
            }

            pstmt.executeUpdate();

            AlertUtils.showInfo("Success", "Insurance record added successfully!");
            return true;
        } catch (SQLException e) {
            AlertUtils.showError("Database Error", "Failed to add insurance: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateInsurance(User currentUser, Insurance insurance) {
        if (!currentUser.canEditInsurance()) {
            AlertUtils.showError("Permission Denied", "You don't have permission to edit insurance records.");
            return false;
        }

        Connection conn = currentUser.getDbConnection();

        String sql = """
            UPDATE insurance 
            SET policy_number = ?, provider = ?, insurance_type = ?,
                start_date = ?, expiry_date = ?, status = ?, premium_amount = ?
            WHERE insurance_id = ?
        """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, insurance.getPolicyNumber());
            pstmt.setString(2, insurance.getProvider());
            pstmt.setString(3, insurance.getInsuranceType());
            pstmt.setDate(4, Date.valueOf(insurance.getStartDate()));
            pstmt.setDate(5, Date.valueOf(insurance.getExpiryDate()));
            pstmt.setString(6, insurance.getStatus());
            pstmt.setDouble(7, insurance.getPremiumAmount());
            pstmt.setInt(8, insurance.getInsuranceId());
            pstmt.executeUpdate();

            AlertUtils.showInfo("Success", "Insurance record updated successfully!");
            return true;
        } catch (SQLException e) {
            AlertUtils.showError("Database Error", "Failed to update insurance: " + e.getMessage());
            return false;
        }
    }
}