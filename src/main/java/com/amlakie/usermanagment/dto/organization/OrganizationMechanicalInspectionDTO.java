package com.amlakie.usermanagment.dto.organization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor // Can be helpful if you want to create instances with all fields
public class OrganizationMechanicalInspectionDTO {
    private boolean engineCondition;
    private boolean enginePower;
    private boolean suspension;
    private boolean brakes;
    private boolean steering;
    private boolean gearbox;
    private boolean mileage;
    private boolean fuelGauge;
    private boolean tempGauge;
    private boolean oilGauge;
}