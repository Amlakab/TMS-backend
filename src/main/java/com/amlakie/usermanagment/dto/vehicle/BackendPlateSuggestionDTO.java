package com.amlakie.usermanagment.dto.vehicle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BackendPlateSuggestionDTO {
    private String plateNumber;
    private String vehicleType; // "CAR" or "ORGANIZATION_CAR"
}
        