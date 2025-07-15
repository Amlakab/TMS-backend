package com.amlakie.usermanagment.entity; // Or your preferred package for entities

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @Column(name = "employee_id", unique = true, nullable = false, length = 50)
    private String employeeId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "department")
    private String department;

    @Email // Optional: Add validation
    @Column(name = "email", unique = true) // Assuming email should be unique
    private String email; // New field
    @Column(name = "village") // This will be updated
    private String village;

    // Existing relationship for OrganizationCar
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_org_car_id")
    private OrganizationCar assignedCar;

    // --- ADD THESE NEW FIELDS ---
    // This creates the relationship to the RentCar entity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_rent_car_id")
    private RentCar assignedRentCar;

    // This field will store "ORGANIZATION" or "RENT"
    @Column(name = "assigned_car_type")
    private String assignedCarType;
    // Optional: Store plate number directly if frequently needed without joining
    // @Column(name = "assigned_car_plate_number")
    // private String assignedCarPlateNumber;
}