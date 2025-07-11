package com.amlakie.usermanagment.controller;

import com.amlakie.usermanagment.dto.route.AssignRouteRequestDTO;
import com.amlakie.usermanagment.dto.RentCarRouteDTO;
import com.amlakie.usermanagment.service.RentCarRouteAssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth/rent-car-routes") // A new, separate endpoint
public class RentCarRouteAssignmentController {

    private final RentCarRouteAssignmentService rentCarRouteAssignmentService;

    @Autowired
    public RentCarRouteAssignmentController(RentCarRouteAssignmentService rentCarRouteAssignmentService) {
        this.rentCarRouteAssignmentService = rentCarRouteAssignmentService;
    }

    @PostMapping("/assign")
    public ResponseEntity<RentCarRouteDTO> assignRoute(@RequestBody AssignRouteRequestDTO request) {
        RentCarRouteDTO response = rentCarRouteAssignmentService.assignOrUpdateRoute(request);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/car/{plateNumber}")
    public ResponseEntity<RentCarRouteDTO> getAssignedRouteByPlateNumber(@PathVariable String plateNumber) {
        RentCarRouteDTO response = rentCarRouteAssignmentService.getAssignedRouteByPlateNumber(plateNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RentCarRouteDTO> getAssignedRouteById(@PathVariable Long id) {
        RentCarRouteDTO response = rentCarRouteAssignmentService.getAssignedRouteById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<RentCarRouteDTO>> getAllAssignedRoutes() {
        List<RentCarRouteDTO> responses = rentCarRouteAssignmentService.getAllAssignedRoutes();
        return ResponseEntity.ok(responses);
    }
}