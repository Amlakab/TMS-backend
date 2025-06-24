package com.amlakie.usermanagment.entity.maintainance;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Column;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepairInfo {
    private LocalDate dateOfReceipt;
    private LocalDate dateStarted;
    private LocalDate dateFinished;
    private String duration;
    private String inspectorName;
    private String teamLeader;

    @Enumerated(EnumType.STRING)
    private WorksDoneLevel worksDoneLevel;

    @Column(columnDefinition = "TEXT")
    private String worksDoneDescription;
}