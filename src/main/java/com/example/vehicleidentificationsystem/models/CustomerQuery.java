package com.example.vehicleidentificationsystem.models;

import javafx.beans.property.*;
import java.time.LocalDate;

public class CustomerQuery {
    private final IntegerProperty queryId;
    private final IntegerProperty customerId;
    private final StringProperty customerName;
    private final IntegerProperty vehicleId;
    private final StringProperty vehicleReg;
    private final ObjectProperty<LocalDate> queryDate;
    private final StringProperty queryText;
    private final StringProperty responseText;
    private final StringProperty queryType;
    private final IntegerProperty targetId;
    private final StringProperty targetType;
    private final StringProperty targetName;

    public CustomerQuery() {
        this.queryId = new SimpleIntegerProperty(0);
        this.customerId = new SimpleIntegerProperty(0);
        this.customerName = new SimpleStringProperty("");
        this.vehicleId = new SimpleIntegerProperty(0);
        this.vehicleReg = new SimpleStringProperty("");
        this.queryDate = new SimpleObjectProperty<>(LocalDate.now());
        this.queryText = new SimpleStringProperty("");
        this.responseText = new SimpleStringProperty("");
        this.queryType = new SimpleStringProperty("WORKSHOP");
        this.targetId = new SimpleIntegerProperty(0);
        this.targetType = new SimpleStringProperty("");
        this.targetName = new SimpleStringProperty("");
    }

    public CustomerQuery(int queryId, int customerId, String customerName, int vehicleId,
                         String vehicleReg, LocalDate queryDate, String queryText,
                         String responseText, String queryType, int targetId,
                         String targetType, String targetName) {
        this.queryId = new SimpleIntegerProperty(queryId);
        this.customerId = new SimpleIntegerProperty(customerId);
        this.customerName = new SimpleStringProperty(customerName);
        this.vehicleId = new SimpleIntegerProperty(vehicleId);
        this.vehicleReg = new SimpleStringProperty(vehicleReg);
        this.queryDate = new SimpleObjectProperty<>(queryDate);
        this.queryText = new SimpleStringProperty(queryText);
        this.responseText = new SimpleStringProperty(responseText);
        this.queryType = new SimpleStringProperty(queryType);
        this.targetId = new SimpleIntegerProperty(targetId);
        this.targetType = new SimpleStringProperty(targetType);
        this.targetName = new SimpleStringProperty(targetName);
    }

    // Getters and Property getters
    public int getQueryId() { return queryId.get(); }
    public IntegerProperty queryIdProperty() { return queryId; }

    public int getCustomerId() { return customerId.get(); }
    public IntegerProperty customerIdProperty() { return customerId; }

    public String getCustomerName() { return customerName.get(); }
    public StringProperty customerNameProperty() { return customerName; }

    public int getVehicleId() { return vehicleId.get(); }
    public IntegerProperty vehicleIdProperty() { return vehicleId; }

    public String getVehicleReg() { return vehicleReg.get(); }
    public StringProperty vehicleRegProperty() { return vehicleReg; }

    public LocalDate getQueryDate() { return queryDate.get(); }
    public ObjectProperty<LocalDate> queryDateProperty() { return queryDate; }

    public String getQueryText() { return queryText.get(); }
    public StringProperty queryTextProperty() { return queryText; }

    public String getResponseText() { return responseText.get(); }
    public StringProperty responseTextProperty() { return responseText; }

    public String getQueryType() { return queryType.get(); }
    public StringProperty queryTypeProperty() { return queryType; }

    public int getTargetId() { return targetId.get(); }
    public IntegerProperty targetIdProperty() { return targetId; }

    public String getTargetType() { return targetType.get(); }
    public StringProperty targetTypeProperty() { return targetType; }

    public String getTargetName() { return targetName.get(); }
    public StringProperty targetNameProperty() { return targetName; }

    // Setters
    public void setQueryId(int id) { this.queryId.set(id); }
    public void setCustomerId(int id) { this.customerId.set(id); }
    public void setCustomerName(String name) { this.customerName.set(name); }
    public void setVehicleId(int id) { this.vehicleId.set(id); }
    public void setVehicleReg(String reg) { this.vehicleReg.set(reg); }
    public void setQueryDate(LocalDate date) { this.queryDate.set(date); }
    public void setQueryText(String text) { this.queryText.set(text); }
    public void setResponseText(String text) { this.responseText.set(text); }
    public void setQueryType(String type) { this.queryType.set(type); }
    public void setTargetId(int id) { this.targetId.set(id); }
    public void setTargetType(String type) { this.targetType.set(type); }
    public void setTargetName(String name) { this.targetName.set(name); }
}