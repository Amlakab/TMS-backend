package com.amlakie.usermanagment.controller;

import com.amlakie.usermanagment.dto.fogrequest.FuelOilGreaseRequestDTO;
import com.amlakie.usermanagment.dto.fogrequest.RequestItemDTO;
import com.amlakie.usermanagment.service.FuelOilGreaseRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class FuelOilGreaseRequestController {
    private final FuelOilGreaseRequestService requestService;

    @PostMapping
    public ResponseEntity<FuelOilGreaseRequestDTO> createRequest(@RequestBody FuelOilGreaseRequestDTO requestDTO) {
        return ResponseEntity.ok(requestService.createRequest(requestDTO));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<FuelOilGreaseRequestDTO> submitRequest(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.submitRequest(id));
    }

    @PutMapping("/{id}/head-mechanic-review")
    public ResponseEntity<FuelOilGreaseRequestDTO> headMechanicReview(
            @PathVariable Long id,
            @RequestParam String headMechanicName,
            @RequestParam boolean isApproved) {
        return ResponseEntity.ok(requestService.headMechanicReview(id, headMechanicName, isApproved));
    }

    @PutMapping("/{id}/nezek-review")
    public ResponseEntity<FuelOilGreaseRequestDTO> nezekReview(
            @PathVariable Long id,
            @RequestParam String nezekOfficialName,
            @RequestParam boolean isApproved) {
        return ResponseEntity.ok(requestService.nezekReview(id, nezekOfficialName, isApproved));
    }

    @PutMapping("/{id}/fulfill")
    public ResponseEntity<FuelOilGreaseRequestDTO> fulfillRequest(
            @PathVariable Long id,
            @RequestBody List<RequestItemDTO> filledItems) {
        return ResponseEntity.ok(requestService.fulfillRequest(id, filledItems));
    }

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