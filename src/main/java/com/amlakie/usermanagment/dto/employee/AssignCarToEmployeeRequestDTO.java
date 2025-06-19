package com.amlakie.usermanagment.dto.employee;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignCarToEmployeeRequestDTO {
    private String employeeId;
    private String carPlateNumber; // Or Long carId, depending on how you identify the car
    private String villageName;
}