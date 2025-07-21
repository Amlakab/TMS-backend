package com.amlakie.usermanagment.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "daily_service_requests")
@Data
public class DailyServiceRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate requestDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column
    private LocalTime returnTime;

    @ElementCollection
    @CollectionTable(name = "daily_service_travelers", joinColumns = @JoinColumn(name = "request_id"))
    private List<String> travelers = new ArrayList<>();

    @Column(nullable = false)
    private String startingPlace;

    @Column(nullable = false)
    private String endingPlace;

    @Column(nullable = false)
    private String claimantName;

    private String driverName;
    private Double estimatedKilometers;
    private Double startKm;
    private Double endKm;
    private Double kmDifference;
    private String carType;
    private String plateNumber;
    private String reason;
    private String kmReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime completedAt;

    public enum RequestStatus {
        PENDING, ASSIGNED, COMPLETED
    }
}