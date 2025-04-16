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
    private String inspectorName;

    @Column(nullable = false)
    private String plateNumber;

    @Column(nullable = false)
    private LocalDateTime inspectionDate = LocalDateTime.now();

    // Add the following lines
    private String inspectionStatus;
    private String serviceStatus;
    private Integer bodyScore;
    private Integer interiorScore;
    private String notes;

    @OneToOne(cascade = CascadeType.ALL)
    private MechanicalInspection mechanical;

    @OneToOne(cascade = CascadeType.ALL)
    private BodyInspection body;

    @OneToOne(cascade = CascadeType.ALL)
    private InteriorInspection interior;

    @ManyToOne
    @JoinColumn(name = "plateNumber", referencedColumnName = "plateNumber", insertable = false, updatable = false)
    private Car car;
}