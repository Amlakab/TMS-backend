package com.amlakie.usermanagment.dto;

public class DriverLocationDTO {
    private Long id;
    private String driverEmail;
    private String driverName;
    private String plateNumber;
    private String imei;

    // Default constructor
    public DriverLocationDTO() {}

    // Constructor with all fields
    public DriverLocationDTO(Long id, String driverEmail, String driverName, String plateNumber, String imei) {
        this.id = id;
        this.driverEmail = driverEmail;
        this.driverName = driverName;
        this.plateNumber = plateNumber;
        this.imei = imei;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDriverEmail() { return driverEmail; }
    public void setDriverEmail(String driverEmail) { this.driverEmail = driverEmail; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }

    public String getImei() { return imei; }
    public void setImei(String imei) { this.imei = imei; }
}