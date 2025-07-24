package com.amlakie.usermanagment.controller;

import com.amlakie.usermanagment.dto.GpsDataDTO;
import com.amlakie.usermanagment.dto.VehicleLocationResponseDTO;
import com.amlakie.usermanagment.service.VehicleTrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/vehicle-tracking")
@RequiredArgsConstructor
public class VehicleTrackingController {
    private final VehicleTrackingService vehicleTrackingService;

    @PostMapping("/gps")
    public ResponseEntity<VehicleLocationResponseDTO> handleGpsData(@Valid @RequestBody GpsDataDTO gpsDataDTO) {
        return ResponseEntity.ok(vehicleTrackingService.saveGpsData(gpsDataDTO));
    }

    @GetMapping("/locations")
    public ResponseEntity<List<VehicleLocationResponseDTO>> getAllLatestLocations() {
        return ResponseEntity.ok(vehicleTrackingService.getAllLatestLocations());
    }

    @GetMapping("/locations/type/{vehicleType}")
    public ResponseEntity<List<VehicleLocationResponseDTO>> getLatestLocationsByType(
            @PathVariable String vehicleType) {
        return ResponseEntity.ok(vehicleTrackingService.getLatestLocationsByType(vehicleType));
    }

    @GetMapping("/locations/imei/{imei}")
    public ResponseEntity<VehicleLocationResponseDTO> getLatestLocationByImei(
            @PathVariable String imei) {
        return ResponseEntity.ok(vehicleTrackingService.getLatestLocationByImei(imei));
    }

    @GetMapping("/history/{vehicleId}")
    public ResponseEntity<List<VehicleLocationResponseDTO>> getLocationHistory(
            @PathVariable Long vehicleId,
            @RequestParam String vehicleType) {
        return ResponseEntity.ok(vehicleTrackingService.getLocationHistory(vehicleId, vehicleType));
    }

    @GetMapping("/history/{vehicleId}/between")
    public ResponseEntity<List<VehicleLocationResponseDTO>> getLocationHistoryBetween(
            @PathVariable Long vehicleId,
            @RequestParam String vehicleType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(vehicleTrackingService.getLocationHistoryBetween(
                vehicleId, vehicleType, start, end));
    }
}