package com.amlakie.usermanagment.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "driver_location")
public class DriverLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String driverEmail;

    private String driverName;

    @Column(unique = true)
    private String plateNumber;

    @Column(unique = true)
    private String imei;

    // constructors
    public DriverLocation() {}

    public DriverLocation(String driverEmail, String driverName, String plateNumber, String imei) {
        this.driverEmail = driverEmail;
        this.driverName = driverName;
        this.plateNumber = plateNumber;
        this.imei = imei;
    }

    // getters and setters
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