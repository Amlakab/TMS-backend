package com.amlakie.usermanagment.entity;
import com.amlakie.usermanagment.entity.organization.OrganizationCarInspection;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "organization_cars")
@Data
public class OrganizationCar implements Vehicle{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



    @Column(unique = true, nullable = false)
    private String plateNumber;

    @Column(nullable = false)
    private String ownerName;
    @Column(name = "latest_inspection_id") // Optional: Define column name explicitly
    private Long latestInspectionId;

    @Column(nullable = false)
    private String ownerPhone;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private String carType;

    @Column(nullable = false)
    private String manufactureYear;

    @Column(nullable = false)
    private String motorCapacity;

    @Column(nullable = false)
    private float kmPerLiter;

    @Column(nullable = false)
    private String totalKm;

    @Column(nullable = false)
    private String fuelType;

    @Column(nullable = false)
    private String status = "Pending";

    @Column(nullable =  false)
    private Boolean inspected = false;

    @Column(nullable = false)
    private LocalDateTime registeredDate = LocalDateTime.now();
    @Column
    private Double destinationLat;
    @Column
    private Double destinationLng;

    @Column
    private String parkingLocation;

    @Column(nullable = false)
    private String driverName;

    @Column(nullable = false)
    private String driverAttributes;

    @Column(nullable = false)
    private String driverAddress;

    @Column(nullable = false)
    private Integer loadCapacity;

    @Column(nullable = false)
    private String createdBy;

    // In your OrganizationCar.java entity
    // ... other fields ...

    @OneToMany(mappedBy = "organizationCar") // Assuming 'organizationCar' is the field in OrganizationCarInspection
    @JsonManagedReference // This side will be serialized
    private List<OrganizationCarInspection> inspections;

    public Boolean isInspected() {
        return inspected != null ? inspected : false;
    }

    @Override
    public Double getCurrentKm() {
        try {
            return Double.parseDouble(this.totalKm);
        } catch (NumberFormatException e) {
            // Handle cases where totalKm might not be a valid double
            // Or ensure totalKm is stored as Double in the first place
            return null;
        }
    }

    @Override
    public void setCurrentKm(Double currentKm) {
        if (currentKm != null) {
            this.totalKm = String.valueOf(currentKm);
        } else {
            this.totalKm = null; // Or "0.0" or handle as appropriate
        }
    }
    @Override
    public Long getId(){
        return this.id;
    }
    @Override
    public String getPlateNumber() {
        return this.plateNumber;
    }
    @Override
    public float getKmPerLiter() {
        return  this.kmPerLiter;
    }

}