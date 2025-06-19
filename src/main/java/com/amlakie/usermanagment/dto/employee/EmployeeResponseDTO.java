package com.amlakie.usermanagment.dto.employee;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponseDTO {
    private String employeeId;
    private String name;
    private String email;
    private String department;
    private String village;
    private String assignedCarPlateNumber; // Displaying car's plate number
    private Long assignedCarId; // Displaying car's ID
}