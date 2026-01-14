package com.amlakie.usermanagment.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_locations")
@Data
public class VehicleLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "vehicle_type", nullable = false)
    private String vehicleType; // "ORGANIZATION" or "RENT"

    @Column(name = "plate_number", nullable = false)
    private String plateNumber;

    @Column(name = "driver_name")
    private String driverName;

    @Column(name = "vehicle_model")
    private String vehicleModel;

    @Column(name = "vehicle_status")
    private String vehicleStatus;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Double speed = 0.0; // in km/h, default 0

    @Column(nullable = false)
    private Double heading = 0.0; // degrees from north, default 0

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "device_imei", nullable = false)
    private String deviceImei;

    // Add default values in constructor
    public VehicleLocation() {
        this.speed = 0.0;
        this.heading = 0.0;
        this.timestamp = LocalDateTime.now();
    }
}