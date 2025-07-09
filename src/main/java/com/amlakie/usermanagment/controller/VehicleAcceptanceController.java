package com.amlakie.usermanagment.controller;

import com.amlakie.usermanagment.dto.VehicleAcceptanceRequest;
import com.amlakie.usermanagment.dto.VehicleAcceptanceResponse;
import com.amlakie.usermanagment.service.VehicleAcceptanceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/vehicle-acceptance")
public class VehicleAcceptanceController {

    private final VehicleAcceptanceService vehicleAcceptanceService;
    private final ObjectMapper objectMapper;

    public VehicleAcceptanceController(VehicleAcceptanceService vehicleAcceptanceService,
                                       ObjectMapper objectMapper) {
        this.vehicleAcceptanceService = vehicleAcceptanceService;
        this.objectMapper = objectMapper;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VehicleAcceptanceResponse> createVehicleAcceptance(
            @RequestParam("data") String vehicleData,
            @RequestParam(value = "images", required = false) MultipartFile[] images) throws IOException {

        VehicleAcceptanceRequest request = objectMapper.readValue(vehicleData, VehicleAcceptanceRequest.class);
        VehicleAcceptanceResponse response = vehicleAcceptanceService.create(request, images);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VehicleAcceptanceResponse> updateVehicleAcceptance(
            @PathVariable Long id,
            @RequestParam("data") String vehicleData,
            @RequestParam(value = "images", required = false) MultipartFile[] images) throws IOException {

        VehicleAcceptanceRequest request = objectMapper.readValue(vehicleData, VehicleAcceptanceRequest.class);
        VehicleAcceptanceResponse response = vehicleAcceptanceService.update(id, request, images);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleAcceptanceResponse> getVehicleAcceptanceById(@PathVariable Long id) {
        VehicleAcceptanceResponse response = vehicleAcceptanceService.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<VehicleAcceptanceResponse> getVehicleAcceptanceByAssignment(
            @PathVariable Long assignmentId) {
        VehicleAcceptanceResponse response = vehicleAcceptanceService.getByAssignmentHistoryId(assignmentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/assignment/{assignmentId}/all")
    public ResponseEntity<List<VehicleAcceptanceResponse>> getAllVehicleAcceptancesByAssignment(
            @PathVariable Long assignmentId) {
        List<VehicleAcceptanceResponse> responses = vehicleAcceptanceService
                .getAllByAssignmentHistoryId(assignmentId);
        return ResponseEntity.ok(responses);
    }


    // Get latest acceptance by plate number
    @GetMapping("/plate/{plateNumber}")
    public ResponseEntity<VehicleAcceptanceResponse> getLatestByPlateNumber(
            @PathVariable String plateNumber) {
        return ResponseEntity.ok(vehicleAcceptanceService.getLatestByPlateNumber(plateNumber));
    }

    // Get all acceptances by plate number
    @GetMapping("/plate/{plateNumber}/all")
    public ResponseEntity<List<VehicleAcceptanceResponse>> getAllByPlateNumber(
            @PathVariable String plateNumber) {
        return ResponseEntity.ok(vehicleAcceptanceService.getAllByPlateNumber(plateNumber));
    }

    @GetMapping
    public ResponseEntity<Page<VehicleAcceptanceResponse>> getAll(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(vehicleAcceptanceService.getAllVehicleAcceptances(pageable));
    }


    // Get all vehicle acceptances (non-paginated)
    @GetMapping("/all")
    public ResponseEntity<List<VehicleAcceptanceResponse>> getAll() {
        return ResponseEntity.ok(vehicleAcceptanceService.getAllVehicleAcceptances());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicleAcceptance(@PathVariable Long id) throws IOException {
        vehicleAcceptanceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/plate/{plateNumber}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VehicleAcceptanceResponse> updateAssignmentByPlate(
           @PathVariable String plateNumber,
           @RequestBody VehicleAcceptanceRequest request) {

       VehicleAcceptanceResponse response =
               vehicleAcceptanceService.updateAssignmentHistoryByPlate(plateNumber, request);

       return ResponseEntity.status(response.getStatusCode()).body(response);
   }
}



