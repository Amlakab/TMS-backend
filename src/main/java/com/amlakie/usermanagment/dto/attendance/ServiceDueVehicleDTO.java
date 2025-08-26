package com.amlakie.usermanagment.dto.attendance;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ServiceDueVehicleDTO {
    private String plateNumber;
    private String vehicleType;
    private String driverName;
    private Double currentKm;
    private Double lastServiceKm;
    private Double kmSinceLastService;
}