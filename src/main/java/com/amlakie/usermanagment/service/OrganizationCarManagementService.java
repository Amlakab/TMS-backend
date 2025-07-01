package com.amlakie.usermanagment.service;

import com.amlakie.usermanagment.dto.AssignedRouteDTO;
import com.amlakie.usermanagment.dto.OrganizationCarListRes;
import com.amlakie.usermanagment.dto.OrganizationCarReqRes;
import com.amlakie.usermanagment.entity.OrganizationCar;
import com.amlakie.usermanagment.dto.AssignRouteRequest;
import com.amlakie.usermanagment.entity.Vehicle;
import com.amlakie.usermanagment.exception.ResourceNotFoundException;
import com.amlakie.usermanagment.repository.OrganizationCarRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrganizationCarManagementService {

    @Autowired
    private OrganizationCarRepository organizationCarRepository;


    public OrganizationCarReqRes registerOrganizationCar(OrganizationCarReqRes registrationRequest) {
        OrganizationCarReqRes response = new OrganizationCarReqRes();
        try {
            OrganizationCar organizationCar = new OrganizationCar();
            organizationCar.setPlateNumber(registrationRequest.getPlateNumber());
            organizationCar.setOwnerName(registrationRequest.getOwnerName());
            organizationCar.setOwnerPhone(registrationRequest.getOwnerPhone());
            organizationCar.setModel(registrationRequest.getModel());
            organizationCar.setCarType(registrationRequest.getCarType());
            organizationCar.setManufactureYear(registrationRequest.getManufactureYear());
            organizationCar.setMotorCapacity(registrationRequest.getMotorCapacity());
            organizationCar.setKmPerLiter(Float.parseFloat(registrationRequest.getKmPerLiter()));
            organizationCar.setTotalKm(registrationRequest.getTotalKm());
            organizationCar.setFuelType(registrationRequest.getFuelType());
            organizationCar.setStatus(registrationRequest.getStatus());
            organizationCar.setParkingLocation(registrationRequest.getParkingLocation());
            organizationCar.setDriverName(registrationRequest.getDriverName());
            organizationCar.setDriverAttributes(registrationRequest.getDriverAttributes());
            organizationCar.setDriverAddress(registrationRequest.getDriverAddress());
            organizationCar.setLoadCapacity(Integer.valueOf(registrationRequest.getLoadCapacity()));

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            organizationCar.setCreatedBy(authentication.getName());

            OrganizationCar savedCar = organizationCarRepository.save(organizationCar);
            response.setOrganizationCar(savedCar);
            response.setMessage("Organization Car Registered Successfully");
            response.setCodStatus(200);

        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public OrganizationCarReqRes getAllOrganizationCars() {
        OrganizationCarReqRes response = new OrganizationCarReqRes();
        try {
            List<OrganizationCar> cars = organizationCarRepository.findAll();
            response.setOrganizationCarList(cars);
            response.setCodStatus(200);
            response.setMessage("All organization cars retrieved successfully");
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public OrganizationCarReqRes getOrganizationCarById(Long id) {
        OrganizationCarReqRes response = new OrganizationCarReqRes();
        try {
            Optional<OrganizationCar> car = organizationCarRepository.findById(id);
            if (car.isPresent()) {
                response.setOrganizationCar(car.get());
                response.setCodStatus(200);
                response.setMessage("Organization car retrieved successfully");
            } else {
                response.setCodStatus(404);
                response.setMessage("Organization car not found");
            }
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public OrganizationCarReqRes updateOrganizationCar(Long id, OrganizationCarReqRes updateRequest) {
        OrganizationCarReqRes response = new OrganizationCarReqRes();
        try {
            Optional<OrganizationCar> carOptional = organizationCarRepository.findById(id);
            if (carOptional.isPresent()) {
                OrganizationCar existingCar = carOptional.get();
                existingCar.setPlateNumber(updateRequest.getPlateNumber());
                existingCar.setOwnerName(updateRequest.getOwnerName());
                existingCar.setOwnerPhone(updateRequest.getOwnerPhone());
                existingCar.setModel(updateRequest.getModel());
                existingCar.setCarType(updateRequest.getCarType());
                existingCar.setManufactureYear(updateRequest.getManufactureYear());
                existingCar.setMotorCapacity(updateRequest.getMotorCapacity());
                existingCar.setKmPerLiter(Float.parseFloat(updateRequest.getKmPerLiter()));
                existingCar.setTotalKm(updateRequest.getTotalKm());
                existingCar.setFuelType(updateRequest.getFuelType());
                existingCar.setStatus(updateRequest.getStatus());
                existingCar.setParkingLocation(updateRequest.getParkingLocation());
                existingCar.setDriverName(updateRequest.getDriverName());
                existingCar.setDriverAttributes(updateRequest.getDriverAttributes());
                existingCar.setDriverAddress(updateRequest.getDriverAddress());
                existingCar.setLoadCapacity(Integer.valueOf(updateRequest.getLoadCapacity()));

                OrganizationCar updatedCar = organizationCarRepository.save(existingCar);
                response.setOrganizationCar(updatedCar);
                response.setCodStatus(200);
                response.setMessage("Organization car updated successfully");
            } else {
                response.setCodStatus(404);
                response.setMessage("Organization car not found");
            }
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public OrganizationCarReqRes deleteOrganizationCar(Long id) {
        OrganizationCarReqRes response = new OrganizationCarReqRes();
        try {
            if (organizationCarRepository.existsById(id)) {
                organizationCarRepository.deleteById(id);
                response.setCodStatus(200);
                response.setMessage("Organization car deleted successfully");
            } else {
                response.setCodStatus(404);
                response.setMessage("Organization car not found");
            }
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public OrganizationCarReqRes searchOrganizationCars(String query) {
        OrganizationCarReqRes response = new OrganizationCarReqRes();
        try {
            List<OrganizationCar> cars = organizationCarRepository
                    .findByPlateNumberContainingOrOwnerNameContainingOrModelContainingOrDriverNameContaining(
                            query, query, query, query);
            response.setOrganizationCarList(cars);
            response.setCodStatus(200);
            response.setMessage("Search results retrieved successfully");
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }

    public OrganizationCarReqRes updateStatus(String plateNumber, OrganizationCarReqRes updateRequest) {
        OrganizationCarReqRes response = new OrganizationCarReqRes();
        try {
            Optional<OrganizationCar> carOptional = organizationCarRepository.findByPlateNumber(plateNumber);
            if (carOptional.isPresent()) {
                OrganizationCar existingCar = carOptional.get();
                existingCar.setStatus(updateRequest.getStatus());
                OrganizationCar updatedCar = organizationCarRepository.save(existingCar);
                response.setOrganizationCar(updatedCar);
                response.setCodStatus(200);
                response.setMessage("Organization car Status Updated successfully");
            } else {
                response.setCodStatus(404);
                response.setMessage("Organization car not found");
            }
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError(e.getMessage());
        }
        return response;
    }
    // Java
    // In OrganizationCarManagementService.java
    // In OrganizationCarManagementService.java
    // Java
    public OrganizationCarListRes getInspectedAndReadyOrganizationCars() {
        List<OrganizationCar> all = organizationCarRepository.findAll();
        if (all == null) {
            all = List.of();
        }
        List<OrganizationCarReqRes> filtered = all.stream()
                .filter(car ->
                        car.getStatus() != null &&
                                car.getStatus().equalsIgnoreCase("InspectedAndReady")
                )
                .map(car -> {
                    OrganizationCarReqRes dto = new OrganizationCarReqRes();
                    dto.setOrganizationCar(car);
                    return dto;
                })
                .collect(Collectors.toList());
        return new OrganizationCarListRes(filtered);
    }

    public void assignRoute(AssignRouteRequest request) {
        // Find the car by plate number (or ID)
        Optional<OrganizationCar> car = organizationCarRepository.findByPlateNumber(request.plateNumber);
        if (car == null) {
            throw new RuntimeException("Car not found");
        }
        // Save the destination (you may want to create a Route entity or just update fields)
        Optional<OrganizationCar> carOptional = organizationCarRepository.findByPlateNumber(request.plateNumber);
        if (carOptional.isEmpty()) {
            throw new RuntimeException("Car not found");
        }
        OrganizationCar Car = carOptional.get();
        Car.setDestinationLat(request.latitude);
        Car.setDestinationLng(request.longitude);
        organizationCarRepository.save(Car);
    }
    public List<AssignedRouteDTO> getAssignedRoutes() {
        return organizationCarRepository.findAll().stream()
                .filter(car -> car.getDestinationLat() != null && car.getDestinationLng() != null)
                .map(car -> {
                    AssignedRouteDTO dto = new AssignedRouteDTO();
                    dto.plateNumber = car.getPlateNumber();
                    dto.destinationLat = car.getDestinationLat();
                    dto.destinationLng = car.getDestinationLng();
                    return dto;
                })
                .collect(Collectors.toList());
    }
}