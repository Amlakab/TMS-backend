package com.amlakie.usermanagment.dto;

import lombok.Data;

@Data
public class CompletionDTO {
    private Double startKm;
    private Double endKm;
    private Double kmDifference;
    private String reason;
    private String kmReason;
}