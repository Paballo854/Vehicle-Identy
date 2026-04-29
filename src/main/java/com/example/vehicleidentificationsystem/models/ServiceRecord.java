package com.example.vehicleidentificationsystem.models;

import javafx.beans.property.*;
import java.time.LocalDate;

public class ServiceRecord {
    private final IntegerProperty serviceId;
    private final IntegerProperty vehicleId;
    private final StringProperty vehicleReg;
    private final ObjectProperty<LocalDate> serviceDate;
    private final StringProperty serviceType;
    private final StringProperty description;
    private final DoubleProperty cost;
    private final IntegerProperty workshopId;
    private final StringProperty workshopName;

    public ServiceRecord() {
        this.serviceId = new SimpleIntegerProperty(0);
        this.vehicleId = new SimpleIntegerProperty(0);
        this.vehicleReg = new SimpleStringProperty("");
        this.serviceDate = new SimpleObjectProperty<>(LocalDate.now());
        this.serviceType = new SimpleStringProperty("");
        this.description = new SimpleStringProperty("");
        this.cost = new SimpleDoubleProperty(0);
        this.workshopId = new SimpleIntegerProperty(0);
        this.workshopName = new SimpleStringProperty("");
    }

    public ServiceRecord(int serviceId, int vehicleId, String vehicleReg, LocalDate serviceDate,
                         String serviceType, String description, double cost,
                         int workshopId, String workshopName) {
        this.serviceId = new SimpleIntegerProperty(serviceId);
        this.vehicleId = new SimpleIntegerProperty(vehicleId);
        this.vehicleReg = new SimpleStringProperty(vehicleReg);
        this.serviceDate = new SimpleObjectProperty<>(serviceDate);
        this.serviceType = new SimpleStringProperty(serviceType);
        this.description = new SimpleStringProperty(description);
        this.cost = new SimpleDoubleProperty(cost);
        this.workshopId = new SimpleIntegerProperty(workshopId);
        this.workshopName = new SimpleStringProperty(workshopName);
    }

    // Getters and Property getters
    public int getServiceId() { return serviceId.get(); }
    public IntegerProperty serviceIdProperty() { return serviceId; }

    public int getVehicleId() { return vehicleId.get(); }
    public IntegerProperty vehicleIdProperty() { return vehicleId; }

    public String getVehicleReg() { return vehicleReg.get(); }
    public StringProperty vehicleRegProperty() { return vehicleReg; }

    public LocalDate getServiceDate() { return serviceDate.get(); }
    public ObjectProperty<LocalDate> serviceDateProperty() { return serviceDate; }

    public String getServiceType() { return serviceType.get(); }
    public StringProperty serviceTypeProperty() { return serviceType; }

    public String getDescription() { return description.get(); }
    public StringProperty descriptionProperty() { return description; }

    public double getCost() { return cost.get(); }
    public DoubleProperty costProperty() { return cost; }

    public int getWorkshopId() { return workshopId.get(); }
    public IntegerProperty workshopIdProperty() { return workshopId; }

    public String getWorkshopName() { return workshopName.get(); }
    public StringProperty workshopNameProperty() { return workshopName; }

    // Setters
    public void setServiceId(int id) { this.serviceId.set(id); }
    public void setVehicleId(int id) { this.vehicleId.set(id); }
    public void setVehicleReg(String reg) { this.vehicleReg.set(reg); }
    public void setServiceDate(LocalDate date) { this.serviceDate.set(date); }
    public void setServiceType(String type) { this.serviceType.set(type); }
    public void setDescription(String desc) { this.description.set(desc); }
    public void setCost(double cost) { this.cost.set(cost); }
    public void setWorkshopId(int id) { this.workshopId.set(id); }
    public void setWorkshopName(String name) { this.workshopName.set(name); }
}