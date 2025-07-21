package com.amlakie.usermanagment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class DailyServiceRequestDTO {
    @NotNull(message = "Request date is required")
    private LocalDate requestDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    private LocalTime returnTime;

    @NotEmpty(message = "At least one traveler is required")
    private List<@NotBlank String> travelers;

    @NotBlank(message = "Starting place is required")
    private String startingPlace;

    @NotBlank(message = "Ending place is required")
    private String endingPlace;

    @NotBlank(message = "Claimant name is required")
    private String claimantName;
}