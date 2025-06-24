// MaintenanceRequest.java - Updated
package com.amlakie.usermanagment.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "maintenance_requests")
@Data
public class MaintenanceRequest {

    public enum RequestStatus {
        PENDING, CHECKED, REJECTED, INSPECTION, COMPLETED, APPROVED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String plateNumber;

    @Column(nullable = false)
    private String vehicleType;

    @Column(nullable = false)
    private String reportingDriver;

    @Column(nullable = false)
    private String categoryWorkProcess;

    @Column(nullable = false)
    private Double kilometerReading;

    @Column(nullable = false, length = 500)
    private String defectDetails;

    @Column(length = 500)
    private String mechanicDiagnosis;

    @Column
    private String requestingPersonnel;

    @Column
    private String authorizingPersonnel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private String createdBy;

    @Column
    private LocalDateTime updatedAt;

    @Column
    private String updatedBy;

    @ElementCollection
    @CollectionTable(name = "maintenance_request_attachments", joinColumns = @JoinColumn(name = "request_id"))
    @Column(name = "attachment")
    private List<String> attachments;

    @ElementCollection
    @CollectionTable(name = "maintenance_request_images", joinColumns = @JoinColumn(name = "request_id"))
    @Column(name = "image_url")
    private List<String> carImages;

    @ElementCollection
    @CollectionTable(name = "maintenance_request_physical_content", joinColumns = @JoinColumn(name = "request_id"))
    @Column(name = "content")
    private List<String> physicalContent;

    @ElementCollection
    @CollectionTable(name = "maintenance_request_notes", joinColumns = @JoinColumn(name = "request_id"))
    @Column(name = "note")
    private List<String> notes;

    @ElementCollection
    @CollectionTable(name = "maintenance_request_signatures", joinColumns = @JoinColumn(name = "request_id"))
    private List<Signature> signatures;

    // Embedded signature class
    @Embeddable
    @Data
    public static class Signature {
        private String role;
        private String name;
        private String signature;
        private String date;
    }
}