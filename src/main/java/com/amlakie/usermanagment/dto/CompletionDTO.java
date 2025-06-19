// CompletionDTO.java (for PATCH /complete)
package com.amlakie.usermanagment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CompletionDTO {
    @NotNull(message = "Start KM is required")
    private Double startKm;
    private String reason;
    @NotNull(message = "End KM is required")
    private Double endKm;
}