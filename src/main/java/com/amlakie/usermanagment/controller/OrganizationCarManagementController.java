package com.amlakie.usermanagment.controller;

import com.amlakie.usermanagment.dto.AssignRouteRequest;
import com.amlakie.usermanagment.dto.AssignedRouteDTO;
import com.amlakie.usermanagment.dto.OrganizationCarListRes;
import com.amlakie.usermanagment.dto.OrganizationCarReqRes;
import com.amlakie.usermanagment.service.OrganizationCarManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth/organization-car")
public class OrganizationCarManagementController {

    @Autowired
    private OrganizationCarManagementService organizationCarManagementService;

    @PostMapping("/register")
    public OrganizationCarReqRes registerOrganizationCar(@RequestBody OrganizationCarReqRes registrationRequest) {
        return organizationCarManagementService.registerOrganizationCar(registrationRequest);
    }

    @GetMapping("/all")
    public ResponseEntity<OrganizationCarReqRes> getAllOrganizationCars() {
        return ResponseEntity.ok(organizationCarManagementService.getAllOrganizationCars());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizationCarReqRes> getOrganizationCarById(@PathVariable Long id) {
        return ResponseEntity.ok(organizationCarManagementService.getOrganizationCarById(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<OrganizationCarReqRes> updateOrganizationCar(@PathVariable Long id, @RequestBody OrganizationCarReqRes updateRequest) {
        return ResponseEntity.ok(organizationCarManagementService.updateOrganizationCar(id, updateRequest));
    }
    @PutMapping("/status/{platenumber}")
    public ResponseEntity<OrganizationCarReqRes> changeStatus(@PathVariable String plateNumber, @RequestBody OrganizationCarReqRes updateRequest) {
        return ResponseEntity.ok(organizationCarManagementService.updateStatus(plateNumber, updateRequest));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<OrganizationCarReqRes> deleteOrganizationCar(@PathVariable Long id) {
        return ResponseEntity.ok(organizationCarManagementService.deleteOrganizationCar(id));
    }

    @GetMapping("/search")
    public ResponseEntity<OrganizationCarReqRes> searchOrganizationCars(@RequestParam String query) {
        return ResponseEntity.ok(organizationCarManagementService.searchOrganizationCars(query));
    }

    @GetMapping("/service-buses")
    public ResponseEntity<OrganizationCarListRes> getServiceBusesAndMiniBuses() {
        return ResponseEntity.ok(organizationCarManagementService.getInspectedAndReadyOrganizationCars());
    }
    @PostMapping("/assign-route")
    public ResponseEntity<?> assignRouteToBus(@RequestBody AssignRouteRequest request) {
        // Save the assignment in your database (implement this logic)
        organizationCarManagementService.assignRoute(request);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/assigned-routes")
    public ResponseEntity<List<AssignedRouteDTO>> getAssignedRoutes() {
        List<AssignedRouteDTO> routes = organizationCarManagementService.getAssignedRoutes();
        return ResponseEntity.ok(routes);
    }


}