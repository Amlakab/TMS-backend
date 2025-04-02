package com.amlakie.usermanagment.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CarInspectionReqRes {
    private int codStatus;
    private String message;
    private String error;

    private Long id;
    private String plateNumber;
    private LocalDateTime inspectionDate;

    // Mechanical attributes
    private boolean engineCondition;
    private boolean fullInsurance;
    private boolean enginePower;
    private boolean suspension;
    private boolean brakes;
    private boolean steering;
    private boolean gearbox;
    private boolean mileage;
    private boolean fuelGauge;
    private boolean tempGauge;
    private boolean oilGauge;

    // Body attributes
    private BodyConditionDTO bodyCondition;

    // Interior attributes
    private boolean engineExhaust;
    private boolean seatComfort;
    private boolean seatFabric;
    private boolean floorMat;
    private boolean rearViewMirror;
    private boolean carTab;
    private boolean mirrorAdjustment;
    private boolean doorLock;
    private boolean ventilationSystem;
    private boolean dashboardDecoration;
    private boolean seatBelt;
    private boolean sunshade;
    private boolean windowCurtain;
    private boolean interiorRoof;
    private boolean carIgnition;
    private boolean fuelConsumption;
    private boolean headlights;
    private boolean rainWiper;
    private boolean turnSignalLight;
    private boolean brakeLight;
    private boolean licensePlateLight;
    private boolean clock;
    private boolean rpm;
    private boolean batteryStatus;
    private boolean chargingIndicator;
}