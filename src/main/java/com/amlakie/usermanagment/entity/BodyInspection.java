package com.amlakie.usermanagment.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class BodyInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    private ItemCondition bodyCollision;

    @OneToOne(cascade = CascadeType.ALL)
    private ItemCondition bodyScratches;

    @OneToOne(cascade = CascadeType.ALL)
    private ItemCondition paintCondition;

    @OneToOne(cascade = CascadeType.ALL)
    private ItemCondition breakages;

    @OneToOne(cascade = CascadeType.ALL)
    private ItemCondition cracks;
}