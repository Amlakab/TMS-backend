package com.amlakie.usermanagment.controller;

import com.amlakie.usermanagment.dto.route.AssignRouteRequestDTO;
import com.amlakie.usermanagment.dto.route.AssignedRouteResponseDTO;
import com.amlakie.usermanagment.service.RouteAssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes") // Example base path
public class RouteAssignmentController {

    private final RouteAssignmentService routeAssignmentService;

    @Autowired
    public RouteAssignmentController(RouteAssignmentService routeAssignmentService) {
        this.routeAssignmentService = routeAssignmentService;
    }

    @PostMapping("/assign")
    public ResponseEntity<AssignedRouteResponseDTO> assignOrUpdateRoute(@RequestBody AssignRouteRequestDTO request) {
        AssignedRouteResponseDTO response = routeAssignmentService.assignOrUpdateRoute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/car/{plateNumber}")
    public ResponseEntity<AssignedRouteResponseDTO> getAssignedRouteByPlateNumber(@PathVariable String plateNumber) {
        AssignedRouteResponseDTO response = routeAssignmentService.getAssignedRouteByPlateNumber(plateNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssignedRouteResponseDTO> getAssignedRouteById(@PathVariable Long id) {
        AssignedRouteResponseDTO response = routeAssignmentService.getAssignedRouteById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<AssignedRouteResponseDTO>> getAllAssignedRoutes() {
        List<AssignedRouteResponseDTO> responses = routeAssignmentService.getAllAssignedRoutes();
        return ResponseEntity.ok(responses);
    }
}