package com.amlakie.usermanagment.entity.fogrequest;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "fuel_oil_grease_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuelOilGreaseRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate requestDate;

    @Column(nullable = false)
    private String carType;

    @Column(nullable = false)
    private String plateNumber;

    @Column(nullable = false)
    private Double kmReading;

    @Column(nullable = false)
    private String shortExplanation;

    // This list will hold all the individual items (fuel, motor oil, etc.)
    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RequestItem> items;

    @Column(nullable = false)
    private String mechanicName;

    private String headMechanicName;

    @Enumerated(EnumType.STRING)
    private ApprovalStatus headMechanicApproval; // Enum for approval status

    private String nezekOfficialName;

    @Enumerated(EnumType.STRING)
    private NezekStatus nezekStatus; // Enum for Nezek status

    @Column(nullable = false)
    private Boolean isFulfilled;

    @Enumerated(EnumType.STRING)
    private RequestStatus status; // Enum for request lifecycle status
}