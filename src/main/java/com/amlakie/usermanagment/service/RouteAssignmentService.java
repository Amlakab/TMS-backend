package com.amlakie.usermanagment.service;

import com.amlakie.usermanagment.dto.route.AssignRouteRequestDTO;
import com.amlakie.usermanagment.dto.route.AssignedRouteResponseDTO;
import com.amlakie.usermanagment.dto.route.WaypointInputDTO;
import com.amlakie.usermanagment.dto.route.WaypointOutputDTO;
import com.amlakie.usermanagment.entity.*;
import com.amlakie.usermanagment.repository.AssignedRouteRepository;
import com.amlakie.usermanagment.repository.OrganizationCarRepository;
// Import RouteHistoryRepository if it's not already imported
import com.amlakie.usermanagment.repository.RouteHistoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RouteAssignmentService {

    private final AssignedRouteRepository assignedRouteRepository;
    private final OrganizationCarRepository organizationCarRepository;
    private final RouteHistoryRepository routeHistoryRepository; // Added for injection

    @Autowired
    public RouteAssignmentService(AssignedRouteRepository assignedRouteRepository,
                                  OrganizationCarRepository organizationCarRepository,
                                  RouteHistoryRepository routeHistoryRepository) { // Added routeHistoryRepository
        this.assignedRouteRepository = assignedRouteRepository;
        this.organizationCarRepository = organizationCarRepository;
        this.routeHistoryRepository = routeHistoryRepository; // Initialize it
    }

    @Transactional
    public AssignedRouteResponseDTO assignOrUpdateRoute(AssignRouteRequestDTO request) {
        OrganizationCar car = organizationCarRepository.findByPlateNumber(request.getPlateNumber())
                .orElseThrow(() -> new EntityNotFoundException("OrganizationCar not found with plate number: " + request.getPlateNumber()));

        AssignedRoute currentAssignedRoute = assignedRouteRepository.findByOrganizationCarId(car.getId())
                .orElse(null);

        // --- Create History Record if a route existed and is being changed ---
        if (currentAssignedRoute != null && (request.getWaypoints() == null || request.getWaypoints().isEmpty() || !isRouteEffectivelyTheSame(currentAssignedRoute, request.getWaypoints()))) {
            // Archive if current route exists AND
            // (new waypoints are empty/null OR new waypoints are different)
            archiveRoute(currentAssignedRoute);
        }
        // --- End History Record Creation ---

        AssignedRoute assignedRouteToSave;
        if (request.getWaypoints() == null || request.getWaypoints().isEmpty()) {
            // If new waypoints are empty, it means unassign the route
            if (currentAssignedRoute != null) {
                assignedRouteRepository.delete(currentAssignedRoute);
            }
            // Potentially return a specific DTO or null indicating unassignment
            // For now, let's assume we might want to return car details without a route
            // Or throw an exception if unassigning via this method isn't desired without explicit action
            // This part needs clarification based on desired behavior for empty waypoints.
            // For simplicity, if unassigning means deleting, we might not return a standard DTO.
            // Let's assume for now it means the car has no active route.
            // We could return a DTO with an empty waypoint list or null.
            // For now, let's just ensure no route is saved if waypoints are empty.
            // And if a current route existed, it's archived and deleted.
            return new AssignedRouteResponseDTO(null, car.getPlateNumber(), car.getId(), new ArrayList<>());
        } else {
            // Assign or update with new waypoints
            assignedRouteToSave = (currentAssignedRoute != null) ? currentAssignedRoute : new AssignedRoute();
            assignedRouteToSave.setOrganizationCar(car);

            assignedRouteToSave.clearWaypoints(); // Clear existing waypoints

            for (int i = 0; i < request.getWaypoints().size(); i++) {
                WaypointInputDTO waypointDTO = request.getWaypoints().get(i);
                RouteWaypoint routeWaypoint = new RouteWaypoint();
                routeWaypoint.setLatitude(waypointDTO.getLatitude());
                routeWaypoint.setLongitude(waypointDTO.getLongitude());
                routeWaypoint.setSequenceOrder(i);
                assignedRouteToSave.addWaypoint(routeWaypoint);
            }
            AssignedRoute savedRoute = assignedRouteRepository.save(assignedRouteToSave);
            return convertToDTO(savedRoute);
        }
    }

    private boolean isRouteEffectivelyTheSame(AssignedRoute currentRoute, List<WaypointInputDTO> newWaypointsDto) {
        if (currentRoute.getWaypoints() == null && (newWaypointsDto == null || newWaypointsDto.isEmpty())) {
            return true; // Both are effectively no route
        }
        if (currentRoute.getWaypoints() == null || newWaypointsDto == null) {
            return false; // One has a route, the other doesn't
        }
        if (currentRoute.getWaypoints().size() != newWaypointsDto.size()) {
            return false;
        }
        for (int i = 0; i < newWaypointsDto.size(); i++) {
            RouteWaypoint currentWp = currentRoute.getWaypoints().get(i);
            WaypointInputDTO newWpDto = newWaypointsDto.get(i);
            // Consider adding a tolerance for double comparison if exact match is too strict
            if (!currentWp.getLatitude().equals(newWpDto.getLatitude()) ||
                    !currentWp.getLongitude().equals(newWpDto.getLongitude())) {
                return false;
            }
        }
        return true;
    }


    private void archiveRoute(AssignedRoute routeToArchive) {
        // Ensure waypoints are loaded if they are LAZY fetched and not already loaded
        // This might require fetching the entity again with waypoints if they weren't loaded
        // For simplicity, assuming waypoints are loaded with currentAssignedRoute
        if (routeToArchive.getWaypoints().isEmpty() && routeToArchive.getId() != null) {
            // If waypoints are lazy and not loaded, fetch them.
            // This is a simplified check; a proper check would involve PersistenceUnitUtil.isLoaded
            // or fetching the entity with waypoints explicitly if needed.
            // For now, we assume they are loaded if currentAssignedRoute was fetched with waypoints.
        }


        RouteHistory history = new RouteHistory();
        history.setOrganizationCar(routeToArchive.getOrganizationCar());
        history.setOriginalAssignedRouteId(routeToArchive.getId());
        history.setAssignmentStartDate(routeToArchive.getAssignmentDate());
        history.setAssignmentEndDate(LocalDateTime.now());

        for (RouteWaypoint wp : routeToArchive.getWaypoints()) {
            RouteHistoryWaypoint historyWp = new RouteHistoryWaypoint();
            historyWp.setLatitude(wp.getLatitude());
            historyWp.setLongitude(wp.getLongitude());
            historyWp.setSequenceOrder(wp.getSequenceOrder());
            history.addWaypoint(historyWp);
        }
        routeHistoryRepository.save(history);
    }

    // REMOVE THE DUPLICATE assignOrUpdateRoute METHOD THAT WAS HERE

    @Transactional(readOnly = true)
    public AssignedRouteResponseDTO getAssignedRouteByPlateNumber(String plateNumber) {
        return assignedRouteRepository.findByOrganizationCarPlateNumberWithWaypoints(plateNumber)
                .map(this::convertToDTO)
                .orElseThrow(() -> new EntityNotFoundException("No assigned route found for car with plate number: " + plateNumber));
    }

    @Transactional(readOnly = true)
    public AssignedRouteResponseDTO getAssignedRouteById(Long id) {
        return assignedRouteRepository.findByIdWithWaypointsAndCar(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new EntityNotFoundException("AssignedRoute not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<AssignedRouteResponseDTO> getAllAssignedRoutes() {
        return assignedRouteRepository.findAllWithWaypointsAndCar().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private AssignedRouteResponseDTO convertToDTO(AssignedRoute route) {
        if (route == null) {
            return null;
        }
        List<WaypointOutputDTO> waypointDTOs = route.getWaypoints() == null ? new ArrayList<>() :
                route.getWaypoints().stream()
                        .map(wp -> new WaypointOutputDTO(wp.getId(), wp.getLatitude(), wp.getLongitude(), wp.getSequenceOrder()))
                        .collect(Collectors.toList());

        return new AssignedRouteResponseDTO(
                route.getId(),
                route.getOrganizationCar().getPlateNumber(),
                route.getOrganizationCar().getId(),
                waypointDTOs
        );
    }
}