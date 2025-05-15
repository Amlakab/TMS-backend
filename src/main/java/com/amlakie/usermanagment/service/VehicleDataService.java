package com.amlakie.usermanagment.service;

import com.amlakie.usermanagment.dto.vehicle.BackendPlateSuggestionDTO;
import com.amlakie.usermanagment.dto.vehicle.BackendVehicleDTO;
import com.amlakie.usermanagment.entity.Car;
import com.amlakie.usermanagment.entity.OrganizationCar;
import com.amlakie.usermanagment.entity.Vehicle;
import com.amlakie.usermanagment.exception.ResourceNotFoundException;
import com.amlakie.usermanagment.exception.ValidationException;
import com.amlakie.usermanagment.repository.CarRepository;
import com.amlakie.usermanagment.repository.OrganizationCarRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class VehicleDataService {

    private static final Logger log = LoggerFactory.getLogger(VehicleDataService.class);

    private final CarRepository carRepository;
    private final OrganizationCarRepository organizationCarRepository;

    public VehicleDataService(CarRepository carRepository, OrganizationCarRepository organizationCarRepository) {
        this.carRepository = carRepository;
        this.organizationCarRepository = organizationCarRepository;
    }

    // Renamed from getVehicleDetails for clarity
    @Transactional(readOnly = true)
    public Optional<BackendVehicleDTO> getVehicleDetails(String plateNumber, String vehicleType) {
        Vehicle vehicle;
        if ("CAR".equalsIgnoreCase(vehicleType)) {
            // Use orElse(null) to avoid immediate exception if you want to return Optional.empty() later
            vehicle = carRepository.findByPlateNumber(plateNumber).orElse(null);
        } else if ("ORGANIZATION_CAR".equalsIgnoreCase(vehicleType)) {
            vehicle = organizationCarRepository.findByPlateNumber(plateNumber).orElse(null);
        } else {
            log.warn("Invalid vehicle type provided for details: {}", vehicleType);
            throw new ValidationException("Invalid vehicle type provided: " + vehicleType);
        }

        if (vehicle == null) {
            log.info("Vehicle not found with plate {} and type {}", plateNumber, vehicleType);
            return Optional.empty(); // Explicitly return empty if not found
        }
        return Optional.of(mapToBackendVehicleDTO(vehicle));
    }

    // New service method to find by plate number only
    @Transactional(readOnly = true)
    public Optional<BackendVehicleDTO> getVehicleDetailsByPlate(String plateNumber) {
        // Try finding as a Car first
        Optional<Car> carOpt = carRepository.findByPlateNumber(plateNumber);
        if (carOpt.isPresent()) {
            log.info("Found Car with plate: {}", plateNumber);
            return Optional.of(mapToBackendVehicleDTO(carOpt.get()));
        }

        // If not found as Car, try as an OrganizationCar
        Optional<OrganizationCar> orgCarOpt = organizationCarRepository.findByPlateNumber(plateNumber);
        if (orgCarOpt.isPresent()) {
            log.info("Found OrganizationCar with plate: {}", plateNumber);
            return Optional.of(mapToBackendVehicleDTO(orgCarOpt.get()));
        }

        log.info("No vehicle (Car or OrganizationCar) found with plate: {}", plateNumber);
        return Optional.empty(); // Not found in either repository
    }


    @Transactional(readOnly = true)
    public List<BackendPlateSuggestionDTO> getPlateSuggestions(String query, String vehicleTypeFilter) {
        Stream<Vehicle> vehicleStream = Stream.empty();
        String searchQuery = StringUtils.hasText(query) ? query : ""; // Handle empty query for 'Containing'

        boolean searchCars = true;
        boolean searchOrgCars = true;

        if (StringUtils.hasText(vehicleTypeFilter)) {
            if ("CAR".equalsIgnoreCase(vehicleTypeFilter)) {
                searchOrgCars = false;
            } else if ("ORGANIZATION_CAR".equalsIgnoreCase(vehicleTypeFilter)) {
                searchCars = false;
            } else {
                log.warn("Invalid vehicleTypeFilter for suggestions: {}", vehicleTypeFilter);
                // Decide behavior: ignore filter, return empty, or throw exception
            }
        }

        if (searchCars) {
            vehicleStream = Stream.concat(vehicleStream, carRepository.findByPlateNumberContainingIgnoreCase(searchQuery).stream());
        }
        if (searchOrgCars) {
            vehicleStream = Stream.concat(vehicleStream, organizationCarRepository.findByPlateNumberContainingIgnoreCase(searchQuery).stream());
        }

        return vehicleStream
                .distinct()
                .map(this::mapToBackendPlateSuggestionDTO)
                .collect(Collectors.toList());
    }

    private BackendVehicleDTO mapToBackendVehicleDTO(Vehicle vehicle) {
        String type = (vehicle instanceof Car) ? "CAR" : "ORGANIZATION_CAR";
        // Ensure your Vehicle interface and its implementations (Car, OrganizationCar)
        // have getDriverName() and getKmPerLiter().
        // Handle potential nulls from these getters if necessary.
        String driverName = vehicle.getDriverName() != null ? vehicle.getDriverName() : "N/A";
        Double kmPerLiter = vehicle.getKmPerLiter() != null  ? vehicle.getKmPerLiter() : 0.0;


        return new BackendVehicleDTO(
                vehicle.getPlateNumber(),
                kmPerLiter,
                type
        );
    }

    private BackendPlateSuggestionDTO mapToBackendPlateSuggestionDTO(Vehicle vehicle) {
        String type = (vehicle instanceof Car) ? "CAR" : "ORGANIZATION_CAR";
        return new BackendPlateSuggestionDTO(vehicle.getPlateNumber(), type);
    }
}