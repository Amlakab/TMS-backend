package com.amlakie.usermanagment.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "car_inspections")
@Data
public class CarInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String plateNumber;

    @Column(nullable = false)
    private LocalDateTime inspectionDate = LocalDateTime.now();

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
    @Embedded
    private BodyCondition bodyCondition = new BodyCondition();
    @Embedded
    private BodyProblem bodyProblem = new BodyProblem();

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

    @ManyToOne
    @JoinColumn(name = "plateNumber", referencedColumnName = "plateNumber", insertable = false, updatable = false)
    private Car car;
}