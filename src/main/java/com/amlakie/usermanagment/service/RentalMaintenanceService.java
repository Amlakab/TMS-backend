package com.amlakie.usermanagment.service;

import com.amlakie.usermanagment.dto.rentalMaintenance.CreateRentalMaintenanceRequestDTO;
import com.amlakie.usermanagment.dto.rentalMaintenance.RentalMaintenanceRequestDTO;
import com.amlakie.usermanagment.entity.*;
import com.amlakie.usermanagment.exception.ResourceNotFoundException;
import com.amlakie.usermanagment.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RentalMaintenanceService {

    @Autowired
    private RentalMaintenanceRequestRepository maintenanceRepo;

    @Autowired
    private OrganizationCarRepository orgCarRepo;

    @Autowired
    private CarRepository carRepo;

    @Transactional
    public RentalMaintenanceRequestDTO createRequest(CreateRentalMaintenanceRequestDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        RentalMaintenanceRequest request = new RentalMaintenanceRequest();

        if (dto.getRentalCarId() != null) {
            OrganizationCar rentalCar = orgCarRepo.findById(dto.getRentalCarId())
                    .orElseThrow(() -> new ResourceNotFoundException("Organization car not found"));
            request.setRentalCar(rentalCar);
            request.setPlateNumber(rentalCar.getPlateNumber());
            request.setDriverName(rentalCar.getDriverName());
            request.setCarType(rentalCar.getCarType());
        } else if (dto.getCarId() != null) {
            Car car = carRepo.findById(dto.getCarId())
                    .orElseThrow(() -> new ResourceNotFoundException("Car not found"));
            request.setCar(car);
            request.setPlateNumber(car.getPlateNumber());
            request.setDriverName(car.getDriverName() != null ? car.getDriverName() : "No driver");
            request.setCarType(car.getCarType());
        } else {
            throw new IllegalArgumentException("Either rentalCarId or carId must be provided");
        }
        if(dto.getReason()!=null){
            request.setReason(dto.getReason());
        }
        request.setRequesterName(dto.getRequesterName());
        request.setRequesterPhone(dto.getRequesterPhone());
        request.setRequestDate(LocalDateTime.now());
        request.setServiceDate(dto.getServiceDate());
        request.setRequestType(dto.getRequestType());
        request.setStatus("PENDING");
        request.setCreatedBy(auth.getName());

        return convertToDTO(maintenanceRepo.save(request));
    }

    @Transactional
    public RentalMaintenanceRequestDTO approveRequest(Long id) {
        RentalMaintenanceRequest request = maintenanceRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        request.setStatus("APPROVED");

        // Update vehicle status
        if (request.getRentalCar() != null) {
            request.getRentalCar().setStatus("InMaintenance");
            orgCarRepo.save(request.getRentalCar());
        } else if (request.getCar() != null) {
            request.getCar().setStatus("InMaintenance");
            carRepo.save(request.getCar());
        }

        return convertToDTO(maintenanceRepo.save(request));
    }

    @Transactional
    public RentalMaintenanceRequestDTO acceptRetured(Long id) {
        RentalMaintenanceRequest request = maintenanceRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        request.setStatus("COMPLETED");

        // Update vehicle status
        if (request.getRentalCar() != null) {
            request.getRentalCar().setStatus("InspectedAndReady");
            orgCarRepo.save(request.getRentalCar());
        } else if (request.getCar() != null) {
            request.getCar().setStatus("InspectedAndReady");
            carRepo.save(request.getCar());
        }

        return convertToDTO(maintenanceRepo.save(request));
    }

    @Transactional
    public RentalMaintenanceRequestDTO completeRequest(Long id, LocalDateTime returnDate) {
        RentalMaintenanceRequest request = maintenanceRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        // Calculate date difference (integer days) and store as String
        if (request.getRequestDate() != null && returnDate != null) {
            long daysBetween = java.time.Duration
                    .between(request.getRequestDate(), returnDate)
                    .toDays();

            // Convert integer to string before saving
            request.setDateDifference(String.valueOf((int) daysBetween));
        }

        request.setStatus("RETURNED");
        request.setReturnDate(returnDate);

        // Update vehicle status
        if (request.getRentalCar() != null) {
            request.getRentalCar().setStatus("InspectedAndReady");
            orgCarRepo.save(request.getRentalCar());
        } else if (request.getCar() != null) {
            request.getCar().setStatus("Approved");
            carRepo.save(request.getCar());
        }

        return convertToDTO(maintenanceRepo.save(request));
    }



    public List<RentalMaintenanceRequestDTO> getAllRequests() {
        return maintenanceRepo.findAll().stream().map(this::convertToDTO).toList();
    }

    public List<RentalMaintenanceRequestDTO> getPendingRequests() {
        return maintenanceRepo.findByStatus("PENDING").stream().map(this::convertToDTO).toList();
    }

    private RentalMaintenanceRequestDTO convertToDTO(RentalMaintenanceRequest request) {
        RentalMaintenanceRequestDTO dto = new RentalMaintenanceRequestDTO();
        dto.setId(request.getId());
        dto.setPlateNumber(request.getPlateNumber());
        dto.setDriverName(request.getDriverName());
        dto.setCarType(request.getCarType());
        dto.setRequesterName(request.getRequesterName());
        dto.setRequesterPhone(request.getRequesterPhone());
        dto.setRequestDate(request.getRequestDate());
        dto.setServiceDate(request.getServiceDate());
        dto.setReturnDate(request.getReturnDate());
        dto.setReason(request.getReason());
        dto.setRequestType(request.getRequestType());
        dto.setDateDifference(request.getDateDifference());
        dto.setStatus(request.getStatus());
        dto.setMaintenanceNotes(request.getMaintenanceNotes());

        if (request.getRentalCar() != null) {
            dto.setRentalCarId(request.getRentalCar().getId());
        }
        if (request.getCar() != null) {
            dto.setCarId(request.getCar().getId());
        }

        return dto;
    }
}