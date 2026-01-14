package com.amlakie.usermanagment.service;

import com.amlakie.usermanagment.dto.AssignmentDTO;
import com.amlakie.usermanagment.dto.CompletionDTO;
import com.amlakie.usermanagment.dto.DailyServiceRequestDTO;
import com.amlakie.usermanagment.entity.DailyServiceRequest;
import com.amlakie.usermanagment.exception.InvalidRequestException;
import com.amlakie.usermanagment.exception.ResourceNotFoundException;
import com.amlakie.usermanagment.repository.DailyServiceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
public class DailyServiceRequestService {

    private final DailyServiceRequestRepository repository;
    private static final double KM_DIFFERENCE_THRESHOLD = 100.0;
    private static final double KM_VARIANCE_PERCENTAGE = 0.2; // 20% variance allowed

    @Autowired
    public DailyServiceRequestService(DailyServiceRequestRepository repository) {
        this.repository = repository;
    }

    public DailyServiceRequest createRequest(DailyServiceRequestDTO dto) {
        validateRequestDTO(dto);

        DailyServiceRequest request = new DailyServiceRequest();
        request.setRequestDate(dto.getRequestDate());
        request.setStartTime(dto.getStartTime());
        request.setReturnTime(dto.getReturnTime());
        request.setTravelers(dto.getTravelers());
        request.setStartingPlace(dto.getStartingPlace());
        request.setEndingPlace(dto.getEndingPlace());
        request.setClaimantName(dto.getClaimantName());
        request.setStatus(DailyServiceRequest.RequestStatus.PENDING);

        return repository.save(request);
    }

    public List<DailyServiceRequest> getPendingRequests() {
        return repository.findByStatus(DailyServiceRequest.RequestStatus.PENDING);
    }

    public List<DailyServiceRequest> getAllRequests() {
        return repository.findAll();
    }

    public DailyServiceRequest assignRequest(Long id, AssignmentDTO dto) {
        DailyServiceRequest request = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + id));

        if (request.getStatus() != DailyServiceRequest.RequestStatus.PENDING) {
            throw new InvalidRequestException("Only pending requests can be assigned");
        }

        validateAssignmentDTO(dto);

        request.setDriverName(dto.getDriverName());
        request.setCarType(dto.getCarType());
        request.setPlateNumber(dto.getPlateNumber());
        request.setEstimatedKilometers(Double.parseDouble(dto.getEstimatedKilometers()));
        request.setStatus(DailyServiceRequest.RequestStatus.ASSIGNED);

        return repository.save(request);
    }

    public DailyServiceRequest completeRequest(Long id, CompletionDTO dto) {
        DailyServiceRequest request = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + id));

        if (request.getStatus() != DailyServiceRequest.RequestStatus.ASSIGNED) {
            throw new InvalidRequestException("Only assigned requests can be completed");
        }

        validateCompletionData(request, dto);

        double kmDifference = calculateKmDifference(dto.getStartKm(), dto.getEndKm());

        // Set completion data
        request.setStartKm(dto.getStartKm());
        request.setEndKm(dto.getEndKm());
        request.setKmDifference(kmDifference);
        request.setReason(dto.getReason());
        request.setKmReason(dto.getKmReason());
        request.setStatus(DailyServiceRequest.RequestStatus.COMPLETED);
        request.setCompletedAt(LocalDateTime.now());

        // Compare with estimated kilometers if available
        if (request.getEstimatedKilometers() != null) {
            validateKilometerVariance(request.getEstimatedKilometers(), kmDifference, dto);
        }

        return repository.save(request);
    }

    public List<DailyServiceRequest> getRequestsForDriver(String driverName) {
        if (driverName == null || driverName.isEmpty()) {
            return repository.findByStatus(DailyServiceRequest.RequestStatus.ASSIGNED);
        }
        return repository.findByDriverNameAndStatus(
                driverName,
                DailyServiceRequest.RequestStatus.ASSIGNED
        );
    }

    public DailyServiceRequest getRequestById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + id));
    }

    // Validation methods
    private void validateRequestDTO(DailyServiceRequestDTO dto) {
        if (dto.getRequestDate() == null) {
            throw new InvalidRequestException("Request date is required");
        }

        if (dto.getRequestDate().isBefore(LocalDate.now())) {
            throw new InvalidRequestException("Request date cannot be in the past");
        }

        if (dto.getStartTime() == null) {
            throw new InvalidRequestException("Start time is required");
        }

        if (dto.getReturnTime() != null && dto.getStartTime() != null &&
                dto.getReturnTime().isBefore(dto.getStartTime())) {
            throw new InvalidRequestException("Return time cannot be before start time");
        }

        if (dto.getTravelers() == null || dto.getTravelers().isEmpty()) {
            throw new InvalidRequestException("At least one traveler is required");
        }

        dto.getTravelers().forEach(traveler -> {
            if (traveler == null || traveler.trim().isEmpty()) {
                throw new InvalidRequestException("Traveler name cannot be empty");
            }
        });

        if (dto.getStartingPlace() == null || dto.getStartingPlace().trim().isEmpty()) {
            throw new InvalidRequestException("Starting place is required");
        }

        if (dto.getEndingPlace() == null || dto.getEndingPlace().trim().isEmpty()) {
            throw new InvalidRequestException("Ending place is required");
        }

        if (dto.getClaimantName() == null || dto.getClaimantName().trim().isEmpty()) {
            throw new InvalidRequestException("Claimant name is required");
        }
    }

    private void validateAssignmentDTO(AssignmentDTO dto) {
        if (dto.getDriverName() == null || dto.getDriverName().trim().isEmpty()) {
            throw new InvalidRequestException("Driver name is required");
        }

        if (dto.getCarType() == null || dto.getCarType().trim().isEmpty()) {
            throw new InvalidRequestException("Car type is required");
        }

        if (dto.getPlateNumber() == null || dto.getPlateNumber().trim().isEmpty()) {
            throw new InvalidRequestException("Plate number is required");
        }

        if (dto.getEstimatedKilometers() == null || dto.getEstimatedKilometers().trim().isEmpty()) {
            throw new InvalidRequestException("Estimated kilometers is required");
        }

        try {
            double km = Double.parseDouble(dto.getEstimatedKilometers());
            if (km <= 0) {
                throw new InvalidRequestException("Estimated kilometers must be positive");
            }
        } catch (NumberFormatException e) {
            throw new InvalidRequestException("Estimated kilometers must be a valid number");
        }
    }

    private void validateCompletionData(DailyServiceRequest request, CompletionDTO dto) {
        if (dto.getStartKm() == null) {
            throw new InvalidRequestException("Starting kilometers are required");
        }

        if (dto.getEndKm() == null) {
            throw new InvalidRequestException("Ending kilometers are required");
        }

        if (dto.getStartKm() < 0 || dto.getEndKm() < 0) {
            throw new InvalidRequestException("Kilometer values must be positive");
        }

        if (dto.getEndKm() < dto.getStartKm()) {
            throw new InvalidRequestException("Ending kilometers cannot be less than starting kilometers");
        }

        // Check for late return
        if (request.getReturnTime() != null &&
                LocalTime.now().isAfter(request.getReturnTime()) &&
                (dto.getReason() == null || dto.getReason().trim().isEmpty())) {
            throw new InvalidRequestException("Reason is required for late return");
        }
    }

    private void validateKilometerVariance(Double estimatedKm, Double actualKm, CompletionDTO dto) {
        double variance = Math.abs(actualKm - estimatedKm);
        double allowedVariance = estimatedKm * KM_VARIANCE_PERCENTAGE;

        if (variance > allowedVariance) {
            if (dto.getKmReason() == null || dto.getKmReason().trim().isEmpty()) {
                throw new InvalidRequestException(
                        String.format("Reason is required for kilometer difference exceeding %.1f km (estimated: %.1f km, actual: %.1f km)",
                                allowedVariance, estimatedKm, actualKm)
                );
            }
        }
    }

    private double calculateKmDifference(Double startKm, Double endKm) {
        return endKm - startKm;
    }
}