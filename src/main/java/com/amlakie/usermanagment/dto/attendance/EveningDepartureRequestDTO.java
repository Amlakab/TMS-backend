package com.amlakie.usermanagment.dto.attendance;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class EveningDepartureRequestDTO {

    @NotNull(message = "Evening KM reading is required")
    @PositiveOrZero(message = "Evening KM must be a non-negative number")
    private Double eveningKm;

    // Optional: if fuel was added this evening (less common, but possible)
    @PositiveOrZero(message = "Fuel liters added must be a non-negative number")
    private Double fuelLitersAdded;
}