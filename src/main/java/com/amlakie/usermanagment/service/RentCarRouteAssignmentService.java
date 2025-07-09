// Create this new file: D:/my git projects/TMS-backend/src/main/java/com/amlakie/usermanagment/service/RentCarRouteAssignmentService.java
package com.amlakie.usermanagment.service;

import com.amlakie.usermanagment.dto.route.AssignRouteRequestDTO;
import com.amlakie.usermanagment.dto.RentCarRouteDTO;
import com.amlakie.usermanagment.dto.route.WaypointInputDTO;
import com.amlakie.usermanagment.dto.route.WaypointOutputDTO;
import com.amlakie.usermanagment.entity.RentCar;
import com.amlakie.usermanagment.entity.RentCarRoute;
import com.amlakie.usermanagment.entity.RentCarRouteWaypoint;
import com.amlakie.usermanagment.repository.RentCarRepository;
import com.amlakie.usermanagment.repository.RentCarRouteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RentCarRouteAssignmentService {

    private final RentCarRouteRepository rentCarRouteRepository;
    private final RentCarRepository rentCarRepository;

    @Autowired
    public RentCarRouteAssignmentService(RentCarRouteRepository rentCarRouteRepository, RentCarRepository rentCarRepository) {
        this.rentCarRouteRepository = rentCarRouteRepository;
        this.rentCarRepository = rentCarRepository;
    }
// Add these methods inside your RentCarRouteAssignmentService.java class

    @Transactional(readOnly = true)
    public RentCarRouteDTO getAssignedRouteByPlateNumber(String plateNumber) {
        // Use the efficient repository method and map the result to a DTO
        return rentCarRouteRepository.findByRentCarPlateNumberWithWaypoints(plateNumber)
                .map(this::convertToDTO)
                .orElseThrow(() -> new EntityNotFoundException("No assigned route found for RentCar with plate number: " + plateNumber));
    }

    @Transactional(readOnly = true)
    public RentCarRouteDTO getAssignedRouteById(Long id) {
        return rentCarRouteRepository.findByIdWithWaypointsAndCar(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new EntityNotFoundException("RentCarRoute not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<RentCarRouteDTO> getAllAssignedRoutes() {
        return rentCarRouteRepository.findAllWithWaypointsAndCar().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    @Transactional
    public RentCarRouteDTO assignOrUpdateRoute(AssignRouteRequestDTO request) {
        // Find the specific RentCar
        RentCar vehicle = rentCarRepository.findByPlateNumber(request.getPlateNumber())
                .orElseThrow(() -> new EntityNotFoundException("RentCar not found with plate number: " + request.getPlateNumber()));

        // Find an existing route for this specific car
        RentCarRoute currentAssignedRoute = rentCarRouteRepository.findByRentCarId(vehicle.getId())
                .orElse(null);

        // If the new route is empty, delete the old one
        if (request.getWaypoints() == null || request.getWaypoints().isEmpty()) {
            if (currentAssignedRoute != null) {
                rentCarRouteRepository.delete(currentAssignedRoute);
            }
            return new RentCarRouteDTO(null, vehicle.getPlateNumber(), vehicle.getId(), new ArrayList<>());
        }

        // Create or update the route
        RentCarRoute routeToSave = (currentAssignedRoute != null) ? currentAssignedRoute : new RentCarRoute();
        routeToSave.setRentCar(vehicle);
        routeToSave.clearWaypoints();

        for (int i = 0; i < request.getWaypoints().size(); i++) {
            WaypointInputDTO waypointDTO = request.getWaypoints().get(i);
            RentCarRouteWaypoint waypoint = new RentCarRouteWaypoint();
            waypoint.setLatitude(waypointDTO.getLatitude());
            waypoint.setLongitude(waypointDTO.getLongitude());
            waypoint.setSequenceOrder(i);
            routeToSave.addWaypoint(waypoint);
        }

        RentCarRoute savedRoute = rentCarRouteRepository.save(routeToSave);
        return convertToDTO(savedRoute);
    }

    private RentCarRouteDTO convertToDTO(RentCarRoute route) {
        if (route == null) return null;
        return new RentCarRouteDTO(
                route.getId(),
                route.getRentCar().getPlateNumber(),
                route.getRentCar().getId(),
                route.getWaypoints().stream()
                        .map(wp -> new WaypointOutputDTO(wp.getId(), wp.getLatitude(), wp.getLongitude(), wp.getSequenceOrder()))
                        .collect(Collectors.toList())
        );
    }
}