package com.amlakie.usermanagment.dto.maintainance;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepairDetailsDTO {
    // Dates will be received as strings and converted in the service
    private String dateOfReceipt;
    private String dateStarted;
    private String dateFinished;
    private String duration;
    private String inspectorName;
    private String teamLeader;
    private String worksDoneLevel; // "low", "medium", "high", or ""
    private String worksDoneDescription;
}

