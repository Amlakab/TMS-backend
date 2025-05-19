// VehicleAcceptanceResponse.java
package com.amlakie.usermanagment.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class VehicleAcceptanceResponse {
    private Long id;
    private String plateNumber;
    private String carType;
    private String km;
    private Map<String, Boolean> inspectionItems;
    private List<String> attachments;
    private List<String> carImages;
    private List<String> physicalContent;
    private List<String> notes;
    private List<SignatureResponse> signatures;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long assignmentHistoryId;
    private String status;
    private String message;
    private int statusCode;
}


