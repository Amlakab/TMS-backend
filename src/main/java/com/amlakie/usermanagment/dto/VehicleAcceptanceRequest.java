// VehicleAcceptanceRequest.java
package com.amlakie.usermanagment.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class VehicleAcceptanceRequest {
    private Long id;
    private String plateNumber;
    private String carType;
    private String km;
    private Map<String, Boolean> inspectionItems;
    private List<String> attachments;
    private List<String> carImages;
    private List<String> physicalContent;
    private List<String> notes;
    private List<SignatureRequest> signatures;
    private Long assignmentHistoryId;
    private List<String> existingImageUrls;

}

// SignatureRequest.java
