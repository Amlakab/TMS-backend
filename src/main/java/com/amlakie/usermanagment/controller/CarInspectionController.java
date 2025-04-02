package com.amlakie.usermanagment.controller;

import com.amlakie.usermanagment.dto.CarInspectionReqRes;
import com.amlakie.usermanagment.dto.CarInspectionListResponse;
import com.amlakie.usermanagment.service.CarInspectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inspections")
public class CarInspectionController {

    @Autowired
    private CarInspectionService inspectionService;

    @PostMapping("/create")
    public ResponseEntity<CarInspectionReqRes> createInspection(@RequestBody CarInspectionReqRes request) {
        return ResponseEntity.ok(inspectionService.createInspection(request));
    }

    @GetMapping("/get-all")
    public ResponseEntity<CarInspectionListResponse> getAllInspections() {
        return ResponseEntity.ok(inspectionService.getAllInspections());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<CarInspectionReqRes> getInspectionById(@PathVariable Long id) {
        return ResponseEntity.ok(inspectionService.getInspectionById(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<CarInspectionReqRes> updateInspection(
            @PathVariable Long id,
            @RequestBody CarInspectionReqRes request) {
        return ResponseEntity.ok(inspectionService.updateInspection(id, request));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<CarInspectionReqRes> deleteInspection(@PathVariable Long id) {
        return ResponseEntity.ok(inspectionService.deleteInspection(id));
    }

    @GetMapping("/by-plate/{plateNumber}")
    public ResponseEntity<CarInspectionListResponse> getInspectionsByPlateNumber(
            @PathVariable String plateNumber) {
        return ResponseEntity.ok(inspectionService.getInspectionsByPlateNumber(plateNumber));
    }
}