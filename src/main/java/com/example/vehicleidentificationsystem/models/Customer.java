package com.example.vehicleidentificationsystem.models;

import javafx.beans.property.*;

public class Customer {
    private final IntegerProperty customerId;
    private final StringProperty name;
    private final StringProperty address;
    private final StringProperty phone;
    private final StringProperty email;

    public Customer() {
        this.customerId = new SimpleIntegerProperty(0);
        this.name = new SimpleStringProperty("");
        this.address = new SimpleStringProperty("");
        this.phone = new SimpleStringProperty("");
        this.email = new SimpleStringProperty("");
    }

    public Customer(int customerId, String name, String address, String phone, String email) {
        this.customerId = new SimpleIntegerProperty(customerId);
        this.name = new SimpleStringProperty(name);
        this.address = new SimpleStringProperty(address);
        this.phone = new SimpleStringProperty(phone);
        this.email = new SimpleStringProperty(email);
    }

    // Getters and Property getters
    public int getCustomerId() { return customerId.get(); }
    public IntegerProperty customerIdProperty() { return customerId; }

    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }

    public String getAddress() { return address.get(); }
    public StringProperty addressProperty() { return address; }

    public String getPhone() { return phone.get(); }
    public StringProperty phoneProperty() { return phone; }

    public String getEmail() { return email.get(); }
    public StringProperty emailProperty() { return email; }

    // Setters
    public void setCustomerId(int id) { this.customerId.set(id); }
    public void setName(String name) { this.name.set(name); }
    public void setAddress(String address) { this.address.set(address); }
    public void setPhone(String phone) { this.phone.set(phone); }
    public void setEmail(String email) { this.email.set(email); }
}