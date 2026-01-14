package com.amlakie.usermanagment.controller;

import com.amlakie.usermanagment.dto.rentalMaintenance.CreateRentalMaintenanceRequestDTO;
import com.amlakie.usermanagment.dto.rentalMaintenance.RentalMaintenanceRequestDTO;
import com.amlakie.usermanagment.dto.rentalMaintenance.UserDTO;
import com.amlakie.usermanagment.service.RentalMaintenanceService;
import com.amlakie.usermanagment.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/auth/rental-maintenance")
public class RentalMaintenanceController {

    private final RentalMaintenanceService maintenanceService;
    private final UserService userService;

    @Autowired
    public RentalMaintenanceController(RentalMaintenanceService maintenanceService,
                                       UserService userService) {
        this.maintenanceService = maintenanceService;
        this.userService = userService;
    }

    @PostMapping
    //@PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<RentalMaintenanceRequestDTO> createRequest(
            @RequestBody CreateRentalMaintenanceRequestDTO dto) {
        return ResponseEntity.ok(maintenanceService.createRequest(dto));
    }

    @GetMapping
    public ResponseEntity<List<RentalMaintenanceRequestDTO>> getAllRequests() {
        return ResponseEntity.ok(maintenanceService.getAllRequests());
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('DISTRIBUTOR')")
    public ResponseEntity<List<RentalMaintenanceRequestDTO>> getPendingRequests() {
        return ResponseEntity.ok(maintenanceService.getPendingRequests());
    }

    @PatchMapping("/{id}/approve")
    //@PreAuthorize("hasRole('DISTRIBUTOR')")
    public ResponseEntity<RentalMaintenanceRequestDTO> approveRequest(@PathVariable Long id) {
        return ResponseEntity.ok(maintenanceService.approveRequest(id));
    }

    @PatchMapping("/{id}/return")
    //@PreAuthorize("hasRole('DISTRIBUTOR')")
    public ResponseEntity<RentalMaintenanceRequestDTO> acceptReturned(@PathVariable Long id) {
        return ResponseEntity.ok(maintenanceService.acceptRetured(id));
    }

    @PatchMapping("/{id}/complete")
    //@PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<RentalMaintenanceRequestDTO> completeRequest(
            @PathVariable Long id,
            @RequestBody LocalDateTime returnDate) {
        return ResponseEntity.ok(maintenanceService.completeRequest(id, returnDate));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @PostMapping("/switch-role/{role}")
    public ResponseEntity<String> switchRole(@PathVariable String role) {
        if ("DISTRIBUTOR".equals(role) || "DRIVER".equals(role)) {
            userService.setTestRole(role); // Updated method name
            return ResponseEntity.ok("Test role switched to " + role);
        }
        return ResponseEntity.badRequest().body("Invalid role. Use DISTRIBUTOR or DRIVER");
    }
}