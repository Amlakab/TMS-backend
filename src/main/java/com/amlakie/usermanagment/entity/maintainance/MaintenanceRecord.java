package com.amlakie.usermanagment.entity.maintainance;

import com.amlakie.usermanagment.entity.maintainance.RepairInfo; // Adjust package
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "maintenance_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String plateNumber;

    // Vehicle details from the fetched information
    private String vehicleType;
    private String vehicleKm;
    private String vehicleChassisNumber;

    @Column(columnDefinition = "TEXT")
    private String driverDescription;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "dateOfReceipt", column = @Column(name = "mechanical_date_receipt")),
            @AttributeOverride(name = "dateStarted", column = @Column(name = "mechanical_date_started")),
            @AttributeOverride(name = "dateFinished", column = @Column(name = "mechanical_date_finished")),
            @AttributeOverride(name = "duration", column = @Column(name = "mechanical_duration")),
            @AttributeOverride(name = "inspectorName", column = @Column(name = "mechanical_inspector_name")),
            @AttributeOverride(name = "teamLeader", column = @Column(name = "mechanical_team_leader")),
            @AttributeOverride(name = "worksDoneLevel", column = @Column(name = "mechanical_works_level")),
            @AttributeOverride(name = "worksDoneDescription", column = @Column(name = "mechanical_works_description"))
    })
    private RepairInfo mechanicalRepair;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "dateOfReceipt", column = @Column(name = "electrical_date_receipt")),
            @AttributeOverride(name = "dateStarted", column = @Column(name = "electrical_date_started")),
            @AttributeOverride(name = "dateFinished", column = @Column(name = "electrical_date_finished")),
            @AttributeOverride(name = "duration", column = @Column(name = "electrical_duration")),
            @AttributeOverride(name = "inspectorName", column = @Column(name = "electrical_inspector_name")),
            @AttributeOverride(name = "teamLeader", column = @Column(name = "electrical_team_leader")),
            @AttributeOverride(name = "worksDoneLevel", column = @Column(name = "electrical_works_level")),
            @AttributeOverride(name = "worksDoneDescription", column = @Column(name = "electrical_works_description"))
    })
    private RepairInfo electricalRepair;
}
