package com.example.vehicleidentificationsystem.models;

import javafx.beans.property.*;
import java.time.LocalDate;

public class PoliceReport {
    private final IntegerProperty reportId;
    private final IntegerProperty vehicleId;
    private final StringProperty vehicleReg;
    private final ObjectProperty<LocalDate> reportDate;
    private final StringProperty reportType;
    private final StringProperty description;
    private final StringProperty officerName;

    public PoliceReport() {
        this.reportId = new SimpleIntegerProperty(0);
        this.vehicleId = new SimpleIntegerProperty(0);
        this.vehicleReg = new SimpleStringProperty("");
        this.reportDate = new SimpleObjectProperty<>(LocalDate.now());
        this.reportType = new SimpleStringProperty("");
        this.description = new SimpleStringProperty("");
        this.officerName = new SimpleStringProperty("");
    }

    public PoliceReport(int reportId, int vehicleId, String vehicleReg, LocalDate reportDate,
                        String reportType, String description, String officerName) {
        this.reportId = new SimpleIntegerProperty(reportId);
        this.vehicleId = new SimpleIntegerProperty(vehicleId);
        this.vehicleReg = new SimpleStringProperty(vehicleReg);
        this.reportDate = new SimpleObjectProperty<>(reportDate);
        this.reportType = new SimpleStringProperty(reportType);
        this.description = new SimpleStringProperty(description);
        this.officerName = new SimpleStringProperty(officerName);
    }

    // Getters and Property getters
    public int getReportId() { return reportId.get(); }
    public IntegerProperty reportIdProperty() { return reportId; }

    public int getVehicleId() { return vehicleId.get(); }
    public IntegerProperty vehicleIdProperty() { return vehicleId; }

    public String getVehicleReg() { return vehicleReg.get(); }
    public StringProperty vehicleRegProperty() { return vehicleReg; }

    public LocalDate getReportDate() { return reportDate.get(); }
    public ObjectProperty<LocalDate> reportDateProperty() { return reportDate; }

    public String getReportType() { return reportType.get(); }
    public StringProperty reportTypeProperty() { return reportType; }

    public String getDescription() { return description.get(); }
    public StringProperty descriptionProperty() { return description; }

    public String getOfficerName() { return officerName.get(); }
    public StringProperty officerNameProperty() { return officerName; }

    // Setters
    public void setReportId(int id) { this.reportId.set(id); }
    public void setVehicleId(int id) { this.vehicleId.set(id); }
    public void setVehicleReg(String reg) { this.vehicleReg.set(reg); }
    public void setReportDate(LocalDate date) { this.reportDate.set(date); }
    public void setReportType(String type) { this.reportType.set(type); }
    public void setDescription(String desc) { this.description.set(desc); }
    public void setOfficerName(String name) { this.officerName.set(name); }
}