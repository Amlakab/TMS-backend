package com.amlakie.usermanagment.entity.attendance;

import com.amlakie.usermanagment.entity.Car;
import com.amlakie.usermanagment.entity.OrganizationCar;
import com.amlakie.usermanagment.entity.Vehicle;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;
import org.hibernate.type.descriptor.java.LongJavaType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor // Good for JPA
@ToString(exclude = {"vehicle"}) // Exclude related entities from default toString to avoid issues
@EqualsAndHashCode(of = "id") // Base equals/hashCode on a stable unique ID
@Entity
@Table(name = "car_attendance")
public class CarAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Any
    @AnyDiscriminator(DiscriminatorType.STRING)
    @AnyKeyJavaType(LongJavaType.class)
    @AnyDiscriminatorValues({
            @AnyDiscriminatorValue(discriminator = "CAR", entity = Car.class),
            @AnyDiscriminatorValue(discriminator = "ORGANIZATION_CAR", entity = OrganizationCar.class) // Changed discriminator for clarity
    })
    @Column(name = "vehicle_type") // Stores "CAR" or "ORGANIZATION_CAR"
    @JoinColumn(name = "vehicle_id") // Stores the ID of the Car or OrganizationCar
    private Vehicle vehicle;
    @Column(name = "vehicle_type", insertable = false, updatable = false)
    private String vehicleTypeDiscriminator;

    // --- KM Readings for a Single Attendance Period (e.g., one day) ---
    @Column(name = "morning_km") // Explicit column naming
    private Double morningKm;

    @Column(name = "evening_km") // Renamed from nightKm for clarity if it represents evening departure
    private Double eveningKm;
    @Column(name = "attendance_date", nullable = false)// Defaults to nullable = true
    private LocalDate date;

    @Column(name = "daily_km_difference") // Renamed from dayKmDiff
    private Double dailyKmDifference;

    @Column(name = "km_at_fueling") // Renamed from dayKmDiff
    private Double kmAtFueling;
    @Column(name = "overnight_km_difference_from_previous") // More descriptive name
    private Double overnightKmDifferenceFromPrevious; // This would be (current morningKm - previous eveningKm)

    @Column(name = "fuel_liters_added")
    private Double fuelLitersAdded; // Renamed for clarity

    @Column(name = "km_per_liter_calculated")
    private Double kmPerLiterCalculated; // Renamed

     // The date of this attendance record

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    @PrePersist
    @PreUpdate
    public void calculateInternalDifferences() {
        if (this.morningKm != null && this.eveningKm != null) {
            if (this.eveningKm >= this.morningKm) {
                this.dailyKmDifference = this.eveningKm - this.morningKm;
            } else {
                this.dailyKmDifference = null;
                System.err.println("Warning: Evening KM ("+ this.eveningKm +") is less than Morning KM ("+ this.morningKm +") for attendance ID: " + this.id);
            }
        } else {
            this.dailyKmDifference = null;
        }
    }
}