package com.amlakie.usermanagment.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

// src/main/java/com/amlakie/usermanagment/entity/AssignmentHistory.java
@Entity
@Table(name = "assignment_history")
@Data
public class AssignmentHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String requestLetterNo;

    @Column(nullable = false)
    private LocalDateTime requestDate;

    @Column(nullable = false)
    private String requesterName;

    @Column(nullable = false)
    private String rentalType;

    @Column(nullable = false)
    private String position;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String travelWorkPercentage;

    @Column(nullable = false)
    private String shortNoticePercentage;

    @Column(nullable = false)
    private String mobilityIssue;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private int totalPercentage;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String plateNumber;

    @ManyToOne
    @JoinColumn(name = "car_id", nullable = true)
    private Car car;

    @ManyToOne
    @JoinColumn(name = "rent_car_id", nullable = true)
    private RentCar cars;

    @Column(nullable = false)
    private LocalDateTime assignedDate = LocalDateTime.now();
}
