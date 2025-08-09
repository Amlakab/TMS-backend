package com.amlakie.usermanagment.controller;

import com.amlakie.usermanagment.dto.*;
import com.amlakie.usermanagment.entity.AssignmentHistory;
import com.amlakie.usermanagment.exception.ResourceNotFoundException;
import com.amlakie.usermanagment.repository.AssignmentHistoryRepository;
import com.amlakie.usermanagment.service.CarManagementService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class CarManagementController {

    @Autowired
    private CarManagementService carManagementService;

    @Autowired
    private AssignmentHistoryRepository assignmentHistoryRepository;

    @PostMapping("/auth/car/register")
    public CarReqRes registerCar(@RequestBody CarReqRes registrationRequest) {
        return carManagementService.registerCar(registrationRequest);
    }

    @GetMapping("/auth/car/all")
    public ResponseEntity<CarReqRes> getAllCars() {
        return ResponseEntity.ok(carManagementService.getAllCars());
    }

    @GetMapping("/auth/car/{id}")
    public ResponseEntity<CarReqRes> getCarById(@PathVariable Long id) {
        return ResponseEntity.ok(carManagementService.getCarById(id));
    }

    @PutMapping("/auth/car/update/{id}")
    public ResponseEntity<CarReqRes> updateCar(@PathVariable Long id, @RequestBody CarReqRes updateRequest) {
        return ResponseEntity.ok(carManagementService.updateCar(id, updateRequest));
    }

    @DeleteMapping("/auth/car/delete/{id}")
    public ResponseEntity<CarReqRes> deleteCar(@PathVariable Long id) {
        return ResponseEntity.ok(carManagementService.deleteCar(id));
    }

    @GetMapping("/auth/car/search")
    public ResponseEntity<CarReqRes> searchCars(@RequestParam String query) {
        return ResponseEntity.ok(carManagementService.searchCars(query));
    }

    @PutMapping("/auth/car/status/{plateNumber}")
    public ResponseEntity<CarReqRes> updateStatus(@PathVariable String plateNumber, @RequestBody CarReqRes updateRequest) {
        return ResponseEntity.ok(carManagementService.updateStatus(plateNumber, updateRequest));
    }

    @PostMapping("/auth/car/assign")
    public ResponseEntity<CarReqRes> createAssignment(
            @Valid @ModelAttribute AssignmentRequest request,
            @RequestParam(value = "driverLicenseFile", required = false) MultipartFile driverLicenseFile) {
        request.setDriverLicenseFile(driverLicenseFile);
        return ResponseEntity.ok(carManagementService.createAssignment(request));
    }

    @GetMapping("/auth/licenses/expiring")
    public ResponseEntity<CarReqRes> getExpiringLicenses() {
        return ResponseEntity.ok(carManagementService.getExpiringLicenses());
    }

    @GetMapping("/auth/car/approved")
    public ResponseEntity<CarReqRes> getApprovedCars() {
        return ResponseEntity.ok(carManagementService.getApprovedCars());
    }

    @GetMapping("/auth/car/in-transfer")
    public ResponseEntity<CarReqRes> getInTransferCars() {
        return ResponseEntity.ok(carManagementService.getInTransferCars());
    }

    @GetMapping("/auth/assignment/all")
    public ResponseEntity<CarReqRes> getAllAssignmentHistories() {
        return ResponseEntity.ok(carManagementService.getAllAssignmentHistories());
    }

    @GetMapping("/auth/assignments/pending")
    public ResponseEntity<CarReqRes> getPendingCars() {
        return ResponseEntity.ok(carManagementService.getPendingRequests());
    }

    @GetMapping("/auth/assignments/pending-and-semipending")
    public ResponseEntity<CarReqRes> getPendingAndSemiPendingCars() {
        return ResponseEntity.ok(carManagementService.getPendingAndSemiPendingRequests());
    }

    @PutMapping("/auth/car/assignments/update/{id}")
    public ResponseEntity<CarReqRes> updateAssignmentHistory(@PathVariable Long id, @RequestBody AssignmentRequest updateRequest) {
        return ResponseEntity.ok(carManagementService.updateAssignmentHistory(id, updateRequest));
    }

    @GetMapping("/auth/assignment/{id}")
    public ResponseEntity<CarReqRes> getAssignmentHistoryById(@PathVariable Long id) {
        return ResponseEntity.ok(carManagementService.getAssignmentHistoryById(id));
    }

    @DeleteMapping("/auth/assignment/delete/{id}")
    public ResponseEntity<CarReqRes> deleteAssignmentHistory(@PathVariable Long id) {
        return ResponseEntity.ok(carManagementService.deleteAssignmentHistory(id));
    }

    @PostMapping("/auth/assignment/{id}/license")
    public ResponseEntity<CarReqRes> uploadDriverLicense(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(carManagementService.uploadDriverLicense(id, file));
    }

    @GetMapping("/auth/assignment/{id}/license/{fileIndex}")
    public ResponseEntity<byte[]> getDriverLicenseFile(
            @PathVariable Long id,
            @PathVariable int fileIndex) {
        try {
            AssignmentHistory history = assignmentHistoryRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));

            if (history.getDriverLicenseFiles() == null || history.getDriverLicenseFiles().size() <= fileIndex) {
                throw new ResourceNotFoundException("License file not found");
            }

            String filePath = history.getDriverLicenseFiles().get(fileIndex);
            String contentType = history.getDriverLicenseFileTypes().get(fileIndex);
            String filename = history.getDriverLicenseFileNames().get(fileIndex);

            byte[] fileContent = carManagementService.getFile(filePath);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(fileContent);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/auth/assignment/status/{id}")
    public ResponseEntity<CarReqRes> updateAssignmentStatus(@PathVariable Long id, @RequestBody AssignmentRequest updateRequest) {
        return ResponseEntity.ok(carManagementService.updateAssignmentStatus(id, updateRequest));
    }
}