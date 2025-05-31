package com.amlakie.usermanagment.dto.organization;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrganizationInteriorInspectionDTO {

    // Add messages for better error feedback
    @NotNull(message = "Engine exhaust details required") @Valid
    private OrganizationItemConditionDTO engineExhaust = new OrganizationItemConditionDTO();

    @NotNull(message = "Seat comfort details required") @Valid
    private OrganizationItemConditionDTO seatComfort = new OrganizationItemConditionDTO();

    @NotNull(message = "Seat fabric details required") @Valid
    private OrganizationItemConditionDTO seatFabric = new OrganizationItemConditionDTO();

    @NotNull(message = "Floor mat details required") @Valid
    private OrganizationItemConditionDTO floorMat = new OrganizationItemConditionDTO();

    @NotNull(message = "Rear view mirror details required") @Valid
    private OrganizationItemConditionDTO rearViewMirror = new OrganizationItemConditionDTO();

    @NotNull(message = "Car tab details required") @Valid
    private OrganizationItemConditionDTO carTab = new OrganizationItemConditionDTO();

    @NotNull(message = "Mirror adjustment details required") @Valid
    private OrganizationItemConditionDTO mirrorAdjustment = new OrganizationItemConditionDTO();

    @NotNull(message = "Door lock details required") @Valid
    private OrganizationItemConditionDTO doorLock = new OrganizationItemConditionDTO();

    @NotNull(message = "Ventilation system details required") @Valid
    private OrganizationItemConditionDTO ventilationSystem = new OrganizationItemConditionDTO();

    @NotNull(message = "Dashboard decoration details required") @Valid
    private OrganizationItemConditionDTO dashboardDecoration = new OrganizationItemConditionDTO();

    @NotNull(message = "Seat belt details required") @Valid
    private OrganizationItemConditionDTO seatBelt = new OrganizationItemConditionDTO();

    @NotNull(message = "Sunshade details required") @Valid
    private OrganizationItemConditionDTO sunshade = new OrganizationItemConditionDTO();

    @NotNull(message = "Window curtain details required") @Valid
    private OrganizationItemConditionDTO windowCurtain = new OrganizationItemConditionDTO();

    @NotNull(message = "Interior roof details required") @Valid
    private OrganizationItemConditionDTO interiorRoof = new OrganizationItemConditionDTO();

    @NotNull(message = "Car ignition details required") @Valid
    private OrganizationItemConditionDTO carIgnition = new OrganizationItemConditionDTO();

    @NotNull(message = "Fuel consumption details required") @Valid
    private OrganizationItemConditionDTO fuelConsumption = new OrganizationItemConditionDTO();

    @NotNull(message = "Headlights details required") @Valid
    private OrganizationItemConditionDTO headlights = new OrganizationItemConditionDTO();

    @NotNull(message = "Rain wiper details required") @Valid
    private OrganizationItemConditionDTO rainWiper = new OrganizationItemConditionDTO();

    @NotNull(message = "Turn signal light details required") @Valid
    private OrganizationItemConditionDTO turnSignalLight = new OrganizationItemConditionDTO();

    @NotNull(message = "Brake light details required") @Valid
    private OrganizationItemConditionDTO brakeLight = new OrganizationItemConditionDTO();

    @NotNull(message = "License plate light details required") @Valid
    private OrganizationItemConditionDTO licensePlateLight = new OrganizationItemConditionDTO();

    @NotNull(message = "Clock details required") @Valid
    private OrganizationItemConditionDTO clock = new OrganizationItemConditionDTO();

    @NotNull(message = "RPM details required") @Valid
    private OrganizationItemConditionDTO rpm = new OrganizationItemConditionDTO();

    @NotNull(message = "Battery status details required") @Valid
    private OrganizationItemConditionDTO batteryStatus = new OrganizationItemConditionDTO();

    @NotNull(message = "Charging indicator details required") @Valid
    private OrganizationItemConditionDTO chargingIndicator = new OrganizationItemConditionDTO();
}