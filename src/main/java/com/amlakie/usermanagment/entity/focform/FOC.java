package com.amlakie.usermanagment.entity.focform;

import com.amlakie.usermanagment.entity.focform.OilUsed;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "foc_forms")
@Data
public class FOC {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String plateNumber;

    @Column(nullable = false)
    private String receivedBy;

    @Column(nullable = false)
    private String assignedOfficial;

    @Column(nullable = false)
    private String driverName;

    @Column(nullable = false)
    private double entryKm;

    @Column(nullable = false)
    private double entryFuel;

    @Column(nullable = false)
    private double kmDrivenInWorkshop;

    @Column(nullable = false, length = 1000)
    private String purposeAndDestination;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "foc_id")
    private List<OilUsed> oilUsed;

    @Column(nullable = false)
    private double fuelUsed;

    @Column(nullable = false)
    private LocalDate exitDate;

    @Column(nullable = false)
    private double exitKm;

    @Column(nullable = false)
    private String dispatchOfficer;

    @Column(nullable = false)
    private String mechanicName;

    @Column(nullable = false)
    private String headMechanicName;

    @Column(nullable = false)
    private LocalDate createdAt = LocalDate.now();
}

