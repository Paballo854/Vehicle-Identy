module com.example.vehicleidentificationsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.postgresql.jdbc;
    requires jbcrypt;

    opens com.example.vehicleidentificationsystem to javafx.fxml;
    opens com.example.vehicleidentificationsystem.controllers to javafx.fxml;
    opens com.example.vehicleidentificationsystem.models to javafx.base;

    exports com.example.vehicleidentificationsystem;
    exports com.example.vehicleidentificationsystem.controllers;
    exports com.example.vehicleidentificationsystem.models;
    exports com.example.vehicleidentificationsystem.services;
}