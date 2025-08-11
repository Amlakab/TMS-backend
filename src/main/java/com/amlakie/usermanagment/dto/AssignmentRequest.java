package com.amlakie.usermanagment.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class AssignmentRequest {
    private String requestLetterNo;
    private String requestDate;
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
    private String model;
    private Long carId;
    private Long rentCarId;
    private String assignedDate;
    private String numberOfCar;
    private String licenseExpiryDate;
    private MultipartFile driverLicenseFile;
    private String driverLicenseNumber;
    private List<Long> carIds;
    private List<Long> rentCarIds;
}