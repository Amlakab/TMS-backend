// VehicleAcceptance.java
package com.amlakie.usermanagment.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "vehicle_acceptance")
@Data
public class VehicleAcceptance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String plateNumber;

    @Column(nullable = false)
    private String carType;

    @Column(nullable = false)
    private String km;

    @ElementCollection
    @CollectionTable(name = "vehicle_inspection_items", joinColumns = @JoinColumn(name = "vehicle_acceptance_id"))
    @MapKeyColumn(name = "part_name")
    @Column(name = "is_ok")
    private Map<String, Boolean> inspectionItems = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "vehicle_attachments", joinColumns = @JoinColumn(name = "vehicle_acceptance_id"))
    @Column(name = "attachment")
    private List<String> attachments;

    @ElementCollection
    @CollectionTable(name = "vehicle_images", joinColumns = @JoinColumn(name = "vehicle_acceptance_id"))
    @Column(name = "image_path")
    private List<String> carImages;

    @ElementCollection
    @CollectionTable(name = "vehicle_physical_content", joinColumns = @JoinColumn(name = "vehicle_acceptance_id"))
    @Column(name = "content")
    private List<String> physicalContent;

    @ElementCollection
    @CollectionTable(name = "vehicle_notes", joinColumns = @JoinColumn(name = "vehicle_acceptance_id"))
    @Column(name = "note")
    private List<String> notes;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "vehicle_acceptance_id")
    private List<Signature> signatures;

    @ManyToOne
    @JoinColumn(name = "assignment_history_id")
    private AssignmentHistory assignmentHistory;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

