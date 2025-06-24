package com.amlakie.usermanagment.dto.maintainance; // Adjust package

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDetailsDTO {
    private String type;
    private String km;
    private String chassisNumber;
}
    