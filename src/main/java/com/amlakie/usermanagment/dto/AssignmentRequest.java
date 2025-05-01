package com.amlakie.usermanagment.dto;

import lombok.Data;

import java.time.LocalDateTime;

// src/main/java/com/amlakie/usermanagment/dto/AssignmentRequest.java
@Data
// AssignmentRequest.java
public class AssignmentRequest {
    private String requestLetterNo;
    private String requestDate; // As string from frontend
    private String requesterName;
    private String rentalType;
    private String position;
    private String department;
    private String phoneNumber;
    private String travelWorkPercentage;
    private String shortNoticePercentage;
    private String mobilityIssue;
    private String gender;
    private int totalPercentage;
    private String status;
    private Long carId; // Only ID instead of full object
    private Long rentCarId;

    // Getters and setters
}