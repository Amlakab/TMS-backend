package com.amlakie.usermanagment.dto.attendance;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CarAttendanceResponseDTO {
    private Long id;
    private String vehiclePlateNumber; // From vehicle.getPlateNumber()
    private String vehicleType; // The discriminator string ("CAR" or "ORGANIZATION_CAR")

    private Double morningKm;
    private Double eveningKm;
    private Double dailyKmDifference;
    private Double overnightKmDifferenceFromPrevious;
    private Double fuelLitersAdded;
    private Double kmPerLiterCalculated; // If you implement this calculation
    private LocalDate date;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String driverName;
    private Double kmPerLiter;
}