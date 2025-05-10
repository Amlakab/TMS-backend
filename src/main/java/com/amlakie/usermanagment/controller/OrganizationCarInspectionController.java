package com.amlakie.usermanagment.controller;

import com.amlakie.usermanagment.dto.organization.OrganizationCarInspectionListResponse;
import com.amlakie.usermanagment.dto.organization.OrganizationCarInspectionReqRes;
import com.amlakie.usermanagment.exception.ResourceNotFoundException;
import com.amlakie.usermanagment.service.OrganizationCarInspectionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/org-inspections")
public class OrganizationCarInspectionController {

    private static final Logger log = LoggerFactory.getLogger(OrganizationCarInspectionController.class);
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private final OrganizationCarInspectionService orgInspectionService;

    // Constructor injection is generally preferred over field injection
    @Autowired
    public OrganizationCarInspectionController(OrganizationCarInspectionService orgInspectionService) {
        this.orgInspectionService = orgInspectionService;
    }

    @PostMapping("/create")
    public ResponseEntity<OrganizationCarInspectionReqRes> createOrgInspection(
            @Valid @RequestBody OrganizationCarInspectionReqRes request) { // Added @Valid
        try {
            log.info("Received POST /api/org-inspections/create for plate: {}", request.getPlateNumber());
            // Log the entire incoming DTO to see if phase details are present
            log.debug("Full incoming request DTO for create: {}", objectMapper.writeValueAsString(request));
            log.debug("Mechanical Details from request: {}", request.getMechanicalDetails());
            log.debug("Body Details from request: {}", request.getBodyDetails());
            log.debug("Interior Details from request: {}", request.getInteriorDetails());
        } catch (JsonProcessingException e) {
            log.error("Error serializing request DTO to JSON for logging during create", e);
        } catch (NullPointerException e) {
            log.error("Null pointer encountered while logging request details. Request or plateNumber might be null.", e);
        }


        OrganizationCarInspectionReqRes createdInspection = orgInspectionService.createInspection(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdInspection.getId())
                .toUri();
        log.info("Successfully created organization car inspection with ID: {}", createdInspection.getId());
        return ResponseEntity.created(location).body(createdInspection);
    }

    @GetMapping("/get-all")
    public ResponseEntity<OrganizationCarInspectionListResponse> getAllInspections() {
        log.info("Received GET /api/org-inspections/get-all");
        return ResponseEntity.ok(orgInspectionService.getAllInspections());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<OrganizationCarInspectionReqRes> getInspectionById(@PathVariable Long id) {
        log.info("Received GET /api/org-inspections/get/{}", id);
        // Consider using a global exception handler (@ControllerAdvice)
        // instead of try-catch in each controller method for cleaner code.
        try {
            OrganizationCarInspectionReqRes inspection = orgInspectionService.getInspectionById(id);
            return ResponseEntity.ok(inspection);
        } catch (ResourceNotFoundException e) {
            log.warn("ResourceNotFoundException for ID {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<OrganizationCarInspectionReqRes> updateInspection(
            @PathVariable Long id,
            @Valid @RequestBody OrganizationCarInspectionReqRes request) {
        try {
            log.info("Received PUT /api/org-inspections/update/{} for plate: {}", id, request.getPlateNumber());
            log.debug("Full incoming request DTO for update: {}", objectMapper.writeValueAsString(request));
        } catch (JsonProcessingException e) {
            log.error("Error serializing request DTO to JSON for logging during update", e);
        } catch (NullPointerException e) {
            log.error("Null pointer encountered while logging request details for update. Request or plateNumber might be null.", e);
        }

        // Consider using a global exception handler
        try {
            OrganizationCarInspectionReqRes updatedInspection = orgInspectionService.updateInspection(id, request);
            log.info("Successfully updated organization car inspection with ID: {}", id);
            return ResponseEntity.ok(updatedInspection);
        } catch (ResourceNotFoundException e) {
            log.warn("ResourceNotFoundException during update for ID {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/by-plate/{plateNumber}")
    public ResponseEntity<OrganizationCarInspectionListResponse> getInspectionsByPlateNumber(
            @PathVariable String plateNumber) {
        log.info("Received GET /api/org-inspections/by-plate/{}", plateNumber);
        return ResponseEntity.ok(orgInspectionService.getInspectionsByPlateNumber(plateNumber));
    }
}