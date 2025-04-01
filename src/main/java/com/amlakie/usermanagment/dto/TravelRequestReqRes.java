package com.amlakie.usermanagment.dto;

import com.amlakie.usermanagment.entity.TravelRequest;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TravelRequestReqRes {
    private int codStatus;
    private String message;
    private String error;
    private TravelRequest travelRequest;
    private List<TravelRequest> travelRequestList;

    // Request fields
    private String startingPlace;
    private String travelerName;
    private String carType;
    private LocalDate startingDate;
    private String department;
    private String destinationPlace;
    private String travelReason;
    private Double travelDistance;
    private LocalDate returnDate;
    private String jobStatus;
    private String claimantName;
    private String approvement;
    private String teamLeaderName;
}