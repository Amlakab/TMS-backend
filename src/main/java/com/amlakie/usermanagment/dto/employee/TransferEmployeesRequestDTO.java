package com.amlakie.usermanagment.dto.employee;
import lombok.Data;

@Data
public class TransferEmployeesRequestDTO {
    private String fromCarPlateNumber;
    private String fromCarType; // e.g., "ORGANIZATION" or "RENT"
    private String toCarPlateNumber;
    private String toCarType; // e.g., "ORGANIZATION" or "RENT"
}