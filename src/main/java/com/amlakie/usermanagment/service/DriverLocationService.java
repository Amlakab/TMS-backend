package com.amlakie.usermanagment.service;

import com.amlakie.usermanagment.dto.DriverLocationDTO;
import com.amlakie.usermanagment.dto.DriverLocationReqRes;
import com.amlakie.usermanagment.entity.DriverLocation;
import com.amlakie.usermanagment.repository.DriverLocationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DriverLocationService {

    @Autowired
    private DriverLocationRepo driverLocationRepo;

    public DriverLocationReqRes registerDriverLocation(DriverLocationDTO registrationRequest) {
        DriverLocationReqRes response = new DriverLocationReqRes();
        try {
            // Check if driver already registered
            if (driverLocationRepo.existsByDriverEmail(registrationRequest.getDriverEmail())) {
                response.setStatus(400);
                response.setMessage("Driver already registered");
                return response;
            }

            // Check if plate number already exists
            if (driverLocationRepo.findByPlateNumber(registrationRequest.getPlateNumber()).isPresent()) {
                response.setStatus(400);
                response.setMessage("Plate number already registered");
                return response;
            }

            // Check if IMEI already exists
            if (driverLocationRepo.findByImei(registrationRequest.getImei()).isPresent()) {
                response.setStatus(400);
                response.setMessage("IMEI already registered");
                return response;
            }

            // Create new driver location
            DriverLocation driverLocation = new DriverLocation(
                    registrationRequest.getDriverEmail(),
                    registrationRequest.getDriverName(),
                    registrationRequest.getPlateNumber().toUpperCase(),
                    registrationRequest.getImei()
            );

            DriverLocation savedLocation = driverLocationRepo.save(driverLocation);

            // Convert to DTO for response
            DriverLocationDTO savedLocationDTO = convertToDTO(savedLocation);

            response.setStatus(200);
            response.setMessage("Driver location registered successfully");
            response.setDriverLocation(savedLocationDTO);
            return response;

        } catch (Exception e) {
            response.setStatus(500);
            response.setMessage("Registration failed: " + e.getMessage());
            return response;
        }
    }

    public DriverLocationReqRes getDriverLocationByDriverEmail(String driverEmail) {
        DriverLocationReqRes response = new DriverLocationReqRes();
        try {
            Optional<DriverLocation> driverLocation = driverLocationRepo.findByDriverEmail(driverEmail);
            if (driverLocation.isPresent()) {
                DriverLocationDTO driverLocationDTO = convertToDTO(driverLocation.get());
                response.setStatus(200);
                response.setDriverLocation(driverLocationDTO);
                response.setMessage("Driver location found");
            } else {
                response.setStatus(404);
                response.setMessage("Driver location not found");
            }
            return response;
        } catch (Exception e) {
            response.setStatus(500);
            response.setMessage("Error: " + e.getMessage());
            return response;
        }
    }

    public DriverLocationReqRes updateDriverLocation(DriverLocationDTO updateRequest) {
        DriverLocationReqRes response = new DriverLocationReqRes();
        try {
            Optional<DriverLocation> existingLocation = driverLocationRepo.findByDriverEmail(updateRequest.getDriverEmail());
            if (existingLocation.isPresent()) {
                DriverLocation driverLocation = existingLocation.get();

                // Update fields
                if (updateRequest.getPlateNumber() != null) {
                    driverLocation.setPlateNumber(updateRequest.getPlateNumber().toUpperCase());
                }
                if (updateRequest.getImei() != null) {
                    driverLocation.setImei(updateRequest.getImei());
                }

                DriverLocation updatedLocation = driverLocationRepo.save(driverLocation);
                DriverLocationDTO updatedLocationDTO = convertToDTO(updatedLocation);

                response.setStatus(200);
                response.setDriverLocation(updatedLocationDTO);
                response.setMessage("Driver location updated successfully");
            } else {
                response.setStatus(404);
                response.setMessage("Driver location not found");
            }
            return response;
        } catch (Exception e) {
            response.setStatus(500);
            response.setMessage("Update failed: " + e.getMessage());
            return response;
        }
    }

    private DriverLocationDTO convertToDTO(DriverLocation driverLocation) {
        return new DriverLocationDTO(
                driverLocation.getId(),
                driverLocation.getDriverEmail(),
                driverLocation.getDriverName(),
                driverLocation.getPlateNumber(),
                driverLocation.getImei()
        );
    }
}