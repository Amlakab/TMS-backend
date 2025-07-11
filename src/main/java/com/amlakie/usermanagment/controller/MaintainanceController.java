package com.amlakie.usermanagment.controller;
import com.amlakie.usermanagment.dto.maintainance.MaintenanceRecordDTO; // Adjust package
import com.amlakie.usermanagment.dto.maintainance.VehicleDetailsDTO;    // Adjust package
import com.amlakie.usermanagment.entity.MaintenanceRequest;
import com.amlakie.usermanagment.service.MaintenanceRequestService;
import com.amlakie.usermanagment.service.MaintenanceService; // Adjust package
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/maintenance") // Adjust base path as needed
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MaintainanceController {

    private static final Logger log = LoggerFactory.getLogger(MaintainanceController.class);
    private final MaintenanceService maintenanceService;
    private final MaintenanceRequestService maintenanceRequestService;
    @GetMapping("/approved")
    public ResponseEntity<List<MaintenanceRequest>> getApprovedMaintenanceRequests() {
        log.info("REST request to get all APPROVED maintenance requests");
        List<MaintenanceRequest> approvedRequests = maintenanceRequestService.getInspectionRequest();
        return ResponseEntity.ok(approvedRequests);
    }
    // Endpoint to match frontend's handleSubmit
    @PostMapping("/records")
    public ResponseEntity<?> createMaintenanceRecord(@RequestBody MaintenanceRecordDTO maintenanceRecordDTO) {
        log.info("Received request to create maintenance record for plate: {}", maintenanceRecordDTO.getPlateNumber());
        try {
            MaintenanceRecordDTO createdRecord = maintenanceService.createMaintenanceRecord(maintenanceRecordDTO);
            return new ResponseEntity<>(createdRecord, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error creating maintenance record for plate {}: {}", maintenanceRecordDTO.getPlateNumber(), e.getMessage(), e);
            // Consider a more specific error response DTO
            return new ResponseEntity<>("Error creating maintenance record: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/vehicle-details")
    public ResponseEntity<?> getVehicleDetails(@RequestParam String plateNumber) {
        log.info("Received request to fetch vehicle details for plate: {}", plateNumber);
        Optional<VehicleDetailsDTO> vehicleDetails = maintenanceService.getVehicleDetailsByPlateNumber(plateNumber);
        if (vehicleDetails.isPresent()) {
            return ResponseEntity.ok(vehicleDetails.get());
        } else {
            log.warn("Vehicle not found with plate number: {}", plateNumber);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Vehicle not found with plate number: " + plateNumber);
        }
    }
}
