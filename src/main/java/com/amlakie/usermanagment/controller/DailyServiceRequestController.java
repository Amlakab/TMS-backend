package com.amlakie.usermanagment.controller;

import com.amlakie.usermanagment.dto.*;
import com.amlakie.usermanagment.entity.DailyServiceRequest;
import com.amlakie.usermanagment.service.DailyServiceRequestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/daily-requests")
public class DailyServiceRequestController {

    private final DailyServiceRequestService service;

    @Autowired
    public DailyServiceRequestController(DailyServiceRequestService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public ResponseEntity<DailyServiceRequest> createRequest(@Valid @RequestBody DailyServiceRequestDTO dto) {
        return ResponseEntity.ok(service.createRequest(dto));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<DailyServiceRequest>> getPendingRequests() {
        return ResponseEntity.ok(service.getPendingRequests());
    }

    @GetMapping("/all")
    public ResponseEntity<List<DailyServiceRequest>> getAllRequests() {
        return ResponseEntity.ok(service.getAllRequests());
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<DailyServiceRequest> assignRequest(
            @PathVariable Long id,
            @Valid @RequestBody AssignmentDTO dto) {
        return ResponseEntity.ok(service.assignRequest(id, dto));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<DailyServiceRequest> completeRequest(
            @PathVariable Long id,
            @Valid @RequestBody CompletionDTO dto) {
        return ResponseEntity.ok(service.completeRequest(id, dto));
    }

    @GetMapping("/driver")
    public ResponseEntity<List<DailyServiceRequest>> getDriverRequests(
            @RequestParam(required = false) String driverName) {
        return ResponseEntity.ok(service.getRequestsForDriver(driverName));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DailyServiceRequest> getRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getRequestById(id));
    }
}