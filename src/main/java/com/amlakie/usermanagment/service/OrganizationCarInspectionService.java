package com.amlakie.usermanagment.service;

import com.amlakie.usermanagment.dto.organization.*;
import com.amlakie.usermanagment.entity.OrganizationCar;
import com.amlakie.usermanagment.entity.organization.*;
import com.amlakie.usermanagment.entity.organization.enums.InspectionStatusType;
import com.amlakie.usermanagment.entity.organization.enums.ServiceStatusType;
import com.amlakie.usermanagment.repository.OrganizationCarInspectionRepository;
import com.amlakie.usermanagment.repository.OrganizationCarRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrganizationCarInspectionService {

    private static final Logger log = LoggerFactory.getLogger(OrganizationCarInspectionService.class);

    // Car status strings (consider moving to an enum if OrganizationCar.status becomes an enum)
    private static final String CAR_STATUS_PENDING_INSPECTION = "PendingInspection"; // Or "PENDING_INSPECTION"
    private static final String CAR_STATUS_INSPECTED_READY = "InspectedAndReady"; // Or "INSPECTED_AND_READY"
    private static final String CAR_STATUS_INSPECTION_REJECTED = "InspectionRejected"; // Or "INSPECTION_REJECTED"
    // private static final String CAR_STATUS_UNKNOWN = "Unknown"; // If needed

    private final OrganizationCarInspectionRepository orgCarInspectionRepository;
    private final OrganizationCarRepository orgCarRepository;

    @Autowired
    public OrganizationCarInspectionService(
            OrganizationCarInspectionRepository orgCarInspectionRepository,
            OrganizationCarRepository orgCarRepository
    ) {
        this.orgCarInspectionRepository = orgCarInspectionRepository;
        this.orgCarRepository = orgCarRepository;
    }

    @Transactional
    public OrganizationCarInspectionReqRes createInspection(OrganizationCarInspectionReqRes request) {
        if (request == null || request.getPlateNumber() == null || request.getPlateNumber().trim().isEmpty()) {
            log.warn("Organization car inspection request was null or missing plate number.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Organization car inspection request is invalid or missing plate number");
        }

        final String plateNumber = request.getPlateNumber();
        OrganizationCar orgCar = orgCarRepository.findByPlateNumber(plateNumber)
                .orElseGet(() -> createNewOrganizationCar(plateNumber));

        applyInspectionBusinessLogic(request);

        OrganizationCarInspection organizationCarInspection;
        try {
            organizationCarInspection = mapRequestToEntity(request);
        } catch (IllegalArgumentException iae) {
            log.warn("Error mapping enum value during DTO to Entity conversion for plate {}: {}", plateNumber, iae.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid value provided for inspection status or service status: " + iae.getMessage(), iae);
        }

        organizationCarInspection.setOrganizationCar(orgCar);

        OrganizationCarInspection savedInspection;
        try {
            savedInspection = orgCarInspectionRepository.save(organizationCarInspection);
            log.info("Successfully created inspection with id {} for car plate number {}", savedInspection.getId(), plateNumber);
            updateCarAfterInspection(savedInspection);
        } catch (DataAccessException e) {
            log.error("Database error saving organization car inspection for plate number {}: {}", plateNumber, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error saving organization car inspection", e);
        } catch (Exception e) { // Catch other exceptions from updateCarAfterInspection
            log.error("Unexpected error during inspection creation or car update for plate number {}: {}", plateNumber, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing inspection and updating car", e);
        }

        OrganizationCarInspectionReqRes orgResponse = mapEntityToResponse(savedInspection);
        if (orgResponse != null) {
            orgResponse.setCodStatus(HttpStatus.CREATED.value());
            orgResponse.setMessage("Inspection created successfully");
        } else {
            log.error("Mapping saved entity ID {} to response returned null!", savedInspection.getId());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to map created inspection to response");
        }
        return orgResponse;
    }

    private OrganizationCar createNewOrganizationCar(String plateNumber) {
        log.info("OrganizationCar with plate number {} not found. Creating new entry.", plateNumber);
        OrganizationCar newCar = new OrganizationCar();
        newCar.setPlateNumber(plateNumber);
        newCar.setOwnerName("Unknown");
        newCar.setCarType("UNKNOWN"); // If CarType is an enum, this needs proper mapping or default enum
        newCar.setOwnerPhone("0000000000");
        newCar.setModel("UNKNOWN");
        newCar.setFuelType("UNKNOWN"); // If FuelType is an enum, this needs proper mapping or default enum
        newCar.setCreatedBy("SYSTEM_AUTO_CREATE");
        newCar.setParkingLocation("UNKNOWN");
        newCar.setMotorCapacity("0"); // Assuming String, adjust if number
        newCar.setTotalKm(0.0); // Assuming String, adjust if number
        newCar.setRegisteredDate(LocalDateTime.now());
        newCar.setStatus(CAR_STATUS_PENDING_INSPECTION); // If Status is an enum, this needs proper mapping
        newCar.setManufactureYear("0"); // Assuming String, adjust if number
        try {
            newCar.setKmPerLiter(0.0f); // Assuming float
        } catch (NumberFormatException e) {
            log.error("Failed to parse default kmPerLiter '0.0'", e);
            newCar.setKmPerLiter(0.0f);
        }
        newCar.setInspected(false);

        try {
            OrganizationCar savedCar = orgCarRepository.save(newCar);
            log.info("Successfully created new OrganizationCar with plate number {}", savedCar.getPlateNumber());
            return savedCar;
        } catch (DataAccessException e) {
            log.error("Database error saving new organization car with plate number {}: {}", plateNumber, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save new organization car details", e);
        }
    }

    private void applyInspectionBusinessLogic(OrganizationCarInspectionReqRes request) {
        if (request == null) {
            log.warn("applyInspectionBusinessLogic called with null request.");
            return;
        }

        boolean mechanicalFailed = request.getMechanicalDetails() != null && !checkMechanicalPass(request.getMechanicalDetails());

        if (mechanicalFailed) {
            log.info("Mechanical check failed for plate {}. Setting status to Rejected.", request.getPlateNumber());
            request.setInspectionStatus(OrganizationCarInspectionReqRes.InspectionStatus.Rejected); // DTO Enum
            String failureNote = "Inspection failed due to critical mechanical issues.";
            request.setNotes(request.getNotes() == null || request.getNotes().isEmpty() ? failureNote : request.getNotes() + " " + failureNote);
        }

        if (request.getInspectionStatus() != OrganizationCarInspectionReqRes.InspectionStatus.Rejected) {
            int calculatedBodyScore = calculateBodyScore(request.getBodyDetails());
            int calculatedInteriorScore = calculateInteriorScore(request.getInteriorDetails());
            request.setBodyScore(calculatedBodyScore);
            request.setInteriorScore(calculatedInteriorScore);

            if (calculatedBodyScore < 70 || calculatedInteriorScore < 70) {
                log.info("Scores below threshold for plate {}. Setting status to Rejected.", request.getPlateNumber());
                request.setInspectionStatus(OrganizationCarInspectionReqRes.InspectionStatus.Rejected); // DTO Enum
                String scoreFailureNote = "Inspection failed due to low body or interior score.";
                request.setNotes(request.getNotes() == null || request.getNotes().isEmpty() ? scoreFailureNote : request.getNotes() + " " + scoreFailureNote);
            }
        } else {
            log.debug("Inspection already rejected mechanically, setting scores to 0 for plate {}", request.getPlateNumber());
            request.setBodyScore(0);
            request.setInteriorScore(0);
        }

        // Determine service status based on final inspection status (using DTO enums)
        if (request.getInspectionStatus() == OrganizationCarInspectionReqRes.InspectionStatus.Approved) {
            request.setServiceStatus(OrganizationCarInspectionReqRes.ServiceStatus.Ready);
        } else if (request.getInspectionStatus() == OrganizationCarInspectionReqRes.InspectionStatus.ConditionallyApproved) {
            request.setServiceStatus(OrganizationCarInspectionReqRes.ServiceStatus.ReadyWithWarning);
        } else { // Rejected or other
            request.setServiceStatus(OrganizationCarInspectionReqRes.ServiceStatus.Pending); // Or NotReady based on rules
        }
        log.debug("Applied business logic for plate {}. Final DTO statuses: Inspection={}, Service={}",
                request.getPlateNumber(), request.getInspectionStatus(), request.getServiceStatus());
    }

    private boolean checkMechanicalPass(@NotNull(message = "Mechanical inspection details are required") @Valid OrganizationMechanicalInspectionDTO mechanical) {
        if (mechanical == null) {
            log.warn("checkMechanicalPass called with null MechanicalDetailsDTO. Assuming failure.");
            return false;
        }
        return mechanical.isEngineCondition() &&
                mechanical.isEnginePower() &&
                mechanical.isSuspension() &&
                mechanical.isBrakes() &&
                mechanical.isSteering() &&
                mechanical.isGearbox();
    }

    private int calculateBodyScore(@NotNull(message = "Body inspection details are required") @Valid OrganizationBodyInspectionDTO bodyDetails) {
        if (bodyDetails == null) {
            log.warn("calculateBodyScore called with null BodyDetailsDTO. Assuming 0 score.");
            return 0;
        }
        int score = 100;
        score -= calculatePointsDeducted(bodyDetails.getBodyCollision());
        score -= calculatePointsDeducted(bodyDetails.getBodyScratches());
        score -= calculatePointsDeducted(bodyDetails.getPaintCondition());
        score -= calculatePointsDeducted(bodyDetails.getBreakages());
        score -= calculatePointsDeducted(bodyDetails.getCracks());
        return Math.max(0, score);
    }

    private int calculateInteriorScore(@NotNull(message = "Interior inspection details are required") @Valid OrganizationInteriorInspectionDTO interiorDetails) {
        if (interiorDetails == null) {
            log.warn("calculateInteriorScore called with null InteriorDetailsDTO. Assuming 0 score.");
            return 0;
        }
        int score = 100;
        score -= calculatePointsDeducted(interiorDetails.getEngineExhaust());
        score -= calculatePointsDeducted(interiorDetails.getSeatComfort());
        // ... (rest of the interior score calculations) ...
        score -= calculatePointsDeducted(interiorDetails.getChargingIndicator());
        return Math.max(0, score);
    }

    private int calculatePointsDeducted(@NotNull @Valid OrganizationItemConditionDTO condition) {
        if (condition == null || !condition.getProblem()) {
            return 0;
        }
        if (condition.getSeverity() == null) {
            log.warn("Severity is null for a problem item. Applying default deduction (5 points).");
            return 5;
        }
        // Assuming OrganizationItemConditionDTO.Severity is an enum in the DTO
        switch (condition.getSeverity()) {
            case HIGH: return 20;
            case MEDIUM: return 10;
            case LOW: return 5;
            case NONE:
            default:
                log.warn("Unexpected or NONE severity '{}' found for a problem item (problem=true). Applying default deduction (5 points).", condition.getSeverity());
                return 5;
        }
    }

    @Transactional
    protected void updateCarAfterInspection(OrganizationCarInspection orgInspection) {
        if (orgInspection == null || orgInspection.getOrganizationCar() == null) {
            log.warn("Cannot update car status - inspection or associated car is null.");
            return;
        }

        OrganizationCar car = orgInspection.getOrganizationCar();
        String currentCarStatusString = car.getStatus(); // Assuming car.status is String
        String newCarStatusString = currentCarStatusString;
        boolean needsSave = false;

        if (orgInspection.getId() != null) {
            if (car.getLatestInspectionId() == null || !car.getLatestInspectionId().equals(orgInspection.getId())) {
                log.info("Updating latestInspectionId for car plate {} to inspection ID {}", car.getPlateNumber(), orgInspection.getId());
                car.setLatestInspectionId(orgInspection.getId());
                needsSave = true;
            }
        }

        if (car.isInspected() == null || !car.isInspected()) {
            log.info("Marking car plate {} as inspected (boolean flag).", car.getPlateNumber());
            car.setInspected(true);
            needsSave = true;
        }

        InspectionStatusType inspectionEntityStatus = orgInspection.getInspectionStatus(); // Backend Entity Enum

        if (inspectionEntityStatus != null) {
            if (inspectionEntityStatus == InspectionStatusType.APPROVED) {
                newCarStatusString = CAR_STATUS_INSPECTED_READY;
            } else if (inspectionEntityStatus == InspectionStatusType.REJECTED) {
                newCarStatusString = CAR_STATUS_INSPECTION_REJECTED;
            } else if (inspectionEntityStatus == InspectionStatusType.CONDITIONALLY_APPROVED) {
                newCarStatusString = CAR_STATUS_INSPECTED_READY; // Or a specific "ConditionallyReady" string status
            } else if (inspectionEntityStatus == InspectionStatusType.PENDING) {
                log.debug("Inspection status is PENDING for inspection ID {}. Car string status remains '{}'.", orgInspection.getId(), currentCarStatusString);
            } else {
                log.warn("Unknown InspectionStatusType '{}' found for inspection ID {}. Car string status not updated.", inspectionEntityStatus, orgInspection.getId());
            }
        } else {
            log.warn("Inspection status is null for inspection ID {}. Car string status not updated.", orgInspection.getId());
        }

        if (!Objects.equals(currentCarStatusString, newCarStatusString)) {
            log.info("Updating string status for car plate {} from '{}' to '{}'", car.getPlateNumber(), currentCarStatusString, newCarStatusString);
            car.setStatus(newCarStatusString); // Assuming car.setStatus takes a String
            needsSave = true;
        } else {
            log.debug("Car string status ('{}') for plate {} remains unchanged after inspection ID {}.", currentCarStatusString, car.getPlateNumber(), orgInspection.getId());
        }

        if (needsSave) {
            try {
                orgCarRepository.save(car);
                log.info("Successfully saved updates (status/inspected flag) for car plate {}", car.getPlateNumber());
            } catch (DataAccessException e) {
                log.error("Database error updating car plate {} after inspection: {}", car.getPlateNumber(), e.getMessage(), e);
                throw new RuntimeException("Failed to save car updates after inspection for plate " + car.getPlateNumber(), e);
            }
        } else {
            log.debug("No changes required for car plate {} after inspection ID {}.", car.getPlateNumber(), orgInspection.getId());
        }
    }

    @Transactional(readOnly = true)
    public OrganizationCarInspectionListResponse getAllInspections() {
        // ... (implementation as before, ensuring mapEntityToResponse is robust)
        List<OrganizationCarInspection> orgInspections;
        try {
            orgInspections = orgCarInspectionRepository.findAll();
        } catch (DataAccessException e) {
            log.error("Database error retrieving all car inspections: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving inspections: Database error.", e);
        }
        List<OrganizationCarInspectionReqRes> orgInspectionDTOs = orgInspections.stream()
                .map(this::mapEntityToResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        OrganizationCarInspectionListResponse response = new OrganizationCarInspectionListResponse();
        response.setInspections(orgInspectionDTOs);
        response.setCodStatus(HttpStatus.OK.value());
        response.setMessage("Inspections retrieved successfully");
        return response;
    }

    @Transactional(readOnly = true)
    public OrganizationCarInspectionReqRes getInspectionById(Long id) {
        // ... (implementation as before, ensuring mapEntityToResponse is robust)
        try {
            OrganizationCarInspection orgInspection = orgCarInspectionRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inspection with id " + id + " not found"));
            log.debug("Retrieved inspection with id: {}", id);

            OrganizationCarInspectionReqRes response = mapEntityToResponse(orgInspection);
            if (response == null) {
                log.error("mapEntityToResponse returned null for a found inspection ID {}", id);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error mapping inspection to response");
            }
            response.setCodStatus(HttpStatus.OK.value());
            response.setMessage("Inspection retrieved successfully");
            return response;
        } catch (ResponseStatusException e) {
            log.warn("Failed to find inspection with id {}: {}", id, e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            log.error("Database error retrieving inspection with id {}: {}", id, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving inspection", e);
        } catch (IllegalArgumentException iae) {
            log.warn("Error mapping enum value from entity to DTO for inspection ID {}: {}", id, iae.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error mapping inspection data", iae);
        }
    }

    @Transactional
    public OrganizationCarInspectionReqRes updateInspection(Long id, OrganizationCarInspectionReqRes request) {
        // ... (implementation as before, ensuring robust enum mapping in updateExistingEntityFromRequest)
        try {
            OrganizationCarInspection existingInspection = orgCarInspectionRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inspection with id " + id + " not found for update"));

            applyInspectionBusinessLogic(request); // Apply logic to DTO first

            updateExistingEntityFromRequest(existingInspection, request); // Then map DTO to existing entity

            OrganizationCarInspection savedInspection = orgCarInspectionRepository.save(existingInspection);
            log.info("Successfully updated inspection with id: {}", id);
            updateCarAfterInspection(savedInspection);

            OrganizationCarInspectionReqRes response = mapEntityToResponse(savedInspection);
            if (response == null) {
                log.error("mapEntityToResponse returned null for updated inspection ID {}", id);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error mapping updated inspection to response");
            }
            response.setCodStatus(HttpStatus.OK.value());
            response.setMessage("Inspection updated successfully");
            return response;
        } catch (ResponseStatusException e) {
            log.warn("Failed to update inspection with id {}: {}", id, e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            log.error("Database error updating inspection with id {}: {}", id, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating inspection", e);
        } catch (IllegalArgumentException iae) {
            log.warn("Error mapping enum value during inspection update for ID {}: {}", id, iae.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid value provided for inspection status or service status: " + iae.getMessage(), iae);
        }
    }

    @Transactional
    public void deleteInspection(Long id) {
        // ... (implementation as before)
        try {
            Optional<OrganizationCarInspection> inspectionOpt = orgCarInspectionRepository.findById(id);
            if (inspectionOpt.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Inspection with id " + id + " not found for deletion.");
            }
            orgCarInspectionRepository.deleteById(id);
            log.info("Successfully deleted inspection with id: {}", id);

            inspectionOpt.ifPresent(inspection -> {
                if (inspection.getOrganizationCar() != null) {
                    OrganizationCar car = inspection.getOrganizationCar();
                    if (car.getLatestInspectionId() != null && car.getLatestInspectionId().equals(id)) {
                        log.info("Deleted inspection {} was the latest for car plate {}. Clearing latestInspectionId.", id, car.getPlateNumber());
                        car.setLatestInspectionId(null);
                        // Potentially update car.setStatus(...) here based on new state
                        try {
                            orgCarRepository.save(car);
                            log.info("Successfully updated car plate {} after deleting latest inspection {}", car.getPlateNumber(), id);
                        } catch (Exception e) {
                            log.error("Failed to update car plate {} after deleting latest inspection {}", car.getPlateNumber(), id, e);
                        }
                    }
                }
            });
        } catch (ResponseStatusException e) {
            log.warn("Failed to delete inspection with id {}: {}", id, e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            log.error("Database error deleting inspection with id {}: {}", id, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting inspection, potentially due to related data.", e);
        }
    }

    @Transactional(readOnly = true)
    public OrganizationCarInspectionListResponse getInspectionsByPlateNumber(String plateNumber) {
        // ... (implementation as before, ensuring mapEntityToResponse is robust)
        if (plateNumber == null || plateNumber.trim().isEmpty()) {
            log.warn("Attempted to get inspections with null or empty plate number.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plate number cannot be empty");
        }
        List<OrganizationCarInspection> inspections;
        try {
            inspections = orgCarInspectionRepository.findByOrganizationCar_PlateNumber(plateNumber);
            log.info("Retrieved {} inspections for plate number: {}", inspections.size(), plateNumber);
        } catch (DataAccessException e) {
            log.error("Database error retrieving inspections for plate number {}: {}", plateNumber, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving inspections by plate number", e);
        }
        List<OrganizationCarInspectionReqRes> orgInspectionDTOs = inspections.stream()
                .map(this::mapEntityToResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        OrganizationCarInspectionListResponse response = new OrganizationCarInspectionListResponse();
        response.setInspections(orgInspectionDTOs);
        response.setCodStatus(HttpStatus.OK.value());
        response.setMessage("Inspections retrieved successfully for plate number: " + plateNumber);
        return response;
    }

    // Helper to convert PascalCase or CamelCase to UPPER_SNAKE_CASE
    private String convertPascalOrCamelToSnakeCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        // Add underscore before uppercase letters (but not if it's the first letter,
        // and not if it's preceded by another uppercase letter or an underscore already)
        // This regex is a common way to achieve this.
        return input.replaceAll("(?<=[a-z0-9])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])", "_").toUpperCase();
    }


    private OrganizationCarInspection mapRequestToEntity(OrganizationCarInspectionReqRes request) {
        if (request == null) {
            log.warn("mapRequestToEntity called with null request.");
            return null;
        }
        OrganizationCarInspection inspection = new OrganizationCarInspection();
        inspection.setInspectionDate(request.getInspectionDate());
        inspection.setInspectorName(request.getInspectorName());

        if (request.getInspectionStatus() != null) {
            try {
                String statusNameFromDto = request.getInspectionStatus().name(); // e.g., "ConditionallyApproved"
                String processedStatusName = convertPascalOrCamelToSnakeCase(statusNameFromDto); // e.g., "CONDITIONALLY_APPROVED"
                inspection.setInspectionStatus(InspectionStatusType.valueOf(processedStatusName));
            } catch (IllegalArgumentException e) {
                log.error("Failed to map InspectionStatus from DTO '{}' to entity enum. Processed as '{}'",
                        request.getInspectionStatus().name(), convertPascalOrCamelToSnakeCase(request.getInspectionStatus().name()), e);
                throw e; // Re-throw to be caught by calling method
            }
        } else {
            inspection.setInspectionStatus(null);
        }

        if (request.getServiceStatus() != null) {
            try {
                String statusNameFromDto = request.getServiceStatus().name(); // e.g., "ReadyWithWarning"
                String processedStatusName = convertPascalOrCamelToSnakeCase(statusNameFromDto); // e.g., "READY_WITH_WARNING"
                inspection.setServiceStatus(ServiceStatusType.valueOf(processedStatusName));
            } catch (IllegalArgumentException e) {
                log.error("Failed to map ServiceStatus from DTO '{}' to entity enum. Processed as '{}'",
                        request.getServiceStatus().name(), convertPascalOrCamelToSnakeCase(request.getServiceStatus().name()), e);
                throw e; // Re-throw to be caught by calling method
            }
        } else {
            inspection.setServiceStatus(null);
        }

        inspection.setBodyScore(request.getBodyScore());
        inspection.setInteriorScore(request.getInteriorScore());
        inspection.setNotes(request.getNotes());

        if (request.getMechanicalDetails() != null) {
            inspection.setMechanicalDetails(mapOrgMechanicalDTOtoEntity(request.getMechanicalDetails()));
            if (inspection.getMechanicalDetails() != null) {
                inspection.getMechanicalDetails().setOrganizationCarInspection(inspection);
            }
        }
        if (request.getBodyDetails() != null) {
            inspection.setBodyDetails(mapOrgBodyDTOtoEntity(request.getBodyDetails()));
            if (inspection.getBodyDetails() != null) {
                inspection.getBodyDetails().setOrganizationCarInspection(inspection);
            }
        }
        if (request.getInteriorDetails() != null) {
            inspection.setInteriorDetails(mapOrgInteriorDTOtoEntity(request.getInteriorDetails()));
            if (inspection.getInteriorDetails() != null) {
                inspection.getInteriorDetails().setOrganizationCarInspection(inspection);
            }
        }
        return inspection;
    }

    private void updateExistingEntityFromRequest(OrganizationCarInspection existingInspection, OrganizationCarInspectionReqRes request) {
        if (request == null || existingInspection == null) {
            log.warn("Attempted to update inspection with null request or entity.");
            return;
        }

        if (request.getInspectionDate() != null) existingInspection.setInspectionDate(request.getInspectionDate());
        if (request.getInspectorName() != null && !request.getInspectorName().isBlank()) existingInspection.setInspectorName(request.getInspectorName());
        if (request.getBodyScore() != null) existingInspection.setBodyScore(request.getBodyScore());
        if (request.getInteriorScore() != null) existingInspection.setInteriorScore(request.getInteriorScore());
        if (request.getNotes() != null) existingInspection.setNotes(request.getNotes());

        if (request.getInspectionStatus() != null) {
            try {
                String statusNameFromDto = request.getInspectionStatus().name();
                String processedStatusName = convertPascalOrCamelToSnakeCase(statusNameFromDto);
                existingInspection.setInspectionStatus(InspectionStatusType.valueOf(processedStatusName));
            } catch (IllegalArgumentException e) {
                log.error("Failed to map InspectionStatus from DTO '{}' to entity enum during update. Processed as '{}'",
                        request.getInspectionStatus().name(), convertPascalOrCamelToSnakeCase(request.getInspectionStatus().name()), e);
                throw e;
            }
        }

        if (request.getServiceStatus() != null) {
            try {
                String statusNameFromDto = request.getServiceStatus().name();
                String processedStatusName = convertPascalOrCamelToSnakeCase(statusNameFromDto);
                existingInspection.setServiceStatus(ServiceStatusType.valueOf(processedStatusName));
            } catch (IllegalArgumentException e) {
                log.error("Failed to map ServiceStatus from DTO '{}' to entity enum during update. Processed as '{}'",
                        request.getServiceStatus().name(), convertPascalOrCamelToSnakeCase(request.getServiceStatus().name()), e);
                throw e;
            }
        }

        if (request.getMechanicalDetails() != null) {
            if (existingInspection.getMechanicalDetails() == null) {
                existingInspection.setMechanicalDetails(new OrganizationMechanicalInspection());
                existingInspection.getMechanicalDetails().setOrganizationCarInspection(existingInspection);
            }
            updateOrgMechanicalFromDTO(existingInspection.getMechanicalDetails(), request.getMechanicalDetails());
        }

        if (request.getBodyDetails() != null) {
            if (existingInspection.getBodyDetails() == null) {
                existingInspection.setBodyDetails(new OrganizationBodyInspection());
                existingInspection.getBodyDetails().setOrganizationCarInspection(existingInspection);
            }
            updateOrgBodyFromDTO(existingInspection.getBodyDetails(), request.getBodyDetails());
        }

        if (request.getInteriorDetails() != null) {
            if (existingInspection.getInteriorDetails() == null) {
                existingInspection.setInteriorDetails(new OrganizationInteriorInspection());
                existingInspection.getInteriorDetails().setOrganizationCarInspection(existingInspection);
            }
            updateOrgInteriorFromDTO(existingInspection.getInteriorDetails(), request.getInteriorDetails());
        }
    }

    // --- DTO -> Entity Update Helpers for Details ---
    private void updateOrgMechanicalFromDTO(OrganizationMechanicalInspection entity, @NotNull @Valid OrganizationMechanicalInspectionDTO dto) {
        // ... (implementation as before) ...
        if (dto == null || entity == null) return;
        entity.setEngineCondition(dto.isEngineCondition());
        entity.setEnginePower(dto.isEnginePower());
        entity.setSuspension(dto.isSuspension());
        entity.setBrakes(dto.isBrakes());
        entity.setSteering(dto.isSteering());
        entity.setGearbox(dto.isGearbox());
        entity.setMileage(dto.isMileage());
        entity.setFuelGauge(dto.isFuelGauge());
        entity.setTempGauge(dto.isTempGauge());
        entity.setOilGauge(dto.isOilGauge());
    }

    private void updateOrgBodyFromDTO(OrganizationBodyInspection entity, @NotNull @Valid OrganizationBodyInspectionDTO dto) {
        // ... (implementation as before) ...
        if (dto == null || entity == null) return;
        if (dto.getBodyCollision() != null) entity.setBodyCollision(mapProblemDetailDTO_To_OrganizationItemConditionEntity(dto.getBodyCollision()));
        if (dto.getBodyScratches() != null) entity.setBodyScratches(mapProblemDetailDTO_To_OrganizationItemConditionEntity(dto.getBodyScratches()));
        if (dto.getPaintCondition() != null) entity.setPaintCondition(mapProblemDetailDTO_To_OrganizationItemConditionEntity(dto.getPaintCondition()));
        if (dto.getBreakages() != null) entity.setBreakages(mapProblemDetailDTO_To_OrganizationItemConditionEntity(dto.getBreakages()));
        if (dto.getCracks() != null) entity.setCracks(mapProblemDetailDTO_To_OrganizationItemConditionEntity(dto.getCracks()));
    }

    private void updateOrgInteriorFromDTO(OrganizationInteriorInspection entity, @NotNull @Valid OrganizationInteriorInspectionDTO dto) {
        // ... (implementation as before, mapping all interior items) ...
        if (dto == null || entity == null) return;
        if (dto.getEngineExhaust() != null) entity.setEngineExhaust(mapProblemDetailDTO_To_OrganizationItemConditionEntity(dto.getEngineExhaust()));
        // ... map all other interior DTO fields to entity fields ...
        if (dto.getChargingIndicator() != null) entity.setChargingIndicator(mapProblemDetailDTO_To_OrganizationItemConditionEntity(dto.getChargingIndicator()));
    }

    // --- DTO -> Entity Mapping Helpers for NEW Detail Entities ---
    private OrganizationMechanicalInspection mapOrgMechanicalDTOtoEntity(@NotNull @Valid OrganizationMechanicalInspectionDTO dto) {
        if (dto == null) return null;
        OrganizationMechanicalInspection entity = new OrganizationMechanicalInspection();
        updateOrgMechanicalFromDTO(entity, dto);
        return entity;
    }

    private OrganizationBodyInspection mapOrgBodyDTOtoEntity(@NotNull @Valid OrganizationBodyInspectionDTO dto) {
        if (dto == null) return null;
        OrganizationBodyInspection entity = new OrganizationBodyInspection();
        updateOrgBodyFromDTO(entity, dto);
        return entity;
    }

    private OrganizationInteriorInspection mapOrgInteriorDTOtoEntity(@NotNull @Valid OrganizationInteriorInspectionDTO dto) {
        if (dto == null) return null;
        OrganizationInteriorInspection entity = new OrganizationInteriorInspection();
        updateOrgInteriorFromDTO(entity, dto);
        return entity;
    }

    private OrganizationItemCondition mapProblemDetailDTO_To_OrganizationItemConditionEntity(@NotNull @Valid OrganizationItemConditionDTO dto) {
        if (dto == null) return null;
        OrganizationItemCondition entity = new OrganizationItemCondition();
        entity.setProblem(dto.getProblem());
        if (dto.getSeverity() != null) {
            entity.setSeverity(dto.getSeverity().name()); // Assuming DTO Severity is enum, entity Severity is String
        } else {
            entity.setSeverity(null); // Or a default string like "NONE"
        }
        entity.setNotes(dto.getNotes());
        return entity;
    }

    // --- Entity -> Response DTO Mapping ---
    private OrganizationCarInspectionReqRes mapEntityToResponse(OrganizationCarInspection inspection) {
        if (inspection == null) {
            log.warn("mapEntityToResponse called with null inspection.");
            return null;
        }
        OrganizationCarInspectionReqRes response = new OrganizationCarInspectionReqRes();
        response.setId(inspection.getId());
        response.setPlateNumber(inspection.getOrganizationCar() != null ? inspection.getOrganizationCar().getPlateNumber() : null);
        response.setInspectionDate(inspection.getInspectionDate());
        response.setInspectorName(inspection.getInspectorName());

        if (inspection.getInspectionStatus() != null) {
            try {
                String entityStatusName = inspection.getInspectionStatus().name(); // e.g., CONDITIONALLY_APPROVED
                String dtoPascalCaseName = toPascalCaseFromSnakeCase(entityStatusName); // e.g., ConditionallyApproved
                response.setInspectionStatus(OrganizationCarInspectionReqRes.InspectionStatus.valueOf(dtoPascalCaseName));
            } catch (IllegalArgumentException e) {
                log.warn("Could not map inspectionStatus value '{}' from entity ID {} to DTO enum. DTO name: {}",
                        inspection.getInspectionStatus(), inspection.getId(), toPascalCaseFromSnakeCase(inspection.getInspectionStatus().name()), e);
                response.setInspectionStatus(null);
            }
        } else {
            response.setInspectionStatus(null);
        }

        if (inspection.getServiceStatus() != null) {
            try {
                String entityStatusName = inspection.getServiceStatus().name(); // e.g., READY_WITH_WARNING
                String dtoPascalCaseName = toPascalCaseFromSnakeCase(entityStatusName); // e.g., ReadyWithWarning
                response.setServiceStatus(OrganizationCarInspectionReqRes.ServiceStatus.valueOf(dtoPascalCaseName));
            } catch (IllegalArgumentException e) {
                log.warn("Could not map serviceStatus value '{}' from entity ID {} to DTO enum. DTO name: {}",
                        inspection.getServiceStatus(), inspection.getId(), toPascalCaseFromSnakeCase(inspection.getServiceStatus().name()), e);
                response.setServiceStatus(null);
            }
        } else {
            response.setServiceStatus(null);
        }

        response.setBodyScore(inspection.getBodyScore());
        response.setInteriorScore(inspection.getInteriorScore());
        response.setNotes(inspection.getNotes());

        if (inspection.getMechanicalDetails() != null) response.setMechanicalId(inspection.getMechanicalDetails().getId());
        if (inspection.getBodyDetails() != null) response.setBodyId(inspection.getBodyDetails().getId());
        if (inspection.getInteriorDetails() != null) response.setInteriorId(inspection.getInteriorDetails().getId());

        response.setMechanicalDetails(mapMechanicalEntityToDTO(inspection.getMechanicalDetails()));
        response.setBodyDetails(mapBodyEntityToDTO(inspection.getBodyDetails()));
        response.setInteriorDetails(mapInteriorEntityToDTO(inspection.getInteriorDetails()));
        return response;
    }

    // --- Entity -> DTO Mapping Helpers for Details ---
    private @NotNull @Valid OrganizationMechanicalInspectionDTO mapMechanicalEntityToDTO(OrganizationMechanicalInspection entity) {
        // ... (implementation as before) ...
        if (entity == null) return null;
        OrganizationMechanicalInspectionDTO dto = new OrganizationMechanicalInspectionDTO();
        dto.setEngineCondition(entity.isEngineCondition());
        dto.setEnginePower(entity.isEnginePower());
        dto.setSuspension(entity.isSuspension());
        dto.setBrakes(entity.isBrakes());
        dto.setSteering(entity.isSteering());
        dto.setGearbox(entity.isGearbox());
        dto.setMileage(entity.isMileage());
        dto.setFuelGauge(entity.isFuelGauge());
        dto.setTempGauge(entity.isTempGauge());
        dto.setOilGauge(entity.isOilGauge());
        return dto;
    }
    private @NotNull @Valid OrganizationBodyInspectionDTO mapBodyEntityToDTO(OrganizationBodyInspection entity) {
        // ... (implementation as before) ...
        if (entity == null) return null;
        OrganizationBodyInspectionDTO dto = new OrganizationBodyInspectionDTO();
        dto.setBodyCollision(mapOrganizationItemCondition_To_ProblemDetailDTO(entity.getBodyCollision()));
        dto.setBodyScratches(mapOrganizationItemCondition_To_ProblemDetailDTO(entity.getBodyScratches()));
        dto.setPaintCondition(mapOrganizationItemCondition_To_ProblemDetailDTO(entity.getPaintCondition()));
        dto.setBreakages(mapOrganizationItemCondition_To_ProblemDetailDTO(entity.getBreakages()));
        dto.setCracks(mapOrganizationItemCondition_To_ProblemDetailDTO(entity.getCracks()));
        return dto;
    }
    private @NotNull @Valid OrganizationInteriorInspectionDTO mapInteriorEntityToDTO(OrganizationInteriorInspection entity) {
        // ... (implementation as before, mapping all interior items) ...
        if (entity == null) return null;
        OrganizationInteriorInspectionDTO dto = new OrganizationInteriorInspectionDTO();
        dto.setEngineExhaust(mapOrganizationItemCondition_To_ProblemDetailDTO(entity.getEngineExhaust()));
        // ... map all other interior entity fields to DTO fields ...
        dto.setChargingIndicator(mapOrganizationItemCondition_To_ProblemDetailDTO(entity.getChargingIndicator()));
        return dto;
    }
    private @NotNull @Valid OrganizationItemConditionDTO mapOrganizationItemCondition_To_ProblemDetailDTO(OrganizationItemCondition entity) {
        // ... (implementation as before, ensuring robust mapping for Severity) ...
        if (entity == null) return null;
        OrganizationItemConditionDTO dto = new OrganizationItemConditionDTO();
        dto.setProblem(entity.isProblem());
        if (entity.getSeverity() != null) {
            try {
                // Assuming entity.getSeverity() is String (e.g., "HIGH")
                // and DTO Severity is enum (e.g., OrganizationItemConditionDTO.Severity.HIGH)
                dto.setSeverity(OrganizationItemConditionDTO.Severity.valueOf(entity.getSeverity().toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Could not map severity value '{}' from OrganizationItemCondition (ID: {}) to DTO enum",
                        entity.getSeverity(), entity.getId() != null ? entity.getId() : "N/A", e);
                dto.setSeverity(null); // Or a default
            }
        } else {
            dto.setSeverity(null);
        }
        dto.setNotes(entity.getNotes());
        return dto;
    }

    // Helper to convert UPPER_SNAKE_CASE (from entity enum .name()) to PascalCase (for DTO enum .valueOf())
    private String toPascalCaseFromSnakeCase(String upperSnakeCase) {
        if (upperSnakeCase == null || upperSnakeCase.isBlank()) {
            return upperSnakeCase;
        }
        String[] parts = upperSnakeCase.split("_");
        StringBuilder pascalCase = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                pascalCase.append(part.substring(0, 1).toUpperCase())
                        .append(part.substring(1).toLowerCase());
            }
        }
        return pascalCase.toString();
    }
}