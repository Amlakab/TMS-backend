package com.amlakie.usermanagment.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "rental_maintenance_requests")
@Data
public class RentalMaintenanceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "rental_car_id")
    private OrganizationCar rentalCar;

    @ManyToOne
    @JoinColumn(name = "car_id")
    private Car car;

    @Column(nullable = false)
    private String plateNumber;

    @Column(nullable = false)
    private String driverName;

    @Column(nullable = false)
    private String carType;

    @Column(nullable = false)
    private String requesterName;

    @Column(nullable = false)
    private String requesterPhone;

    @Column(nullable = false)
    private LocalDateTime requestDate;

    @Column(nullable = false)
    private LocalDateTime serviceDate;

    @Column
    private LocalDateTime returnDate;

    @Column(nullable = false)
    private String requestType;

    @Column
    private String dateDifference;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, APPROVED, COMPLETED

    @Column(columnDefinition = "TEXT")
    private String maintenanceNotes;

    @Column(nullable = false)
    private String createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}