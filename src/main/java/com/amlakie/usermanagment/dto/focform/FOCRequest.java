package com.amlakie.usermanagment.dto.focform;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class FOCRequest {
    private String plateNumber;
    private String receivedBy;
    private String assignedOfficial;
    private String driverName;
    private double entryKm;
    private double entryFuel;
    private double kmDrivenInWorkshop;
    private String purposeAndDestination;
    private List<OilUsedDto> oilUsed;
    private double fuelUsed;
    private LocalDate exitDate;
    private double exitKm;
    private String dispatchOfficer;
    private String mechanicName;
    private String headMechanicName;
}

