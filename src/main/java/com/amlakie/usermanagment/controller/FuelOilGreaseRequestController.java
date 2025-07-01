package com.amlakie.usermanagment.controller;

import com.amlakie.usermanagment.dto.fogrequest.FuelOilGreaseRequestDTO;
import com.amlakie.usermanagment.service.FuelOilGreaseRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fuel-requests")
@RequiredArgsConstructor
public class FuelOilGreaseRequestController {

    private final FuelOilGreaseRequestService requestService;

    @PostMapping
    public ResponseEntity<FuelOilGreaseRequestDTO> createRequest(
            @Valid @RequestBody FuelOilGreaseRequestDTO requestDTO) {
        return new ResponseEntity<>(requestService.createRequest(requestDTO), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<FuelOilGreaseRequestDTO> submitRequest(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.submitRequest(id));
    }

    /**
     * IMPROVEMENT: This endpoint now handles both editing and approving/rejecting.
     * The approval decision and reviewer name are expected in the request body.
     */
    @PutMapping("/{id}/head-mechanic-review")
    public ResponseEntity<FuelOilGreaseRequestDTO> headMechanicReview(
            @PathVariable Long id,
            @Valid @RequestBody FuelOilGreaseRequestDTO reviewDTO) { // The DTO contains all edits and the approval
        return ResponseEntity.ok(requestService.headMechanicReview(id, reviewDTO));
    }

    /**
     * IMPROVEMENT: This endpoint now handles both editing and approving/rejecting.
     * The approval decision and reviewer name are expected in the request body.
     */
    @PutMapping("/{id}/nezek-review")
    public ResponseEntity<FuelOilGreaseRequestDTO> nezekReview(
            @PathVariable Long id,
            @Valid @RequestBody FuelOilGreaseRequestDTO reviewDTO) { // The DTO contains all edits and the approval
        return ResponseEntity.ok(requestService.nezekReview(id, reviewDTO));
    }

    @PutMapping("/{id}/fulfill")
    public ResponseEntity<FuelOilGreaseRequestDTO> fulfillRequest(
            @PathVariable Long id,
            @RequestBody FuelOilGreaseRequestDTO requestDTO) {
        return ResponseEntity.ok(requestService.fulfillRequest(id, requestDTO));
    }

    // --- GET Endpoints ---
    @GetMapping("/pending")
    public ResponseEntity<List<FuelOilGreaseRequestDTO>> getPendingRequests() {
        return ResponseEntity.ok(requestService.getPendingRequests());
    }

    @GetMapping("/checked")
    public ResponseEntity<List<FuelOilGreaseRequestDTO>> getCheckedRequests() {
        return ResponseEntity.ok(requestService.getCheckedRequests());
    }

    @GetMapping("/approved")
    public ResponseEntity<List<FuelOilGreaseRequestDTO>> getApprovedRequests() {
        return ResponseEntity.ok(requestService.getApprovedRequests());
    }

    @GetMapping("/draft")
    public ResponseEntity<List<FuelOilGreaseRequestDTO>> getDraftRequests() {
        return ResponseEntity.ok(requestService.getDraftRequests());
    }

    @GetMapping("/mechanic/{mechanicName}")
    public ResponseEntity<List<FuelOilGreaseRequestDTO>> getRequestsByMechanic(
            @PathVariable String mechanicName) {
        return ResponseEntity.ok(requestService.getRequestsByMechanic(mechanicName));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FuelOilGreaseRequestDTO> getRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.getRequestById(id));
    }
}