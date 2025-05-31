package com.amlakie.usermanagment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemConditionDTO {
    @NotNull
    private Boolean problem = false; // Use Boolean wrapper class for @NotNull

    private Severity severity = Severity.NONE;

    private String notes;

  public enum Severity {
        NONE, LOW, MEDIUM, HIGH
    }
}