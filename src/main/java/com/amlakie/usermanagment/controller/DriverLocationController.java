package com.amlakie.usermanagment.controller;

import com.amlakie.usermanagment.dto.DriverLocationDTO;
import com.amlakie.usermanagment.dto.DriverLocationReqRes;
import com.amlakie.usermanagment.service.DriverLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/driver-location")
public class DriverLocationController {

    @Autowired
    private DriverLocationService driverLocationService;

    // Register driver location
    @PostMapping("/register")
    public ResponseEntity<DriverLocationReqRes> registerDriverLocation(@RequestBody DriverLocationDTO registrationRequest) {
        DriverLocationReqRes response = driverLocationService.registerDriverLocation(registrationRequest);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // Get driver location by driver Email
    @GetMapping("/by-driver/{driverEmail}")
    public ResponseEntity<DriverLocationReqRes> getDriverLocationByDriverId(@PathVariable String driverEmail) {
        DriverLocationReqRes response = driverLocationService.getDriverLocationByDriverEmail(driverEmail);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // Update driver location
    @PutMapping("/update")
    public ResponseEntity<DriverLocationReqRes> updateDriverLocation(@RequestBody DriverLocationDTO updateRequest) {
        DriverLocationReqRes response = driverLocationService.updateDriverLocation(updateRequest);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}