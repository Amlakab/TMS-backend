// D:/my git projects/TMS-backend/src/main/java/com/amlakie/usermanagment/dto/fogrequest/FuelOilGreaseRequestDTO.java

package com.amlakie.usermanagment.dto.fogrequest;

import com.amlakie.usermanagment.entity.fogrequest.NezekStatus;
import com.amlakie.usermanagment.entity.fogrequest.RequestStatus;
import jakarta.validation.constraints.NotBlank; // Import validation annotations
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FuelOilGreaseRequestDTO {
    private Long id;
    private String requestDate;

    @NotBlank(message = "Car type cannot be blank")
    private String carType;

    @NotBlank(message = "Plate number cannot be blank")
    private String plateNumber;

    @NotNull(message = "KM reading cannot be null")
    private Double kmReading;

    @NotBlank(message = "Short explanation cannot be blank")
    private String shortExplanation;

    // These fields directly map to frontend's ItemSectionData
    private RequestItemDTO fuel;
    private RequestItemDTO motorOil;
    private RequestItemDTO brakeFluid;
    private RequestItemDTO steeringFluid;
    private RequestItemDTO grease;

    private String mechanicName;
    private String headMechanicName;
    private Boolean headMechanicApproved;
    private String nezekOfficialName;
    private NezekStatus nezekOfficialStatus;
    private Boolean isFulfilled;
    private RequestStatus status;
}