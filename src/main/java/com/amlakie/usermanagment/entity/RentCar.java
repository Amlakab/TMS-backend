package com.amlakie.usermanagment.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "rent_cars")
@Data
public class RentCar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String frameNo;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String vehiclesUsed;

    @Column(nullable = false)
    private String bodyType;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private String motorNumber;

    @Column(nullable = false)
    private String proYear;

    @Column(nullable = false)
    private String cc;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private String vehiclesType;

    @Column(unique = true, nullable = false)
    private String plateNumber;

    @Column(nullable = false)
    private String color;

    @Column(nullable = false)
    private String door;

    @Column(nullable = false)
    private String cylinder;

    @Column(nullable = false)
    private String fuelType;

    @Column(nullable = false)
    private String vehiclesStatus = "Active";

    @Column(nullable = false)
    private String otherDescription;

    @Column(nullable = false)
    private String radio;

    @Column(nullable = false)
    private String antena;

    @Column(nullable = false)
    private String krik;

    @Column(nullable = false)
    private String krikManesha;

    @Column(nullable = false)
    private String tyerStatus;

    @Column(nullable = false)
    private String gomaMaficha;

    @Column(nullable = false)
    private String mefcha;

    @Column(nullable = false)
    private String reserveTayer;

    @Column(nullable = false)
    private String gomaGet;

    @Column(nullable = false)
    private String pinsa;

    @Column(nullable = false)
    private String kacavite;

    @Column(nullable = false)
    private String fireProtection;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private String vehiclesDonorName;

    @Column(nullable = false)
    private LocalDate dateOfIn;

    @Column(nullable = false)
    private LocalDate dateOfOut;

    @Column(nullable = false)
    private String vehiclesPhoto;

    @Column(nullable = false)
    private String vehiclesUserName;

    @Column(nullable = false)
    private String position;

    @Column(nullable = false)
    private String libre;

    @Column(nullable = false)
    private String transmission;

    @Column(nullable = false)
    private String dataAntollerNatue;

    @Column(nullable = false)
    private String km;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private String createdBy;
}