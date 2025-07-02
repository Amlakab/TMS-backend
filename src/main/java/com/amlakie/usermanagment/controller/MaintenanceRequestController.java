package com.amlakie.usermanagment.controller;

import com.amlakie.usermanagment.dto.MaintenanceRequestDTO;
import com.amlakie.usermanagment.entity.MaintenanceRequest;
import com.amlakie.usermanagment.exception.InvalidRequestException;
import com.amlakie.usermanagment.exception.ResourceNotFoundException;
import com.amlakie.usermanagment.service.MaintenanceRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/maintenance-requests")
public class MaintenanceRequestController {

    private final MaintenanceRequestService maintenanceRequestService;

    @Autowired
    public MaintenanceRequestController(MaintenanceRequestService maintenanceRequestService) {
        this.maintenanceRequestService = maintenanceRequestService;
    }

    @PostMapping
    public ResponseEntity<?> createRequest(@RequestBody MaintenanceRequestDTO requestDTO) {
        try {
            MaintenanceRequest createdRequest = maintenanceRequestService.createRequest(requestDTO);
            return new ResponseEntity<>(createdRequest, HttpStatus.CREATED);
        } catch (InvalidRequestException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/driver")
    public ResponseEntity<List<MaintenanceRequest>> getRequestsForDriver(
            @RequestParam(required = false) String driverName) {
        List<MaintenanceRequest> requests = maintenanceRequestService.getRequestsForDriver(driverName);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/distributor")
    public ResponseEntity<List<MaintenanceRequest>> getRequestsForDistributor() {
        List<MaintenanceRequest> requests = maintenanceRequestService.getRequestsForDistributor();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/maintenance")
    public ResponseEntity<List<MaintenanceRequest>> getRequestsForMaintenance() {
        List<MaintenanceRequest> requests = maintenanceRequestService.getRequestsForMaintenance();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/inspector")
    public ResponseEntity<List<MaintenanceRequest>> getRequestsForInspector() {
        List<MaintenanceRequest> requests = maintenanceRequestService.getRequestsForInspector();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/return")
    public ResponseEntity<List<MaintenanceRequest>> getRequestsForInspectorToReturn() {
        List<MaintenanceRequest> requests = maintenanceRequestService.getRequestsForInspectorToReturn();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRequestById(@PathVariable Long id) {
        try {
            MaintenanceRequest request = maintenanceRequestService.getRequestById(id);
            return ResponseEntity.ok(request);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRequest(
            @PathVariable Long id,
            @RequestBody MaintenanceRequestDTO requestDTO) {
        try {
            MaintenanceRequest updatedRequest = maintenanceRequestService.updateRequest(id, requestDTO);
            return ResponseEntity.ok(updatedRequest);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (InvalidRequestException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateRequestStatus(
            @PathVariable Long id,
            @RequestParam MaintenanceRequest.RequestStatus status) {
        try {
            MaintenanceRequest updatedRequest = maintenanceRequestService.updateRequestStatus(id, status);
            return ResponseEntity.ok(updatedRequest);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (InvalidRequestException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/acceptance")
    public ResponseEntity<?> submitAcceptance(
            @PathVariable Long id,
            @RequestBody MaintenanceRequestDTO acceptanceData) {
        try {
            MaintenanceRequest updatedRequest = maintenanceRequestService.submitAcceptance(id, acceptanceData);
            return ResponseEntity.ok(updatedRequest);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (InvalidRequestException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<?> submitReturn(
            @PathVariable Long id,
            @RequestBody MaintenanceRequestDTO returnData) {
        try {
            MaintenanceRequest updatedRequest = maintenanceRequestService.submitReturn(id, returnData);
            return ResponseEntity.ok(updatedRequest);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (InvalidRequestException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/upload-files")
    public ResponseEntity<?> uploadFiles(
            @PathVariable Long id,
            @RequestParam("files") MultipartFile[] files) {
        try {
            MaintenanceRequest updatedRequest = maintenanceRequestService.uploadFiles(id, files, false);
            return ResponseEntity.ok(updatedRequest);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (InvalidRequestException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload files");
        }
    }

    @PostMapping("/{id}/upload-return-files")
    public ResponseEntity<?> uploadReturnFiles(
            @PathVariable Long id,
            @RequestParam("files") MultipartFile[] files) {
        try {
            MaintenanceRequest updatedRequest = maintenanceRequestService.uploadFiles(id, files, true);
            return ResponseEntity.ok(updatedRequest);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (InvalidRequestException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload return files");
        }
    }

    // MaintenanceRequestController.java - Add this new endpoint
    @PatchMapping("/{id}/complete")
    public ResponseEntity<?> completeRequest(@PathVariable Long id) {
        try {
            MaintenanceRequest completedRequest = maintenanceRequestService.completeRequest(id);
            return ResponseEntity.ok(completedRequest);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (InvalidRequestException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PostMapping("/{id}/complete-return")
    public ResponseEntity<?> completeReturnProcess(
            @PathVariable Long id,
            @RequestBody MaintenanceRequestDTO returnData) {
        try {
            MaintenanceRequest completedRequest = maintenanceRequestService.completeReturnProcess(id, returnData);
            return ResponseEntity.ok(completedRequest);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (InvalidRequestException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/files/{filename}")
    public ResponseEntity<?> getFile(@PathVariable String filename) {
        try {
            byte[] fileBytes = maintenanceRequestService.getFile(filename);
            String contentType = maintenanceRequestService.getFileContentType(filename);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDispositionFormData("inline", filename);

            return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
        }
    }
}