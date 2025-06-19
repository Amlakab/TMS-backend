// MaintenanceRequestDTO.java - Updated
package com.amlakie.usermanagment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MaintenanceRequestDTO {

    @NotBlank(message = "Plate number is required")
    private String plateNumber;

    @NotBlank(message = "Vehicle type is required")
    private String vehicleType;

    @NotBlank(message = "Reporting driver is required")
    private String reportingDriver;

    @NotBlank(message = "Category/work process is required")
    private String categoryWorkProcess;

    @NotNull(message = "Kilometer reading is required")
    private Double kilometerReading;

    @NotBlank(message = "Defect details are required")
    private String defectDetails;

    private String mechanicDiagnosis;
    private String requestingPersonnel;
    private String authorizingPersonnel;
    private String status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    private String createdBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    private String updatedBy;

    // New fields for acceptance form
    private List<String> attachments;
    private List<String> carImages;
    private List<String> physicalContent;
    private List<String> notes;

    private List<SignatureDTO> signatures;

    @Data
    public static class SignatureDTO {
        private String role;
        private String name;
        private String signature;
        private String date;
    }
}