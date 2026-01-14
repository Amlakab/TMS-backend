package com.amlakie.usermanagment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GpsDataDTO {
    @NotBlank(message = "IMEI is required")
    private String imei;

    @NotBlank(message = "Plate number is required")
    private String plateNumber;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    @NotNull(message = "Speed is required")
    private Double speed = 0.0; // Default to 0

    @NotNull(message = "Heading is required")
    private Double heading = 0.0; // Default to 0

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    // Ensure no null values
    public Double getSpeed() {
        return speed != null ? speed : 0.0;
    }

    public Double getHeading() {
        return heading != null ? heading : 0.0;
    }

    public LocalDateTime getTimestamp() {
        return timestamp != null ? timestamp : LocalDateTime.now();
    }
}