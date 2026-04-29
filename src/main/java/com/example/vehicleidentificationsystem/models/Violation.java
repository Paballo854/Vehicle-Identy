package com.example.vehicleidentificationsystem.models;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Violation {
    private final IntegerProperty violationId;
    private final IntegerProperty vehicleId;
    private final StringProperty vehicleReg;
    private final ObjectProperty<LocalDate> violationDate;
    private final StringProperty violationType;
    private final DoubleProperty fineAmount;
    private final StringProperty officerName;

    public Violation() {
        this.violationId = new SimpleIntegerProperty(0);
        this.vehicleId = new SimpleIntegerProperty(0);
        this.vehicleReg = new SimpleStringProperty("");
        this.violationDate = new SimpleObjectProperty<>(LocalDate.now());
        this.violationType = new SimpleStringProperty("");
        this.fineAmount = new SimpleDoubleProperty(0);
        this.officerName = new SimpleStringProperty("");
    }

    public Violation(int violationId, int vehicleId, String vehicleReg, LocalDate violationDate,
                     String violationType, double fineAmount) {
        this.violationId = new SimpleIntegerProperty(violationId);
        this.vehicleId = new SimpleIntegerProperty(vehicleId);
        this.vehicleReg = new SimpleStringProperty(vehicleReg);
        this.violationDate = new SimpleObjectProperty<>(violationDate);
        this.violationType = new SimpleStringProperty(violationType);
        this.fineAmount = new SimpleDoubleProperty(fineAmount);
        this.officerName = new SimpleStringProperty("");
    }

    public Violation(int violationId, int vehicleId, String vehicleReg, LocalDate violationDate,
                     String violationType, double fineAmount, String officerName) {
        this.violationId = new SimpleIntegerProperty(violationId);
        this.vehicleId = new SimpleIntegerProperty(vehicleId);
        this.vehicleReg = new SimpleStringProperty(vehicleReg);
        this.violationDate = new SimpleObjectProperty<>(violationDate);
        this.violationType = new SimpleStringProperty(violationType);
        this.fineAmount = new SimpleDoubleProperty(fineAmount);
        this.officerName = new SimpleStringProperty(officerName);
    }

    public int getViolationId() { return violationId.get(); }
    public IntegerProperty violationIdProperty() { return violationId; }

    public int getVehicleId() { return vehicleId.get(); }
    public IntegerProperty vehicleIdProperty() { return vehicleId; }

    public String getVehicleReg() { return vehicleReg.get(); }
    public StringProperty vehicleRegProperty() { return vehicleReg; }

    public LocalDate getViolationDate() { return violationDate.get(); }
    public ObjectProperty<LocalDate> violationDateProperty() { return violationDate; }

    public String getViolationType() { return violationType.get(); }
    public StringProperty violationTypeProperty() { return violationType; }

    public double getFineAmount() { return fineAmount.get(); }
    public DoubleProperty fineAmountProperty() { return fineAmount; }

    public String getOfficerName() { return officerName.get(); }
    public StringProperty officerNameProperty() { return officerName; }

    public void setViolationId(int id) { this.violationId.set(id); }
    public void setVehicleId(int id) { this.vehicleId.set(id); }
    public void setVehicleReg(String reg) { this.vehicleReg.set(reg); }
    public void setViolationDate(LocalDate date) { this.violationDate.set(date); }
    public void setViolationType(String type) { this.violationType.set(type); }
    public void setFineAmount(double amount) { this.fineAmount.set(amount); }
    public void setOfficerName(String name) { this.officerName.set(name); }
}