package com.example.vehicleidentificationsystem.services;

import com.example.vehicleidentificationsystem.models.*;
import java.sql.*;

public class LoginService {

    public static User authenticate(String username, String password) {
        Connection userConn = null;

        try {
            userConn = DatabaseService.getConnection(username, password);

            String sql = "SELECT user_id, full_name, user_type, company_id, workshop_id FROM app_user WHERE username = ?";
            PreparedStatement pstmt = userConn.prepareStatement(sql);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("user_id");
                String fullName = rs.getString("full_name");
                String userType = rs.getString("user_type");
                int companyId = rs.getInt("company_id");
                int workshopId = rs.getInt("workshop_id");

                User user = createUserByType(userType, userId, username, fullName, userConn);

                if (user != null) {
                    user.setCompanyId(companyId);
                    user.setWorkshopId(workshopId);
                    System.out.println("✅ User authenticated: " + username + " as " + userType);
                    return user;
                }
            }

            if (userConn != null) {
                userConn.close();
            }
            return null;

        } catch (SQLException e) {
            System.err.println("❌ Authentication failed for user: " + username);
            System.err.println("   Error: " + e.getMessage());
            return null;
        }
    }

    private static User createUserByType(String userType, int userId, String username,
                                         String fullName, Connection conn) {
        switch (userType) {
            case "ADMIN":
                return new AdminUser(userId, username, fullName, conn);
            case "POLICE":
                return new PoliceUser(userId, username, fullName, conn);
            case "WORKSHOP":
                return new WorkshopUser(userId, username, fullName, conn);
            case "INSURANCE":
                return new InsuranceUser(userId, username, fullName, conn);
            case "CUSTOMER":
                return new CustomerUser(userId, username, fullName, conn);
            default:
                return null;
        }
    }
}