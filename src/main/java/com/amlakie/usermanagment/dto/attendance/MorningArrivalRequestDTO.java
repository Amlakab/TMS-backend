package com.amlakie.usermanagment.dto.attendance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class MorningArrivalRequestDTO {

    @NotBlank(message = "Plate number is required")
    private String plateNumber;

    @NotBlank(message = "Vehicle type is required (e.g., CAR or ORGANIZATION_CAR)")
    private String vehicleType; // "CAR" or "ORGANIZATION_CAR" to help find the vehicle

    @NotNull(message = "Morning KM reading is required")
    @PositiveOrZero(message = "Morning KM must be a non-negative number")
    private Double morningKm;

    // Optional: if fuel was added this morning
    @PositiveOrZero(message = "Fuel liters added must be a non-negative number")
    private Double fuelLitersAdded;

}