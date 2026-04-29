package com.example.vehicleidentificationsystem.models;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Insurance {
    private final IntegerProperty insuranceId;
    private final IntegerProperty vehicleId;
    private final StringProperty vehicleReg;
    private final StringProperty policyNumber;
    private final StringProperty provider;
    private final StringProperty insuranceType;
    private final ObjectProperty<LocalDate> startDate;
    private final ObjectProperty<LocalDate> expiryDate;
    private final StringProperty status;
    private final DoubleProperty premiumAmount;
    private final IntegerProperty companyId;
    private final StringProperty companyName;

    public Insurance() {
        this.insuranceId = new SimpleIntegerProperty(0);
        this.vehicleId = new SimpleIntegerProperty(0);
        this.vehicleReg = new SimpleStringProperty("");
        this.policyNumber = new SimpleStringProperty("");
        this.provider = new SimpleStringProperty("");
        this.insuranceType = new SimpleStringProperty("Comprehensive");
        this.startDate = new SimpleObjectProperty<>(LocalDate.now());
        this.expiryDate = new SimpleObjectProperty<>(LocalDate.now().plusYears(1));
        this.status = new SimpleStringProperty("Active");
        this.premiumAmount = new SimpleDoubleProperty(0);
        this.companyId = new SimpleIntegerProperty(0);
        this.companyName = new SimpleStringProperty("");
    }

    public Insurance(int insuranceId, int vehicleId, String vehicleReg, String policyNumber,
                     String provider, String insuranceType, LocalDate startDate, LocalDate expiryDate,
                     String status, double premiumAmount, int companyId, String companyName) {
        this.insuranceId = new SimpleIntegerProperty(insuranceId);
        this.vehicleId = new SimpleIntegerProperty(vehicleId);
        this.vehicleReg = new SimpleStringProperty(vehicleReg);
        this.policyNumber = new SimpleStringProperty(policyNumber);
        this.provider = new SimpleStringProperty(provider);
        this.insuranceType = new SimpleStringProperty(insuranceType);
        this.startDate = new SimpleObjectProperty<>(startDate);
        this.expiryDate = new SimpleObjectProperty<>(expiryDate);
        this.status = new SimpleStringProperty(status);
        this.premiumAmount = new SimpleDoubleProperty(premiumAmount);
        this.companyId = new SimpleIntegerProperty(companyId);
        this.companyName = new SimpleStringProperty(companyName);
    }

    // Getters and Property getters
    public int getInsuranceId() { return insuranceId.get(); }
    public IntegerProperty insuranceIdProperty() { return insuranceId; }

    public int getVehicleId() { return vehicleId.get(); }
    public IntegerProperty vehicleIdProperty() { return vehicleId; }

    public String getVehicleReg() { return vehicleReg.get(); }
    public StringProperty vehicleRegProperty() { return vehicleReg; }

    public String getPolicyNumber() { return policyNumber.get(); }
    public StringProperty policyNumberProperty() { return policyNumber; }

    public String getProvider() { return provider.get(); }
    public StringProperty providerProperty() { return provider; }

    public String getInsuranceType() { return insuranceType.get(); }
    public StringProperty insuranceTypeProperty() { return insuranceType; }

    public LocalDate getStartDate() { return startDate.get(); }
    public ObjectProperty<LocalDate> startDateProperty() { return startDate; }

    public LocalDate getExpiryDate() { return expiryDate.get(); }
    public ObjectProperty<LocalDate> expiryDateProperty() { return expiryDate; }

    public String getStatus() { return status.get(); }
    public StringProperty statusProperty() { return status; }

    public double getPremiumAmount() { return premiumAmount.get(); }
    public DoubleProperty premiumAmountProperty() { return premiumAmount; }

    public int getCompanyId() { return companyId.get(); }
    public IntegerProperty companyIdProperty() { return companyId; }

    public String getCompanyName() { return companyName.get(); }
    public StringProperty companyNameProperty() { return companyName; }

    // Setters
    public void setInsuranceId(int id) { this.insuranceId.set(id); }
    public void setVehicleId(int id) { this.vehicleId.set(id); }
    public void setVehicleReg(String reg) { this.vehicleReg.set(reg); }
    public void setPolicyNumber(String num) { this.policyNumber.set(num); }
    public void setProvider(String provider) { this.provider.set(provider); }
    public void setInsuranceType(String type) { this.insuranceType.set(type); }
    public void setStartDate(LocalDate date) { this.startDate.set(date); }
    public void setExpiryDate(LocalDate date) { this.expiryDate.set(date); }
    public void setStatus(String status) { this.status.set(status); }
    public void setPremiumAmount(double amount) { this.premiumAmount.set(amount); }
    public void setCompanyId(int id) { this.companyId.set(id); }
    public void setCompanyName(String name) { this.companyName.set(name); }
}