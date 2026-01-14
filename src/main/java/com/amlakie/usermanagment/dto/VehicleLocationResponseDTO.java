package com.amlakie.usermanagment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VehicleLocationResponseDTO {
    private Long id;
    private Long vehicleId;
    private String vehicleType;
    private String plateNumber;
    private String driverName;
    private String vehicleModel;
    private String vehicleStatus;
    private Double latitude;
    private Double longitude;
    private Double speed;
    private Double heading;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private String deviceImei;

    // Ensure no null values in getters
    public Double getSpeed() {
        return speed != null ? speed : 0.0;
    }

    public Double getHeading() {
        return heading != null ? heading : 0.0;
    }
}