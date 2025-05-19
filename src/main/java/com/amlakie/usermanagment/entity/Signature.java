// Signature.java
package com.amlakie.usermanagment.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "signatures")
public class Signature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String role;
    private String name;
    private String signature;
    private String date;

    @ManyToOne
    @JoinColumn(name = "vehicle_acceptance_id")
    private VehicleAcceptance vehicleAcceptance;
}