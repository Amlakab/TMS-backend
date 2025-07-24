package com.amlakie.usermanagment.dto;

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
    private LocalDateTime timestamp;
    private String deviceImei;
}