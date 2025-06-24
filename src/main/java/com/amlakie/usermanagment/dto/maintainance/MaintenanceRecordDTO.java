package com.amlakie.usermanagment.dto.maintainance;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceRecordDTO {
    private Long id; // For returning the created record's ID
    private String plateNumber;
    private VehicleDetailsDTO vehicleDetails;
    private String driverDescription;
    private RepairDetailsDTO mechanicalRepair;
    private RepairDetailsDTO electricalRepair;
}
