package com.amlakie.usermanagment.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "travel_requests")
@Data
public class TravelRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String startingPlace;

    @Column(nullable = false)
    private String travelerName;

    private String carType;

    @Column(nullable = false)
    private LocalDate startingDate;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private String destinationPlace;

    @Column(nullable = false)
    private String travelReason;

    private Double travelDistance;

    private LocalDate returnDate;

    @Column(nullable = false)
    private String jobStatus;

    private String claimantName;
    private String approvement;
    private String teamLeaderName;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private String createdBy;

    @PrePersist
    public void setCreatedBy() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        this.createdBy = authentication.getName();
    }
}