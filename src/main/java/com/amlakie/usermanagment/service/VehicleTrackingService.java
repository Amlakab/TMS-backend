package com.amlakie.usermanagment.service;

import com.amlakie.usermanagment.dto.GpsDataDTO;
import com.amlakie.usermanagment.dto.VehicleLocationResponseDTO;
import com.amlakie.usermanagment.entity.*;
import com.amlakie.usermanagment.exception.ResourceNotFoundException;
import com.amlakie.usermanagment.repository.OrganizationCarRepository;
import com.amlakie.usermanagment.repository.RentCarRepository;
import com.amlakie.usermanagment.repository.VehicleLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleTrackingService {
    private final VehicleLocationRepository vehicleLocationRepository;
    private final OrganizationCarRepository organizationCarRepository;
    private final RentCarRepository rentCarRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public VehicleLocationResponseDTO saveGpsData(GpsDataDTO gpsDataDTO) {
        // Try to find in organization cars first
        Optional<OrganizationCar> orgCar = organizationCarRepository.findByDeviceImei(gpsDataDTO.getImei());
        if (orgCar.isPresent()) {
            VehicleLocationResponseDTO response = saveVehicleLocation(orgCar.get(), "ORGANIZATION", gpsDataDTO);
            sendWebSocketUpdate(response);
            return response;
        }

        // If not found in organization cars, try rent cars
        Optional<RentCar> rentCar = rentCarRepository.findByDeviceImei(gpsDataDTO.getImei());
        if (rentCar.isPresent()) {
            VehicleLocationResponseDTO response = saveVehicleLocation(rentCar.get(), "RENT", gpsDataDTO);
            sendWebSocketUpdate(response);
            return response;
        }

        throw new ResourceNotFoundException("No vehicle found with IMEI: " + gpsDataDTO.getImei());
    }

    private VehicleLocationResponseDTO saveVehicleLocation(OrganizationCar car, String vehicleType, GpsDataDTO gpsData) {
        VehicleLocation location = new VehicleLocation();
        location.setVehicleId(car.getId());
        location.setVehicleType(vehicleType);
        location.setPlateNumber(car.getPlateNumber());
        location.setDriverName(car.getDriverName());
        location.setVehicleModel(car.getModel());
        location.setVehicleStatus(car.getStatus());
        location.setLatitude(gpsData.getLatitude());
        location.setLongitude(gpsData.getLongitude());
        location.setSpeed(gpsData.getSpeed());
        location.setHeading(gpsData.getHeading());
        location.setTimestamp(gpsData.getTimestamp() != null ? gpsData.getTimestamp() : LocalDateTime.now());
        location.setDeviceImei(gpsData.getImei());

        VehicleLocation savedLocation = vehicleLocationRepository.save(location);
        return mapToResponseDTO(savedLocation);
    }

    private VehicleLocationResponseDTO saveVehicleLocation(RentCar car, String vehicleType, GpsDataDTO gpsData) {
        VehicleLocation location = new VehicleLocation();
        location.setVehicleId(car.getId());
        location.setVehicleType(vehicleType);
        location.setPlateNumber(car.getPlateNumber());
        location.setDriverName(car.getDriverName());
        location.setVehicleModel(car.getModel());
        location.setVehicleStatus(car.getStatus());
        location.setLatitude(gpsData.getLatitude());
        location.setLongitude(gpsData.getLongitude());
        location.setSpeed(gpsData.getSpeed());
        location.setHeading(gpsData.getHeading());
        location.setTimestamp(gpsData.getTimestamp() != null ? gpsData.getTimestamp() : LocalDateTime.now());
        location.setDeviceImei(gpsData.getImei());

        VehicleLocation savedLocation = vehicleLocationRepository.save(location);
        return mapToResponseDTO(savedLocation);
    }

    private void sendWebSocketUpdate(VehicleLocationResponseDTO update) {
        try {
            messagingTemplate.convertAndSend("/topic/vehicle-updates", update);
        } catch (Exception e) {
            // Log the error but don't fail the operation
            System.err.println("Failed to send WebSocket update: " + e.getMessage());
        }
    }

    public List<VehicleLocationResponseDTO> getAllLatestLocations() {
        return vehicleLocationRepository.findAllLatestLocations().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<VehicleLocationResponseDTO> getLatestLocationsByType(String vehicleType) {
        return vehicleLocationRepository.findLatestLocationsByType(vehicleType).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public VehicleLocationResponseDTO getLatestLocationByImei(String imei) {
        VehicleLocation location = vehicleLocationRepository.findLatestByImei(imei);
        if (location == null) {
            throw new ResourceNotFoundException("No location data found for device IMEI: " + imei);
        }
        return mapToResponseDTO(location);
    }

    public List<VehicleLocationResponseDTO> getLocationHistory(Long vehicleId, String vehicleType) {
        return vehicleLocationRepository.findByVehicle(vehicleId, vehicleType).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<VehicleLocationResponseDTO> getLocationHistoryBetween(
            Long vehicleId, String vehicleType, LocalDateTime start, LocalDateTime end) {
        return vehicleLocationRepository.findByVehicleAndTimestampBetween(
                        vehicleId, vehicleType, start, end).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private VehicleLocationResponseDTO mapToResponseDTO(VehicleLocation location) {
        VehicleLocationResponseDTO dto = new VehicleLocationResponseDTO();
        dto.setId(location.getId());
        dto.setVehicleId(location.getVehicleId());
        dto.setVehicleType(location.getVehicleType());
        dto.setPlateNumber(location.getPlateNumber());
        dto.setDriverName(location.getDriverName());
        dto.setVehicleModel(location.getVehicleModel());
        dto.setVehicleStatus(location.getVehicleStatus());
        dto.setLatitude(location.getLatitude());
        dto.setLongitude(location.getLongitude());
        dto.setSpeed(location.getSpeed());
        dto.setHeading(location.getHeading());
        dto.setTimestamp(location.getTimestamp());
        dto.setDeviceImei(location.getDeviceImei());
        return dto;
    }
}