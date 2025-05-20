package com.amlakie.usermanagment.dto.attendance;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;


@Data
public class FuelEntryRequestDTO {


    @NotBlank(message = "Plate number is required")
    private String plateNumber;


    @NotBlank(message = "Vehicle type is required")
    private String vehicleType; // e.g., "ORGANIZATION_CAR", "CAR"


    @NotNull(message = "Liters added is required")
    @PositiveOrZero(message = "Liters added must be a non-negative value")
    private Double litersAdded;


    @NotNull(message = "KM at fueling is required")
    @PositiveOrZero(message = "KM at fueling must be a non-negative value")
    private Double kmAtFueling;


    @NotBlank(message = "Fueling date is required")
    private String fuelingDate; // Expected format: "YYYY-MM-DD"
}