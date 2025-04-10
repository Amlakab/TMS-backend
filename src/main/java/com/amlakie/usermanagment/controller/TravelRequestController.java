package com.amlakie.usermanagment.controller;

import com.amlakie.usermanagment.dto.TravelRequestDTO;
import com.amlakie.usermanagment.entity.TravelRequest;
import com.amlakie.usermanagment.exception.InvalidRequestException;
import com.amlakie.usermanagment.service.TravelRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/travel-requests")
public class TravelRequestController {

    @Autowired
    private TravelRequestService travelRequestService;

    @PostMapping
    public ResponseEntity<?> createRequest(@Valid @RequestBody TravelRequestDTO requestDTO) {
        try {
            TravelRequest created = travelRequestService.createRequest(requestDTO);
            return ResponseEntity.ok(created);
        } catch (InvalidRequestException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user")
    public ResponseEntity<List<TravelRequest>> getUserRequests() {
        List<TravelRequest> requests = travelRequestService.getRequestsForUser();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/manager")
    public ResponseEntity<List<TravelRequest>> getManagerRequests() {
        List<TravelRequest> requests = travelRequestService.getRequestsForManager();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/corporator")
    public ResponseEntity<List<TravelRequest>> getCorporatorRequests() {
        List<TravelRequest> requests = travelRequestService.getRequestsForCorporator();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TravelRequest> getRequestDetails(@PathVariable Long id) {
        TravelRequest request = travelRequestService.getRequestById(id);
        return ResponseEntity.ok(request);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TravelRequest> updateRequestStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        try {
            TravelRequest.RequestStatus statusEnum = TravelRequest.RequestStatus.valueOf(status.toUpperCase());
            TravelRequest updatedRequest = travelRequestService.updateRequestStatus(id, statusEnum);
            return ResponseEntity.ok(updatedRequest);
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("Invalid status value: " + status);
        }
    }

    @PostMapping("/{id}/complete")
    @ResponseBody
    public ResponseEntity<String> completeRequest(
            @PathVariable Long id,
            @RequestBody TravelRequestDTO serviceData) {
        try {
            TravelRequest completed = travelRequestService.completeRequest(id, serviceData);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ObjectMapper().writeValueAsString(completed));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}