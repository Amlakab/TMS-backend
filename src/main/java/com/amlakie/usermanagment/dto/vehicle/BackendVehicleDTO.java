package com.amlakie.usermanagment.dto.vehicle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BackendVehicleDTO {
    private String plateNumber;
    // Assuming Vehicle interface has getDriverName()
    private Double kmPerLiter;   // Assuming Vehicle interface has getKmPerLiter()
    private String vehicleType;  // "CAR" or "ORGANIZATION_CAR"


}
        