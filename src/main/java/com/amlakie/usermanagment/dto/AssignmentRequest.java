package com.amlakie.usermanagment.dto;

import lombok.Data;

import java.time.LocalDateTime;

// src/main/java/com/amlakie/usermanagment/dto/AssignmentRequest.java
@Data
public class AssignmentRequest {
    private String requestLetterNo;
    private LocalDateTime requestDate;
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
    private Long carId;
}
