package com.example.vehicleidentificationsystem.utils;

import com.example.vehicleidentificationsystem.models.User;

public class PrivilegeUtils {

    public static void configureButtonByPrivilege(javafx.scene.control.Button button, boolean hasPrivilege) {
        button.setVisible(hasPrivilege);
        button.setManaged(hasPrivilege);
    }

    public static void configureTabByPrivilege(javafx.scene.control.Tab tab, boolean hasPrivilege) {
        tab.setDisable(!hasPrivilege);
        if (!hasPrivilege) {
            tab.setTooltip(new javafx.scene.control.Tooltip("You don't have permission to access this module"));
        }
    }

    public static String getPermissionMessage(User user, String action) {
        return user.getFullName() + " (" + user.getRoleDisplayName() +
                ") does not have permission to " + action;
    }
}