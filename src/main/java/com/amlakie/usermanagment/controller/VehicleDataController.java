package com.amlakie.usermanagment.controller;

import com.amlakie.usermanagment.dto.vehicle.BackendPlateSuggestionDTO;
import com.amlakie.usermanagment.dto.vehicle.BackendVehicleDTO;
import com.amlakie.usermanagment.service.VehicleDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/vehicles") // Matches frontend's API_VEHICLES_BASE_URL
public class VehicleDataController {

    private final VehicleDataService vehicleDataService;

    public VehicleDataController(VehicleDataService vehicleDataService) {
        this.vehicleDataService = vehicleDataService;
    }

    @GetMapping("/details/{plateNumber}/{vehicleType}")
    public ResponseEntity<BackendVehicleDTO> getVehicleDetails(
            @PathVariable String plateNumber,
            @PathVariable String vehicleType) {
        return vehicleDataService.getVehicleDetails(plateNumber, vehicleType)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/by-plate/{plateNumber}")
    public ResponseEntity<BackendVehicleDTO> getVehicleDetailsByPlate(@PathVariable String plateNumber) {
        Optional<BackendVehicleDTO> vehicleDetailsOpt = vehicleDataService.getVehicleDetailsByPlate(plateNumber);
        return vehicleDetailsOpt
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build()); // Return 404 if not found by plate
    }
    @GetMapping("/suggestions")
    public ResponseEntity<List<BackendPlateSuggestionDTO>> getPlateSuggestions(
            @RequestParam(required = false, defaultValue = "") String query,
            @RequestParam(required = false) String vehicleType) {
        List<BackendPlateSuggestionDTO> suggestions = vehicleDataService.getPlateSuggestions(query, vehicleType);
        return ResponseEntity.ok(suggestions);
    }
}
    