package com.amlakie.usermanagment.controller;

import com.amlakie.usermanagment.dto.TravelRequestReqRes;
import com.amlakie.usermanagment.service.TravelRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/travel-requests")
public class TravelRequestController {

    @Autowired
    private TravelRequestService travelRequestService;

    @PostMapping("/create")
    public ResponseEntity<TravelRequestReqRes> createTravelRequest(@RequestBody TravelRequestReqRes request) {
        return ResponseEntity.ok(travelRequestService.createTravelRequest(request));
    }

    @GetMapping("/test")
    public String publicTest() {
        return "This is a public endpoint";
    }

    @GetMapping("/all")
    public ResponseEntity<TravelRequestReqRes> getAllTravelRequests() {
        return ResponseEntity.ok(travelRequestService.getAllTravelRequests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TravelRequestReqRes> getTravelRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(travelRequestService.getTravelRequestById(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<TravelRequestReqRes> updateTravelRequest(
            @PathVariable Long id, @RequestBody TravelRequestReqRes updateRequest) {
        return ResponseEntity.ok(travelRequestService.updateTravelRequest(id, updateRequest));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<TravelRequestReqRes> deleteTravelRequest(@PathVariable Long id) {
        return ResponseEntity.ok(travelRequestService.deleteTravelRequest(id));
    }

    @GetMapping("/search")
    public ResponseEntity<TravelRequestReqRes> searchTravelRequests(@RequestParam String query) {
        return ResponseEntity.ok(travelRequestService.searchTravelRequests(query));
    }

    @GetMapping("/department/{department}")
    public ResponseEntity<TravelRequestReqRes> getRequestsByDepartment(@PathVariable String department) {
        return ResponseEntity.ok(travelRequestService.getRequestsByDepartment(department));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<TravelRequestReqRes> getRequestsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(travelRequestService.getRequestsByStatus(status));
    }
}