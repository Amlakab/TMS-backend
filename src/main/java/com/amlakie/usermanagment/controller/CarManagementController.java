package com.amlakie.usermanagment.controller;

import com.amlakie.usermanagment.dto.*;
import com.amlakie.usermanagment.entity.AssignmentHistory;
import com.amlakie.usermanagment.repository.AssignmentHistoryRepository;
import com.amlakie.usermanagment.service.CarManagementService;
import com.amlakie.usermanagment.service.FileStorageService;
import com.amlakie.usermanagment.service.VehicleAcceptanceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
public class CarManagementController {

    @Autowired
    private CarManagementService carManagementService;

    @Autowired
    private VehicleAcceptanceService vehicleAcceptanceService;

    @Autowired
    private AssignmentHistoryRepository assignmentHistoryRepository;


    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Register a new user.
     *
     * @param registrationRequest The registration request containing user details.
     * @return A response indicating the result of the registration.
     */
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

        try {
            if (driverLicenseFile != null && !driverLicenseFile.isEmpty()) {
                request.setDriverLicenseFile(driverLicenseFile);
            }
            return ResponseEntity.ok(carManagementService.createAssignment(request));
        } catch (Exception e) {
            CarReqRes errorResponse = new CarReqRes();
            errorResponse.setCodStatus(500);
            errorResponse.setError(e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // Add this new endpoint for checking expiring licenses
    @GetMapping("/auth/licenses/expiring")
    public ResponseEntity<CarReqRes> getExpiringLicenses() {
        return ResponseEntity.ok(carManagementService.getExpiringLicenses());
    }

    @GetMapping("/auth/licenses/file/{assignmentId}")
    public ResponseEntity<byte[]> getLicenseFile(@PathVariable Long assignmentId) {
        try {
            AssignmentHistory assignment = assignmentHistoryRepository.findById(assignmentId)
                    .orElseThrow(() -> new RuntimeException("Assignment not found"));

            if (assignment.getDriverLicenseFilepath() == null) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = Paths.get(assignment.getDriverLicenseFilepath());
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            byte[] fileContent = Files.readAllBytes(filePath);
            String contentType = assignment.getDriverLicenseFileType();
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .header("Content-Type", contentType)
                    .header("Content-Disposition", "inline; filename=\"" + assignment.getDriverLicenseFilename() + "\"")
                    .body(fileContent);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/auth/car/approved")
    public ResponseEntity<CarReqRes> getApprovedCars() {
        return ResponseEntity.ok(carManagementService.getApprovedCars());
    }

    @GetMapping("/auth/car/in-transfer")
    public ResponseEntity<CarReqRes> getInTransferCars() {
        return ResponseEntity.ok(carManagementService.getInTransferCars());
    }


    // Assignment History Endpoints
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


    @PostMapping("/auth/vehicle-acceptance/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String filename = fileStorageService.storeFile(file);
            return ResponseEntity.ok(filename);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to upload file: " + e.getMessage());
        }
    }

    @PutMapping("/auth/assignment/status/{id}")
    public ResponseEntity<CarReqRes> updateAssinmentStatus(@PathVariable Long id, @RequestBody AssignmentRequest updateRequest) {
        return ResponseEntity.ok(carManagementService.updateAssignmentStatus(id, updateRequest));
    }


}