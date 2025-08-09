package com.amlakie.usermanagment.controller;

import com.amlakie.usermanagment.dto.GpsDataDTO;
import com.amlakie.usermanagment.dto.VehicleLocationResponseDTO;
import com.amlakie.usermanagment.service.VehicleTrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

/**
 * Controller for handling vehicle tracking operations via both WebSocket and REST APIs.
 */
@Controller
@RequestMapping("/api/vehicle-tracking")
@RequiredArgsConstructor
public class VehicleTrackingController {
    private final VehicleTrackingService vehicleTrackingService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * WebSocket endpoint for receiving real-time GPS data from vehicles.
     *
     * @param gpsDataDTO The GPS data received from the vehicle
     * @param principal The authenticated user sending the data
     */
    @MessageMapping("/vehicle-gps")
    public void handleGpsDataWebSocket(@Payload @Valid GpsDataDTO gpsDataDTO, Principal principal) {
        try {
            // Process and save the GPS data
            VehicleLocationResponseDTO response = vehicleTrackingService.saveGpsData(gpsDataDTO);

            // Broadcast the update to all subscribers
            messagingTemplate.convertAndSend("/topic/vehicle-updates", response);

            // Send acknowledgment back to sender
            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/acknowledgments",
                    new AckResponse("GPS data processed successfully", response)
            );
        } catch (Exception e) {
            // Send error notification to sender
            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/errors",
                    new ErrorResponse("Failed to process GPS data", e.getMessage())
            );
        }
    }

    /**
     * REST endpoint for receiving GPS data via HTTP POST.
     *
     * @param gpsDataDTO The GPS data to be processed
     * @return ResponseEntity containing the processed vehicle location data
     */
    @PostMapping("/gps")
    public ResponseEntity<VehicleLocationResponseDTO> handleGpsDataRest(@Valid @RequestBody GpsDataDTO gpsDataDTO) {
        VehicleLocationResponseDTO response = vehicleTrackingService.saveGpsData(gpsDataDTO);
        messagingTemplate.convertAndSend("/topic/vehicle-updates", response);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all latest vehicle locations.
     *
     * @return ResponseEntity containing list of latest vehicle locations
     */
    @GetMapping("/locations")
    public ResponseEntity<List<VehicleLocationResponseDTO>> getAllLatestLocations() {
        return ResponseEntity.ok(vehicleTrackingService.getAllLatestLocations());
    }

    /**
     * Get latest locations by vehicle type.
     *
     * @param vehicleType The type of vehicle (e.g., "ORGANIZATION", "RENT")
     * @return ResponseEntity containing list of vehicle locations
     */
    @GetMapping("/locations/type/{vehicleType}")
    public ResponseEntity<List<VehicleLocationResponseDTO>> getLatestLocationsByType(
            @PathVariable String vehicleType) {
        return ResponseEntity.ok(vehicleTrackingService.getLatestLocationsByType(vehicleType));
    }

    /**
     * Get latest location by device IMEI.
     *
     * @param imei The device IMEI number
     * @return ResponseEntity containing the vehicle location
     */
    @GetMapping("/locations/imei/{imei}")
    public ResponseEntity<VehicleLocationResponseDTO> getLatestLocationByImei(
            @PathVariable String imei) {
        return ResponseEntity.ok(vehicleTrackingService.getLatestLocationByImei(imei));
    }

    /**
     * Get location history for a specific vehicle.
     *
     * @param vehicleId The ID of the vehicle
     * @param vehicleType The type of vehicle
     * @return ResponseEntity containing list of historical locations
     */
    @GetMapping("/history/{vehicleId}")
    public ResponseEntity<List<VehicleLocationResponseDTO>> getLocationHistory(
            @PathVariable Long vehicleId,
            @RequestParam String vehicleType) {
        return ResponseEntity.ok(vehicleTrackingService.getLocationHistory(vehicleId, vehicleType));
    }

    /**
     * Get location history for a specific vehicle between two timestamps.
     *
     * @param vehicleId The ID of the vehicle
     * @param vehicleType The type of vehicle
     * @param start The start timestamp
     * @param end The end timestamp
     * @return ResponseEntity containing list of historical locations
     */
    @GetMapping("/history/{vehicleId}/between")
    public ResponseEntity<List<VehicleLocationResponseDTO>> getLocationHistoryBetween(
            @PathVariable Long vehicleId,
            @RequestParam String vehicleType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(vehicleTrackingService.getLocationHistoryBetween(
                vehicleId, vehicleType, start, end));
    }

    // Response DTOs for WebSocket acknowledgments
    private record AckResponse(String message, VehicleLocationResponseDTO data) {}
    private record ErrorResponse(String error, String details) {}
}