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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_car_id") // Foreign key to OrganizationCar
    private OrganizationCar assignedCar;

    // Optional: Store plate number directly if frequently needed without joining
    // @Column(name = "assigned_car_plate_number")
    // private String assignedCarPlateNumber;
}