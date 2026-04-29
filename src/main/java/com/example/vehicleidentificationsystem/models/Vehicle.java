package com.example.vehicleidentificationsystem.models;

import javafx.beans.property.*;

public class Vehicle {
    private final IntegerProperty vehicleId;
    private final StringProperty registrationNumber;
    private final StringProperty make;
    private final StringProperty model;
    private final IntegerProperty year;
    private final IntegerProperty ownerId;
    private final StringProperty ownerName;

    public Vehicle() {
        this.vehicleId = new SimpleIntegerProperty(0);
        this.registrationNumber = new SimpleStringProperty("");
        this.make = new SimpleStringProperty("");
        this.model = new SimpleStringProperty("");
        this.year = new SimpleIntegerProperty(0);
        this.ownerId = new SimpleIntegerProperty(0);
        this.ownerName = new SimpleStringProperty("");
    }

    public Vehicle(int vehicleId, String registrationNumber, String make, String model,
                   int year, int ownerId, String ownerName) {
        this.vehicleId = new SimpleIntegerProperty(vehicleId);
        this.registrationNumber = new SimpleStringProperty(registrationNumber);
        this.make = new SimpleStringProperty(make);
        this.model = new SimpleStringProperty(model);
        this.year = new SimpleIntegerProperty(year);
        this.ownerId = new SimpleIntegerProperty(ownerId);
        this.ownerName = new SimpleStringProperty(ownerName);
    }

    // Getters and Property getters for TableView
    public int getVehicleId() { return vehicleId.get(); }
    public IntegerProperty vehicleIdProperty() { return vehicleId; }

    public String getRegistrationNumber() { return registrationNumber.get(); }
    public StringProperty registrationNumberProperty() { return registrationNumber; }

    public String getMake() { return make.get(); }
    public StringProperty makeProperty() { return make; }

    public String getModel() { return model.get(); }
    public StringProperty modelProperty() { return model; }

    public int getYear() { return year.get(); }
    public IntegerProperty yearProperty() { return year; }

    public int getOwnerId() { return ownerId.get(); }
    public IntegerProperty ownerIdProperty() { return ownerId; }

    public String getOwnerName() { return ownerName.get(); }
    public StringProperty ownerNameProperty() { return ownerName; }

    // Setters
    public void setVehicleId(int id) { this.vehicleId.set(id); }
    public void setRegistrationNumber(String reg) { this.registrationNumber.set(reg); }
    public void setMake(String make) { this.make.set(make); }
    public void setModel(String model) { this.model.set(model); }
    public void setYear(int year) { this.year.set(year); }
    public void setOwnerId(int id) { this.ownerId.set(id); }
    public void setOwnerName(String name) { this.ownerName.set(name); }
}