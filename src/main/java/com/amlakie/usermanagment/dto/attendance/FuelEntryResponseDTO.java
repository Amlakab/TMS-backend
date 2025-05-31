package com.amlakie.usermanagment.dto.attendance;


import lombok.Data;


@Data
public class FuelEntryResponseDTO {
    private Long id;
    private String vehiclePlateNumber;
    private String vehicleType;
    private Double litersAdded; // Total liters added for the record (cumulative)
    private Double kmAtFueling; // KM provided in this request
    private String fuelingDate; // Date of this fueling
    private String message; // Optional success message
    private Double eveningKm; // Added this to return updated value
    // You can add more fields as needed (e.g., dailyKmDifference, etc.)
}
 