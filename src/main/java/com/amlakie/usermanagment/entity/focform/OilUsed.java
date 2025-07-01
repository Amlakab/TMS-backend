package com.amlakie.usermanagment.entity.focform;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "oil_used")
@Data
public class OilUsed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private double amount;
}
