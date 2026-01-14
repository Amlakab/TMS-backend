package com.amlakie.usermanagment.controller;

import com.amlakie.usermanagment.dto.GpsDataDTO;
import com.amlakie.usermanagment.dto.VehicleLocationResponseDTO;
import com.amlakie.usermanagment.service.VehicleTrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/api/vehicle-tracking")
@RequiredArgsConstructor
public class VehicleTrackingController {
    private final VehicleTrackingService vehicleTrackingService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * WebSocket endpoint for receiving real-time GPS data from vehicles/mobile apps/GPS devices.
     */
    @MessageMapping("/vehicle-location")
    public void handleVehicleLocation(@Payload @Valid GpsDataDTO gpsDataDTO, Principal principal) {
        try {
            log.info("📍 Received WebSocket location update - Plate: {}, IMEI: {}",
                    gpsDataDTO.getPlateNumber(), gpsDataDTO.getImei());

            // Process and save the GPS data
            VehicleLocationResponseDTO response = vehicleTrackingService.processAndSaveLocation(gpsDataDTO);

            // Send acknowledgment back to sender
            if (principal != null) {
                messagingTemplate.convertAndSendToUser(
                        principal.getName(),
                        "/queue/location-acknowledgments",
                        new AckResponse("Location data processed successfully", response)
                );
            }
        } catch (Exception e) {
            log.error("❌ Error processing WebSocket location: {}", e.getMessage(), e);
            // Send error notification to sender
            if (principal != null) {
                messagingTemplate.convertAndSendToUser(
                        principal.getName(),
                        "/queue/location-errors",
                        new ErrorResponse("Failed to process location data", e.getMessage())
                );
            }
        }
    }

    /**
     * REST endpoint for receiving GPS data via HTTP POST
     */
    @PostMapping("/location")
    public ResponseEntity<VehicleLocationResponseDTO> handleLocationRest(@Valid @RequestBody GpsDataDTO gpsDataDTO) {
        try {
            log.info("📍 Received REST location update - Plate: {}, IMEI: {}",
                    gpsDataDTO.getPlateNumber(), gpsDataDTO.getImei());

            VehicleLocationResponseDTO response = vehicleTrackingService.processAndSaveLocation(gpsDataDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Error processing REST location: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get all latest vehicle locations.
     */
    @GetMapping("/locations")
    public ResponseEntity<List<VehicleLocationResponseDTO>> getAllLatestLocations() {
        try {
            List<VehicleLocationResponseDTO> locations = vehicleTrackingService.getAllLatestLocations();
            return ResponseEntity.ok(locations);
        } catch (Exception e) {
            log.error("❌ Error getting all latest locations: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get latest locations by vehicle type.
     */
    @GetMapping("/locations/type/{vehicleType}")
    public ResponseEntity<List<VehicleLocationResponseDTO>> getLatestLocationsByType(
            @PathVariable String vehicleType) {
        try {
            List<VehicleLocationResponseDTO> locations = vehicleTrackingService.getLatestLocationsByType(vehicleType);
            return ResponseEntity.ok(locations);
        } catch (Exception e) {
            log.error("❌ Error getting latest locations by type {}: {}", vehicleType, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get latest location by device IMEI.
     */
    @GetMapping("/locations/imei/{imei}")
    public ResponseEntity<VehicleLocationResponseDTO> getLatestLocationByImei(
            @PathVariable String imei) {
        try {
            VehicleLocationResponseDTO location = vehicleTrackingService.getLatestLocationByImei(imei);
            return ResponseEntity.ok(location);
        } catch (Exception e) {
            log.error("❌ Error getting latest location by IMEI {}: {}", imei, e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get location history for a specific vehicle.
     */
    @GetMapping("/history/{vehicleId}")
    public ResponseEntity<List<VehicleLocationResponseDTO>> getLocationHistory(
            @PathVariable Long vehicleId,
            @RequestParam String vehicleType) {
        try {
            List<VehicleLocationResponseDTO> history = vehicleTrackingService.getLocationHistory(vehicleId, vehicleType);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("❌ Error getting location history for vehicle {} (type {}): {}",
                    vehicleId, vehicleType, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get location history for a specific vehicle between two timestamps.
     */
    @GetMapping("/history/{vehicleId}/between")
    public ResponseEntity<List<VehicleLocationResponseDTO>> getLocationHistoryBetween(
            @PathVariable Long vehicleId,
            @RequestParam String vehicleType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        try {
            List<VehicleLocationResponseDTO> history = vehicleTrackingService.getLocationHistoryBetween(
                    vehicleId, vehicleType, start, end);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("❌ Error getting location history for vehicle {} (type {}) between {} and {}: {}",
                    vehicleId, vehicleType, start, end, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Response DTOs for WebSocket acknowledgments
    private record AckResponse(String message, VehicleLocationResponseDTO data) {}
    private record ErrorResponse(String error, String details) {}
}