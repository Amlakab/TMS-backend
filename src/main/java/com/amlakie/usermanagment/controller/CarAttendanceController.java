package com.amlakie.usermanagment.controller;

import com.amlakie.usermanagment.dto.attendance.*;
import com.amlakie.usermanagment.service.CarAttendanceService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/car-attendance") // Consistent API versioning
public class CarAttendanceController {

    private static final Logger logger = LoggerFactory.getLogger(CarAttendanceController.class); // Declare and initialize the logger
    private final CarAttendanceService carAttendanceService;

    @Autowired
    public CarAttendanceController(CarAttendanceService carAttendanceService) {
        this.carAttendanceService = carAttendanceService;
    }

    @PostMapping("/morning-arrival")
    public ResponseEntity<CarAttendanceResponseDTO> recordMorningArrival(@Valid @RequestBody MorningArrivalRequestDTO requestDTO) {
        // The service will throw exceptions (ValidationException, ResourceNotFoundException)
        // which should be handled by a @ControllerAdvice global exception handler for cleaner code.
        CarAttendanceResponseDTO response = carAttendanceService.recordMorningArrival(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PostMapping("/evening-departure/{attendanceId}")
    public ResponseEntity<CarAttendanceResponseDTO> recordEveningDeparture(
            @PathVariable Long attendanceId,
            @Valid @RequestBody EveningDepartureRequestDTO requestDTO) {
        CarAttendanceResponseDTO response = carAttendanceService.recordEveningDeparture(attendanceId, requestDTO);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/today-morning-arrival/{plateNumber}/{vehicleType}")
    public ResponseEntity<CarAttendanceResponseDTO> findTodaysMorningArrivalRecord(
            @PathVariable String plateNumber,
            @PathVariable String vehicleType) { // vehicleType is "CAR" or "ORGANIZATION_CAR"
        CarAttendanceResponseDTO record = carAttendanceService.findTodaysMorningArrivalRecord(plateNumber, vehicleType);
        if (record == null) {
            // Frontend mock returns null, so sending an empty body with 200 OK or 204 No Content is appropriate.
            // Let's go with 204 No Content if nothing is found.
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(record);
    }

    /**
     * Finds the last recorded evening departure for a specific vehicle.
     * Corresponds to the frontend's 'findLastEveningDepartureRecordAPI'.
     */
    @GetMapping("/last-evening-departure/{plateNumber}/{vehicleType}")
    public ResponseEntity<CarAttendanceResponseDTO> findLastEveningDepartureRecord(
            @PathVariable String plateNumber,
            @PathVariable String vehicleType) { // vehicleType is "CAR" or "ORGANIZATION_CAR"
        CarAttendanceResponseDTO record = carAttendanceService.findLastEveningDepartureRecord(plateNumber, vehicleType);
        if (record == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(record);
    }


    // --- Additional Endpoints from your existing controller (adapted) ---


    @GetMapping("/{id}")
    public ResponseEntity<CarAttendanceResponseDTO> getAttendanceById(@PathVariable Long id) {
        // Assuming your service has a method like this:
        return carAttendanceService.getAttendanceById(id) // You'll need to add this method to your service
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping
    public ResponseEntity<List<CarAttendanceResponseDTO>> getAllAttendanceRecords() {
        // Assuming your service has a method like this:
        List<CarAttendanceResponseDTO> records = carAttendanceService.getAllAttendanceRecords(); // Add to service
        if (records.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(records);
    }


    @GetMapping("/vehicle/{plateNumber}/{vehicleType}")
    public ResponseEntity<List<CarAttendanceResponseDTO>> getAttendanceForVehicle(
            @PathVariable String plateNumber,
            @PathVariable String vehicleType) {
        List<CarAttendanceResponseDTO> records = carAttendanceService.getAttendanceForVehicle(plateNumber, vehicleType); // Add to service
        if (records.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(records);
    }
    @PostMapping("/fuel-entries") // Matches the frontend URL
    public ResponseEntity<FuelEntryResponseDTO> recordFuelEntry(@Valid @RequestBody FuelEntryRequestDTO requestDTO) { // Added @Valid
        logger.info("Received request to record fuel entry: {}", requestDTO);
        FuelEntryResponseDTO response = carAttendanceService.recordFuelEntry(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}