package com.amlakie.usermanagment.controller;

import com.amlakie.usermanagment.dto.AssignmentRequest;
import com.amlakie.usermanagment.dto.CarReqRes;
import com.amlakie.usermanagment.dto.OrganizationCarReqRes;
import com.amlakie.usermanagment.service.CarManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class CarManagementController {

    @Autowired
    private CarManagementService carManagementService;

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
    public ResponseEntity<CarReqRes> deleteCar(@PathVariable Long id) { // Return ResponseEntity<CarReqRes>
        CarReqRes serviceResponse = carManagementService.deleteCar(id);

        if (serviceResponse.getCodStatus() == 200) {
            // For successful DELETE, 204 No Content is standard, but if frontend
            // needs the message, return 200 OK with the body. Choose one.
            // Option A: Standard 204 No Content (frontend might not get body)
            // return ResponseEntity.noContent().build();

            // Option B: 200 OK with body (if frontend needs the message)
            return ResponseEntity.ok(serviceResponse);

        } else {
            // For errors (404, 409, 500), return the CarReqRes object
            // Spring Boot will serialize this to JSON with the appropriate status code
            return ResponseEntity.status(serviceResponse.getCodStatus()).body(serviceResponse);
        }
    }

    @GetMapping("/auth/car/search")
    public ResponseEntity<CarReqRes> searchCars(@RequestParam String query) {
        return ResponseEntity.ok(carManagementService.searchCars(query));
    }
    @PutMapping("/auth/car/status/{plateNumber}")
    public ResponseEntity<CarReqRes> updateStatus(@PathVariable String plateNumber, @RequestBody CarReqRes updateRequest) {
        return ResponseEntity.ok(carManagementService.updateStatus(plateNumber, updateRequest));
    }

    // Add these new endpoints to CarManagementController
    @PostMapping("/auth/car/assign")
    public ResponseEntity<CarReqRes> createAssignment(@RequestBody AssignmentRequest assignmentRequest) {
        return ResponseEntity.ok(carManagementService.createAssignment(assignmentRequest));
    }

    @GetMapping("/auth/car/approved")
    public ResponseEntity<CarReqRes> getApprovedCars() {
        return ResponseEntity.ok(carManagementService.getApprovedCars());
    }
}
