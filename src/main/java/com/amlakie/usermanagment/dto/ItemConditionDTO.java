package com.amlakie.usermanagment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemConditionDTO {
    @NotNull // problem field should always be present
    private Boolean problem = false; // Use Boolean wrapper class for @NotNull

    // Severity is only required if problem is true.
    // Validation for this conditional requirement might need custom logic or Bean Validation groups if strictness is needed.
    // For simplicity, allow null/NONE if problem is false.
    private Severity severity = Severity.NONE;

    private String notes;


    // Match frontend severity strings (case-insensitive handling might be needed in service logic or via Jackson config)
    public enum Severity {
        NONE, LOW, MEDIUM, HIGH
    }
}