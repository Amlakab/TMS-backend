package com.amlakie.usermanagment.service;

import com.amlakie.usermanagment.dto.GpsDataDTO;
import com.amlakie.usermanagment.dto.VehicleLocationResponseDTO;
import com.amlakie.usermanagment.entity.*;
import com.amlakie.usermanagment.exception.ResourceNotFoundException;
import com.amlakie.usermanagment.repository.OrganizationCarRepository;
import com.amlakie.usermanagment.repository.RentCarRepository;
import com.amlakie.usermanagment.repository.VehicleLocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleTrackingService {
    private final VehicleLocationRepository vehicleLocationRepository;
    private final OrganizationCarRepository organizationCarRepository;
    private final RentCarRepository rentCarRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public VehicleLocationResponseDTO processAndSaveLocation(GpsDataDTO gpsDataDTO) {
        try {
            log.info("📍 Processing location data - Plate: {}, IMEI: {}, Lat: {}, Lng: {}",
                    gpsDataDTO.getPlateNumber(), gpsDataDTO.getImei(),
                    gpsDataDTO.getLatitude(), gpsDataDTO.getLongitude());

            // Validate required fields
            if (gpsDataDTO.getLatitude() == null || gpsDataDTO.getLongitude() == null) {
                throw new IllegalArgumentException("Latitude and longitude are required");
            }

            // Ensure no null values
            if (gpsDataDTO.getSpeed() == null) {
                gpsDataDTO.setSpeed(0.0);
            }
            if (gpsDataDTO.getHeading() == null) {
                gpsDataDTO.setHeading(0.0);
            }
            if (gpsDataDTO.getTimestamp() == null) {
                gpsDataDTO.setTimestamp(LocalDateTime.now());
            }

            // Try to find vehicle by device IMEI first
            Optional<OrganizationCar> orgCarByImei = organizationCarRepository.findByDeviceImei(gpsDataDTO.getImei());
            if (orgCarByImei.isPresent()) {
                log.info("✅ Found organization vehicle by IMEI: {}", gpsDataDTO.getImei());
                return saveVehicleLocation(orgCarByImei.get(), "ORGANIZATION", gpsDataDTO);
            }

            Optional<RentCar> rentCarByImei = rentCarRepository.findByDeviceImei(gpsDataDTO.getImei());
            if (rentCarByImei.isPresent()) {
                log.info("✅ Found rent vehicle by IMEI: {}", gpsDataDTO.getImei());
                return saveVehicleLocation(rentCarByImei.get(), "RENT", gpsDataDTO);
            }

            // If not found by IMEI, try to find by plate number
            Optional<OrganizationCar> orgCarByPlate = organizationCarRepository.findByPlateNumber(gpsDataDTO.getPlateNumber());
            if (orgCarByPlate.isPresent()) {
                log.info("✅ Found organization vehicle by plate: {}", gpsDataDTO.getPlateNumber());
                return saveVehicleLocation(orgCarByPlate.get(), "ORGANIZATION", gpsDataDTO);
            }

            Optional<RentCar> rentCarByPlate = rentCarRepository.findByPlateNumber(gpsDataDTO.getPlateNumber());
            if (rentCarByPlate.isPresent()) {
                log.info("✅ Found rent vehicle by plate: {}", gpsDataDTO.getPlateNumber());
                return saveVehicleLocation(rentCarByPlate.get(), "RENT", gpsDataDTO);
            }

            // If vehicle not found in database, create a temporary vehicle location
            log.warn("⚠️ Vehicle not found with IMEI: {} or plate: {}. Creating temporary record.",
                    gpsDataDTO.getImei(), gpsDataDTO.getPlateNumber());
            return saveTemporaryVehicleLocation(gpsDataDTO);

        } catch (Exception e) {
            log.error("❌ Error processing location data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process location data: " + e.getMessage(), e);
        }
    }

    private VehicleLocationResponseDTO saveVehicleLocation(OrganizationCar car, String vehicleType, GpsDataDTO gpsData) {
        try {
            VehicleLocation location = new VehicleLocation();
            location.setVehicleId(car.getId());
            location.setVehicleType(vehicleType);
            location.setPlateNumber(car.getPlateNumber());
            location.setDriverName(car.getDriverName() != null ? car.getDriverName() : "Unknown Driver");
            location.setVehicleModel(car.getModel() != null ? car.getModel() : "Unknown Model");
            location.setVehicleStatus(car.getStatus() != null ? car.getStatus() : "UNKNOWN");
            location.setLatitude(gpsData.getLatitude());
            location.setLongitude(gpsData.getLongitude());
            location.setSpeed(gpsData.getSpeed());
            location.setHeading(gpsData.getHeading());
            location.setTimestamp(gpsData.getTimestamp());
            location.setDeviceImei(gpsData.getImei());

            VehicleLocation savedLocation = vehicleLocationRepository.save(location);
            log.info("💾 Saved organization vehicle location - ID: {}, Plate: {}",
                    savedLocation.getId(), savedLocation.getPlateNumber());

            VehicleLocationResponseDTO response = mapToResponseDTO(savedLocation);
            sendWebSocketUpdate(response);
            return response;

        } catch (Exception e) {
            log.error("❌ Error saving organization vehicle location: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save organization vehicle location", e);
        }
    }

    private VehicleLocationResponseDTO saveVehicleLocation(RentCar car, String vehicleType, GpsDataDTO gpsData) {
        try {
            VehicleLocation location = new VehicleLocation();
            location.setVehicleId(car.getId());
            location.setVehicleType(vehicleType);
            location.setPlateNumber(car.getPlateNumber());
            location.setDriverName(car.getDriverName() != null ? car.getDriverName() : "Unknown Driver");
            location.setVehicleModel(car.getModel() != null ? car.getModel() : "Unknown Model");
            location.setVehicleStatus(car.getStatus() != null ? car.getStatus() : "UNKNOWN");
            location.setLatitude(gpsData.getLatitude());
            location.setLongitude(gpsData.getLongitude());
            location.setSpeed(gpsData.getSpeed());
            location.setHeading(gpsData.getHeading());
            location.setTimestamp(gpsData.getTimestamp());
            location.setDeviceImei(gpsData.getImei());

            VehicleLocation savedLocation = vehicleLocationRepository.save(location);
            log.info("💾 Saved rent vehicle location - ID: {}, Plate: {}",
                    savedLocation.getId(), savedLocation.getPlateNumber());

            VehicleLocationResponseDTO response = mapToResponseDTO(savedLocation);
            sendWebSocketUpdate(response);
            return response;

        } catch (Exception e) {
            log.error("❌ Error saving rent vehicle location: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save rent vehicle location", e);
        }
    }

    private VehicleLocationResponseDTO saveTemporaryVehicleLocation(GpsDataDTO gpsData) {
        try {
            VehicleLocation location = new VehicleLocation();
            location.setVehicleId(0L); // Temporary ID
            location.setVehicleType("UNKNOWN");
            location.setPlateNumber(gpsData.getPlateNumber());
            location.setDriverName("Unknown Driver");
            location.setVehicleModel("Unknown Model");
            location.setVehicleStatus("TRACKING");
            location.setLatitude(gpsData.getLatitude());
            location.setLongitude(gpsData.getLongitude());
            location.setSpeed(gpsData.getSpeed());
            location.setHeading(gpsData.getHeading());
            location.setTimestamp(gpsData.getTimestamp());
            location.setDeviceImei(gpsData.getImei());

            VehicleLocation savedLocation = vehicleLocationRepository.save(location);
            log.info("💾 Saved temporary vehicle location - ID: {}, Plate: {}, IMEI: {}",
                    savedLocation.getId(), savedLocation.getPlateNumber(), savedLocation.getDeviceImei());

            VehicleLocationResponseDTO response = mapToResponseDTO(savedLocation);
            sendWebSocketUpdate(response);
            return response;

        } catch (Exception e) {
            log.error("❌ Error saving temporary vehicle location: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save temporary vehicle location", e);
        }
    }

    private void sendWebSocketUpdate(VehicleLocationResponseDTO update) {
        try {
            messagingTemplate.convertAndSend("/topic/vehicle-locations", update);
            log.info("📡 Sent WebSocket update for vehicle: {} (Plate: {})",
                    update.getVehicleId(), update.getPlateNumber());
        } catch (Exception e) {
            log.error("❌ Failed to send WebSocket update: {}", e.getMessage());
            // Don't throw exception, just log error - WebSocket failure shouldn't break the main flow
        }
    }

    public List<VehicleLocationResponseDTO> getAllLatestLocations() {
        try {
            return vehicleLocationRepository.findAllLatestLocations().stream()
                    .map(this::mapToResponseDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("❌ Error getting all latest locations: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get latest locations", e);
        }
    }

    public List<VehicleLocationResponseDTO> getLatestLocationsByType(String vehicleType) {
        try {
            return vehicleLocationRepository.findLatestLocationsByType(vehicleType).stream()
                    .map(this::mapToResponseDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("❌ Error getting latest locations by type {}: {}", vehicleType, e.getMessage(), e);
            throw new RuntimeException("Failed to get latest locations by type", e);
        }
    }

    public VehicleLocationResponseDTO getLatestLocationByImei(String imei) {
        try {
            VehicleLocation location = vehicleLocationRepository.findLatestByImei(imei);
            if (location == null) {
                throw new ResourceNotFoundException("No location data found for device IMEI: " + imei);
            }
            return mapToResponseDTO(location);
        } catch (ResourceNotFoundException e) {
            throw e; // Re-throw specific exception
        } catch (Exception e) {
            log.error("❌ Error getting latest location by IMEI {}: {}", imei, e.getMessage(), e);
            throw new RuntimeException("Failed to get latest location by IMEI", e);
        }
    }

    public List<VehicleLocationResponseDTO> getLocationHistory(Long vehicleId, String vehicleType) {
        try {
            return vehicleLocationRepository.findByVehicle(vehicleId, vehicleType).stream()
                    .map(this::mapToResponseDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("❌ Error getting location history for vehicle {} (type {}): {}",
                    vehicleId, vehicleType, e.getMessage(), e);
            throw new RuntimeException("Failed to get location history", e);
        }
    }

    public List<VehicleLocationResponseDTO> getLocationHistoryBetween(
            Long vehicleId, String vehicleType, LocalDateTime start, LocalDateTime end) {
        try {
            return vehicleLocationRepository.findByVehicleAndTimestampBetween(
                            vehicleId, vehicleType, start, end).stream()
                    .map(this::mapToResponseDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("❌ Error getting location history for vehicle {} (type {}) between {} and {}: {}",
                    vehicleId, vehicleType, start, end, e.getMessage(), e);
            throw new RuntimeException("Failed to get location history between dates", e);
        }
    }

    private VehicleLocationResponseDTO mapToResponseDTO(VehicleLocation location) {
        try {
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
            dto.setSpeed(location.getSpeed() != null ? location.getSpeed() : 0.0);
            dto.setHeading(location.getHeading() != null ? location.getHeading() : 0.0);
            dto.setTimestamp(location.getTimestamp());
            dto.setDeviceImei(location.getDeviceImei());
            return dto;
        } catch (Exception e) {
            log.error("❌ Error mapping VehicleLocation to DTO: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to map vehicle location to response DTO", e);
        }
    }
}