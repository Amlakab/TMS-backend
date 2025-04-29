package com.amlakie.usermanagment.service;

import com.amlakie.usermanagment.dto.*;
import com.amlakie.usermanagment.entity.*;
// Import custom exception if you create one
// import com.amlakie.usermanagment.exception.ResourceNotFoundException;
import com.amlakie.usermanagment.repository.CarInspectionRepository;
import com.amlakie.usermanagment.repository.CarRepository;
import org.slf4j.Logger; // Added for logging
import org.slf4j.LoggerFactory; // Added for logging
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException; // More specific exception
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Added for transaction management
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime; // Added for date handling
import java.util.List;
import java.util.Objects; // Used for null checks
import java.util.Optional;
import java.util.stream.Collectors;




@Service
public class CarInspectionService {

    // Added Logger
    private static final Logger log = LoggerFactory.getLogger(CarInspectionService.class);

    // --- Define constants for Car statuses ---
    private static final String CAR_STATUS_PENDING_INSPECTION = "PendingInspection";
    private static final String CAR_STATUS_INSPECTED_READY = "InspectedAndReady";
    private static final String CAR_STATUS_INSPECTION_REJECTED = "InspectionRejected";
    private static final String CAR_STATUS_UNKNOWN = "Unknown"; // Default/fallback

    private final CarInspectionRepository inspectionRepository;
    private final CarRepository carRepository;

    @Autowired
    public CarInspectionService(CarInspectionRepository inspectionRepository, CarRepository carRepository) {
        this.inspectionRepository = inspectionRepository;
        this.carRepository = carRepository;
    }

    @Transactional
    public CarInspectionReqRes createInspection(CarInspectionReqRes request) {
        if (request == null || request.getPlateNumber() == null || request.getPlateNumber().trim().isEmpty()) {
            log.warn("Car inspection request was null or missing plate number.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Car inspection request is invalid or missing plate number");
        }

        final String plateNumber = request.getPlateNumber();

        // Check if car exists, if not create it
        Car car = carRepository.findByPlateNumber(plateNumber)
                .orElseGet(() -> createNewCar(plateNumber)); // Extracted car creation logic

        // --- Apply Inspection Logic (e.g., Auto-Reject, Score Calculation) ---
        applyInspectionBusinessLogic(request); // Apply rules before mapping

        CarInspection inspection = mapRequestToEntity(request);
        inspection.setCar(car); // Link inspection to the car

        CarInspection savedInspection;
        try {
            savedInspection = inspectionRepository.save(inspection);
            log.info("Successfully created inspection with id {} for car plate number {}", savedInspection.getId(), plateNumber);

            // --- Update Car Status based on the saved inspection ---
            updateCarStatusBasedOnInspection(savedInspection);

        } catch (DataAccessException e) {
            log.error("Database error saving car inspection for plate number {}: {}", plateNumber, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error saving car inspection", e);
        } catch (Exception e) {
            log.error("Unexpected error saving car inspection for plate number {}: {}", plateNumber, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error saving car inspection", e);
        }

        CarInspectionReqRes response = mapEntityToResponse(savedInspection);
        response.setCodStatus(201);
        response.setMessage("Inspection created successfully");
        return response;
    }

    // --- Helper method to create a new car ---
    private Car createNewCar(String plateNumber) {
        log.info("Car with plate number {} not found. Creating new entry.", plateNumber);
        Car newCar = new Car();
        newCar.setPlateNumber(plateNumber);
        newCar.setOwnerName("Unknown");
        newCar.setCarType("Unknown");
        newCar.setOwnerPhone("0000000000");
        newCar.setModel("UNKNOWN");
        newCar.setFuelType("UNKNOWN");
        newCar.setCreatedBy("SYSTEM_AUTO_CREATE");
        newCar.setParkingLocation("UNKNOWN");
        newCar.setMotorCapacity("UNKNOWN");
        newCar.setTotalKm("0");
        newCar.setRegisteredDate(LocalDateTime.now());
        newCar.setStatus(CAR_STATUS_PENDING_INSPECTION); // Use constant
        newCar.setManufactureYear(0);
        newCar.setKmPerLiter(0.0f);
        // Ensure isInspected is set if required by DB schema
        // newCar.setIsInspected(false); // Example if needed

        try {
            return carRepository.save(newCar);
        } catch (DataAccessException e) {
            log.error("Database error saving new car with plate number {}: {}", plateNumber, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save new car details", e);
        }
    }

    // --- Placeholder for business logic (Auto-Reject, Score Calculation) ---
    private void applyInspectionBusinessLogic(CarInspectionReqRes request) {
        if (request == null) return;

        // 1. Auto-Reject based on Mechanical Failure
        if (request.getMechanical() != null && !checkMechanicalPass(request.getMechanical())) {
            log.info("Mechanical check failed for plate {}. Setting status to Rejected.", request.getPlateNumber());
            request.setInspectionStatus(CarInspectionReqRes.InspectionStatus.Rejected);
            // Optionally add/update notes
            String failureNote = "Inspection failed due to critical mechanical issues.";
            request.setNotes(request.getNotes() == null || request.getNotes().isEmpty() ? failureNote : request.getNotes() + " " + failureNote);
        }

        // 2. Calculate Scores (if not already done or if backend should override)
        // Only calculate if the inspection wasn't already rejected by mechanical failure
        if (request.getInspectionStatus() != CarInspectionReqRes.InspectionStatus.Rejected) {
            int calculatedBodyScore = calculateBodyScore(request.getBody());
            int calculatedInteriorScore = calculateInteriorScore(request.getInterior());
            request.setBodyScore(calculatedBodyScore);
            request.setInteriorScore(calculatedInteriorScore);

            // 3. Determine Status based on Scores/Other factors (Example)
            if (calculatedBodyScore < 70 || calculatedInteriorScore < 70) { // Example threshold
                log.info("Scores below threshold for plate {}. Setting status to Rejected.", request.getPlateNumber());
                request.setInspectionStatus(CarInspectionReqRes.InspectionStatus.Rejected);
                // Add notes if needed
            }
            // Add more complex logic for ConditionallyApproved etc. if needed
        } else {
            // If already rejected by mechanical, ensure scores reflect failure (e.g., set to 0)
            log.debug("Inspection already rejected mechanically, setting scores to 0 for plate {}", request.getPlateNumber());
            request.setBodyScore(0);
            request.setInteriorScore(0);
        }

        // 4. Determine Service Status based on Inspection Status
        if (request.getInspectionStatus() == CarInspectionReqRes.InspectionStatus.Approved) {
            request.setServiceStatus(CarInspectionReqRes.ServiceStatus.Ready);
        } else if (request.getInspectionStatus() == CarInspectionReqRes.InspectionStatus.ConditionallyApproved) {
            request.setServiceStatus(CarInspectionReqRes.ServiceStatus.ReadyWithWarning); // Or MaintenanceRequired?
        } else { // Rejected
            request.setServiceStatus(CarInspectionReqRes.ServiceStatus.Pending); // Or MaintenanceRequired?
        }

    }

    // --- Placeholder: Check if mechanical inspection passes ---
    private boolean checkMechanicalPass(MechanicalInspectionDTO mechanical) {
        // Define your critical failure criteria here
        return mechanical.isEngineCondition() &&
                mechanical.isEnginePower() &&
                mechanical.isSuspension() &&
                mechanical.isBrakes() &&
                mechanical.isSteering() && // Example critical items
                mechanical.isGearbox();
    }

    // --- Placeholder: Calculate Body Score ---
    private int calculateBodyScore(BodyInspectionDTO bodyDetails) {
        if (bodyDetails == null) return 0;
        int score = 100;
        score -= calculatePointsDeducted(bodyDetails.getBodyCollision());
        score -= calculatePointsDeducted(bodyDetails.getBodyScratches());
        score -= calculatePointsDeducted(bodyDetails.getPaintCondition());
        score -= calculatePointsDeducted(bodyDetails.getBreakages());
        score -= calculatePointsDeducted(bodyDetails.getCracks());
        return Math.max(0, score);
    }

    // --- Placeholder: Calculate Interior Score ---
    private int calculateInteriorScore(InteriorInspectionDTO interiorDetails) {
        if (interiorDetails == null) return 0;
        int score = 100;
        // Add deductions based on interior item conditions similar to body score
        score -= calculatePointsDeducted(interiorDetails.getSeatFabric());
        score -= calculatePointsDeducted(interiorDetails.getInteriorRoof());
        // ... add deductions for all relevant interior items ...
        return Math.max(0, score);
    }

    // --- Placeholder: Calculate points deducted for an item ---
    // --- Placeholder: Calculate points deducted for an item ---
    // --- Placeholder: Calculate points deducted for an item ---
    private int calculatePointsDeducted(ItemConditionDTO condition) {
        if (condition == null || !condition.getProblem()) {
            return 0;
        }
        // Define your scoring rules based on severity
        // Ensure ItemConditionDTO.Severity enum exists with these values
        switch (condition.getSeverity()) {
            // !!! THESE NEED TO MATCH YOUR ACTUAL ENUM VALUES !!!
            case HIGH: // Example: If you use HIGH instead of MAJOR
                return 20;
            case MEDIUM: // Example: If you use MEDIUM instead of MINOR
                return 10;
            case LOW: // Example: If you have a LOW severity
                return 5;
            case NONE: // Fallthrough if problem=true but severity=NONE
            default:
                // Decide default deduction if severity is NONE or unexpected
                log.warn("Unexpected or NONE severity '{}' found for a problem item. Applying default deduction.", condition.getSeverity());
                return 5;
        }
    }


    // --- Helper method to update Car status ---
    @Transactional // Make this transactional as it modifies the Car
    protected void updateCarStatusBasedOnInspection(CarInspection inspection) {
        if (inspection == null || inspection.getCar() == null) {
            log.warn("Cannot update car status - inspection or associated car is null.");
            return;
        }

        Car car = inspection.getCar();
        String currentCarStatus = car.getStatus();
        String newCarStatus = currentCarStatus; // Default to current status

        String inspectionStatusStr = inspection.getInspectionStatus(); // Status is stored as String

        // Determine the new status based on the inspection outcome
        if (CarInspectionReqRes.InspectionStatus.Approved.name().equals(inspectionStatusStr)) {
            newCarStatus = CAR_STATUS_INSPECTED_READY;
        } else if (CarInspectionReqRes.InspectionStatus.Rejected.name().equals(inspectionStatusStr)) {
            newCarStatus = CAR_STATUS_INSPECTION_REJECTED;
        } else if (CarInspectionReqRes.InspectionStatus.ConditionallyApproved.name().equals(inspectionStatusStr)) {
            // Decide what status ConditionallyApproved maps to for the Car
            // Maybe it's still 'Ready' or a specific 'NeedsAttention' status?
            newCarStatus = CAR_STATUS_INSPECTED_READY; // Example: Treat as ready for now
        } else {
            log.warn("Unknown inspection status '{}' found for inspection ID {}. Car status not updated.", inspectionStatusStr, inspection.getId());
            // Optionally set to a default/unknown status if needed
            // newCarStatus = CAR_STATUS_UNKNOWN;
        }

        // Update the car status only if it has changed
        if (!Objects.equals(currentCarStatus, newCarStatus)) {
            log.info("Updating status for car plate {} from '{}' to '{}'", car.getPlateNumber(), currentCarStatus, newCarStatus);
            car.setStatus(newCarStatus);
            try {
                carRepository.save(car); // Save the updated car
                log.info("Successfully updated status for car plate {}", car.getPlateNumber());
            } catch (DataAccessException e) {
                log.error("Database error updating status for car plate {}: {}", car.getPlateNumber(), e.getMessage(), e);
                // Consider how to handle this failure - maybe throw a runtime exception?
                // For now, just logging the error. The transaction might roll back depending on configuration.
            } catch (Exception e) {
                log.error("Unexpected error updating status for car plate {}: {}", car.getPlateNumber(), e.getMessage(), e);
            }
        } else {
            log.debug("Car status ('{}') for plate {} remains unchanged after inspection ID {}.", currentCarStatus, car.getPlateNumber(), inspection.getId());
        }
    }


    @Transactional(readOnly = true)
    public CarInspectionListResponse getAllInspections() {
        List<CarInspection> inspections;
        try {
            inspections = inspectionRepository.findAll();
            log.info("Retrieved {} inspections.", inspections.size());
        } catch (DataAccessException e) {
            log.error("Database error retrieving all car inspections: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving inspections: Database error.", e);
        } catch (Exception e) {
            log.error("Unexpected error retrieving all car inspections: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving inspections: Unexpected error.", e);
        }

        List<CarInspectionReqRes> inspectionDTOs = inspections.stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());

        CarInspectionListResponse response = new CarInspectionListResponse();
        response.setInspections(inspectionDTOs);
        response.setCodStatus(200);
        response.setMessage("Inspections retrieved successfully");
        return response;
    }

    @Transactional(readOnly = true)
    public CarInspectionReqRes getInspectionById(Long id) {
        try {
            CarInspection inspection = inspectionRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inspection with id " + id + " not found"));

            log.debug("Retrieved inspection with id: {}", id);

            CarInspectionReqRes response = mapEntityToResponse(inspection);
            response.setCodStatus(200);
            response.setMessage("Inspection retrieved successfully");
            return response;

        } catch (ResponseStatusException e) {
            log.warn("Failed to find inspection with id {}: {}", id, e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            log.error("Database error retrieving inspection with id {}: {}", id, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving inspection", e);
        } catch (Exception e) {
            log.error("Unexpected error retrieving inspection with id {}: {}", id, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving inspection", e);
        }
    }

    @Transactional
    public CarInspectionReqRes updateInspection(Long id, CarInspectionReqRes request) {
        try {
            CarInspection existingInspection = inspectionRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inspection with id " + id + " not found for update"));

            // --- Apply Inspection Logic (e.g., Auto-Reject, Score Calculation) ---
            applyInspectionBusinessLogic(request); // Apply rules before updating

            // Update existing entity fields from the (potentially modified) request
            updateExistingEntityFromRequest(existingInspection, request);

            CarInspection savedInspection = inspectionRepository.save(existingInspection);
            log.info("Successfully updated inspection with id: {}", id);

            // --- Update Car Status based on the saved inspection ---
            updateCarStatusBasedOnInspection(savedInspection);

            CarInspectionReqRes response = mapEntityToResponse(savedInspection);
            response.setCodStatus(200);
            response.setMessage("Inspection updated successfully");
            return response;

        } catch (ResponseStatusException e) {
            log.warn("Failed to update inspection with id {}: {}", id, e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            log.error("Database error updating inspection with id {}: {}", id, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating inspection", e);
        } catch (Exception e) {
            log.error("Unexpected error updating inspection with id {}: {}", id, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating inspection", e);
        }
    }

    @Transactional
    public void deleteInspection(Long id) {
        try {
            if (!inspectionRepository.existsById(id)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Inspection with id " + id + " not found for deletion.");
            }
            // --- Consider consequences of deleting an inspection ---
            // Does the associated Car status need to be reverted?
            // Optional<CarInspection> inspectionOpt = inspectionRepository.findById(id); // Fetch before delete if needed

            inspectionRepository.deleteById(id);
            log.info("Successfully deleted inspection with id: {}", id);

            // If needed, update car status after deletion (e.g., back to PendingInspection)
            // inspectionOpt.ifPresent(inspection -> {
            //     if (inspection.getCar() != null) {
            //         Car car = inspection.getCar();
            //         // Logic to determine the new status after deletion
            //         car.setStatus(CAR_STATUS_PENDING_INSPECTION);
            //         try {
            //              carRepository.save(car);
            //              log.info("Reverted status for car plate {} to PendingInspection after deleting inspection {}", car.getPlateNumber(), id);
            //         } catch (Exception e) {
            //              log.error("Failed to revert status for car plate {} after deleting inspection {}", car.getPlateNumber(), id, e);
            //         }
            //     }
            // });

        } catch (ResponseStatusException e) {
            log.warn("Failed to delete inspection with id {}: {}", id, e.getMessage());
            throw e;
        } catch (DataAccessException e) {
            log.error("Database error deleting inspection with id {}: {}", id, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting inspection, potentially due to related data.", e);
        } catch (Exception e) {
            log.error("Unexpected error deleting inspection with id {}: {}", id, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting inspection", e);
        }
    }

    @Transactional(readOnly = true)
    public CarInspectionListResponse getInspectionsByPlateNumber(String plateNumber) {
        if (plateNumber == null || plateNumber.trim().isEmpty()) {
            log.warn("Attempted to get inspections with null or empty plate number.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plate number cannot be empty");
        }

        List<CarInspection> inspections;
        try {
            // Assuming findByPlateNumber exists in CarInspectionRepository
            // If not, you might need findByCarPlateNumber
            // Inside getInspectionsByPlateNumber method
            inspections = inspectionRepository.findByCar_PlateNumber(plateNumber);
            // Example if searching via Car relationship
            log.info("Retrieved {} inspections for plate number: {}", inspections.size(), plateNumber);
        } catch (DataAccessException e) {
            log.error("Database error retrieving inspections for plate number {}: {}", plateNumber, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving inspections by plate number", e);
        } catch (Exception e) {
            log.error("Unexpected error retrieving inspections for plate number {}: {}", plateNumber, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving inspections by plate number", e);
        }

        List<CarInspectionReqRes> inspectionDTOs = inspections.stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());

        CarInspectionListResponse response = new CarInspectionListResponse();
        response.setInspections(inspectionDTOs);
        response.setCodStatus(200);
        response.setMessage("Inspections retrieved successfully for plate number: " + plateNumber);
        return response;
    }

    // ==================================================
    // --- Helper Methods for Manual Mapping ---
    // --- Consider using MapStruct to replace these ---
    // ==================================================

    private CarInspection mapRequestToEntity(CarInspectionReqRes request) {
        if (request == null) return null;

        CarInspection inspection = new CarInspection();
        // Don't set plate number here, it's derived from the Car entity relationship
        inspection.setInspectionDate(request.getInspectionDate());
        inspection.setInspectorName(request.getInspectorName());

        // Map Enums to String for storage (assuming entity fields are String)
        inspection.setInspectionStatus(request.getInspectionStatus() != null ? request.getInspectionStatus().name() : null);
        inspection.setServiceStatus(request.getServiceStatus() != null ? request.getServiceStatus().name() : null);

        inspection.setBodyScore(request.getBodyScore());
        inspection.setInteriorScore(request.getInteriorScore());
        inspection.setNotes(request.getNotes());

        // Map nested objects safely
        if (request.getMechanical() != null) {
            inspection.setMechanical(mapMechanicalDTOtoEntity(request.getMechanical()));
            if (inspection.getMechanical() != null) inspection.getMechanical().setCarInspection(inspection); // Set back-reference if needed
        }
        if (request.getBody() != null) {
            inspection.setBody(mapBodyDTOtoEntity(request.getBody()));
            if (inspection.getBody() != null) inspection.getBody().setCarInspection(inspection); // Set back-reference if needed
        }
        if (request.getInterior() != null) {
            inspection.setInterior(mapInteriorDTOtoEntity(request.getInterior()));
            if (inspection.getInterior() != null) inspection.getInterior().setCarInspection(inspection); // Set back-reference if needed
        }
        return inspection;
    }

    private void updateExistingEntityFromRequest(CarInspection existingInspection, CarInspectionReqRes request) {
        if (request == null || existingInspection == null) {
            log.warn("Attempted to update inspection with null request or entity.");
            return;
        }

        existingInspection.setInspectionDate(request.getInspectionDate());
        existingInspection.setInspectorName(request.getInspectorName());

        // Map Enums to String for storage
        existingInspection.setInspectionStatus(request.getInspectionStatus() != null ? request.getInspectionStatus().name() : null);
        existingInspection.setServiceStatus(request.getServiceStatus() != null ? request.getServiceStatus().name() : null);

        existingInspection.setBodyScore(request.getBodyScore());
        existingInspection.setInteriorScore(request.getInteriorScore());
        existingInspection.setNotes(request.getNotes());

        // Update nested objects
        if (request.getMechanical() != null) {
            if (existingInspection.getMechanical() == null) {
                existingInspection.setMechanical(new MechanicalInspection());
                existingInspection.getMechanical().setCarInspection(existingInspection); // Set back-reference
            }
            updateMechanicalFromDTO(existingInspection.getMechanical(), request.getMechanical());
        } // Decide on else block: clear or ignore

        if (request.getBody() != null) {
            if (existingInspection.getBody() == null) {
                existingInspection.setBody(new BodyInspection());
                existingInspection.getBody().setCarInspection(existingInspection); // Set back-reference
            }
            updateBodyFromDTO(existingInspection.getBody(), request.getBody());
        } // Decide on else block: clear or ignore

        if (request.getInterior() != null) {
            if (existingInspection.getInterior() == null) {
                existingInspection.setInterior(new InteriorInspection());
                existingInspection.getInterior().setCarInspection(existingInspection); // Set back-reference
            }
            updateInteriorFromDTO(existingInspection.getInterior(), request.getInterior());
        } // Decide on else block: clear or ignore
    }


    // --- Mapping helpers for nested objects (DTO -> Entity) ---

    private MechanicalInspection mapMechanicalDTOtoEntity(MechanicalInspectionDTO dto) {
        if (dto == null) return null;
        MechanicalInspection entity = new MechanicalInspection();
        updateMechanicalFromDTO(entity, dto);
        return entity;
    }

    private void updateMechanicalFromDTO(MechanicalInspection entity, MechanicalInspectionDTO dto) {
        // Assuming DTO uses standard boolean getters (isEngineCondition, etc.)
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

    private BodyInspection mapBodyDTOtoEntity(BodyInspectionDTO dto) {
        if (dto == null) return null;
        BodyInspection entity = new BodyInspection();
        updateBodyFromDTO(entity, dto);
        return entity;
    }

    private void updateBodyFromDTO(BodyInspection entity, BodyInspectionDTO dto) {
        // Assuming ItemCondition is @Embeddable or requires manual mapping
        entity.setBodyCollision(mapItemConditionDTO(dto.getBodyCollision()));
        entity.setBodyScratches(mapItemConditionDTO(dto.getBodyScratches()));
        entity.setPaintCondition(mapItemConditionDTO(dto.getPaintCondition()));
        entity.setBreakages(mapItemConditionDTO(dto.getBreakages()));
        entity.setCracks(mapItemConditionDTO(dto.getCracks()));
    }

    private InteriorInspection mapInteriorDTOtoEntity(InteriorInspectionDTO dto) {
        if (dto == null) return null;
        InteriorInspection entity = new InteriorInspection();
        updateInteriorFromDTO(entity, dto);
        return entity;
    }

    private void updateInteriorFromDTO(InteriorInspection entity, InteriorInspectionDTO dto) {
        // Assuming ItemCondition is @Embeddable or requires manual mapping
        entity.setEngineExhaust(mapItemConditionDTO(dto.getEngineExhaust()));
        entity.setSeatComfort(mapItemConditionDTO(dto.getSeatComfort()));
        entity.setSeatFabric(mapItemConditionDTO(dto.getSeatFabric()));
        entity.setFloorMat(mapItemConditionDTO(dto.getFloorMat()));
        entity.setRearViewMirror(mapItemConditionDTO(dto.getRearViewMirror()));
        entity.setCarTab(mapItemConditionDTO(dto.getCarTab()));
        entity.setMirrorAdjustment(mapItemConditionDTO(dto.getMirrorAdjustment()));
        entity.setDoorLock(mapItemConditionDTO(dto.getDoorLock()));
        entity.setVentilationSystem(mapItemConditionDTO(dto.getVentilationSystem()));
        entity.setDashboardDecoration(mapItemConditionDTO(dto.getDashboardDecoration()));
        entity.setSeatBelt(mapItemConditionDTO(dto.getSeatBelt()));
        entity.setSunshade(mapItemConditionDTO(dto.getSunshade()));
        entity.setWindowCurtain(mapItemConditionDTO(dto.getWindowCurtain()));
        entity.setInteriorRoof(mapItemConditionDTO(dto.getInteriorRoof()));
        entity.setCarIgnition(mapItemConditionDTO(dto.getCarIgnition()));
        entity.setFuelConsumption(mapItemConditionDTO(dto.getFuelConsumption()));
        entity.setHeadlights(mapItemConditionDTO(dto.getHeadlights()));
        entity.setRainWiper(mapItemConditionDTO(dto.getRainWiper()));
        entity.setTurnSignalLight(mapItemConditionDTO(dto.getTurnSignalLight()));
        entity.setBrakeLight(mapItemConditionDTO(dto.getBrakeLight()));
        entity.setLicensePlateLight(mapItemConditionDTO(dto.getLicensePlateLight()));
        entity.setClock(mapItemConditionDTO(dto.getClock()));
        entity.setRpm(mapItemConditionDTO(dto.getRpm()));
        entity.setBatteryStatus(mapItemConditionDTO(dto.getBatteryStatus()));
        entity.setChargingIndicator(mapItemConditionDTO(dto.getChargingIndicator()));
    }


    private ItemCondition mapItemConditionDTO(ItemConditionDTO dto) {
        if (dto == null) return null;
        ItemCondition entity = new ItemCondition();
        entity.setProblem(dto.getProblem()); // Assuming DTO getter is getProblem()
        entity.setSeverity(dto.getSeverity() != null ? dto.getSeverity().name() : null);
        entity.setNotes(dto.getNotes());
        return entity;
    }

    // --- Mapping helpers for nested objects (Entity -> DTO) ---

    private CarInspectionReqRes mapEntityToResponse(CarInspection inspection) {
        if (inspection == null) return null;

        CarInspectionReqRes response = new CarInspectionReqRes();
        response.setId(inspection.getId());
        // Safely get plate number
        response.setPlateNumber(inspection.getCar() != null ? inspection.getCar().getPlateNumber() : null); // Prefer getting from related Car
        response.setInspectionDate(inspection.getInspectionDate());
        response.setInspectorName(inspection.getInspectorName());

        // Safer String to Enum mapping
        try {
            response.setInspectionStatus(inspection.getInspectionStatus() != null ? CarInspectionReqRes.InspectionStatus.valueOf(inspection.getInspectionStatus()) : null);
        } catch (IllegalArgumentException e) {
            log.warn("Could not map inspectionStatus value '{}' from entity ID {} to DTO enum", inspection.getInspectionStatus(), inspection.getId(), e);
            response.setInspectionStatus(null);
        }
        try {
            response.setServiceStatus(inspection.getServiceStatus() != null ? CarInspectionReqRes.ServiceStatus.valueOf(inspection.getServiceStatus()) : null);
        } catch (IllegalArgumentException e) {
            log.warn("Could not map serviceStatus value '{}' from entity ID {} to DTO enum", inspection.getServiceStatus(), inspection.getId(), e);
            response.setServiceStatus(null);
        }

        response.setBodyScore(inspection.getBodyScore());
        response.setInteriorScore(inspection.getInteriorScore());
        response.setNotes(inspection.getNotes());

        // Map nested objects safely
        response.setMechanical(mapMechanicalEntityToDTO(inspection.getMechanical()));
        response.setBody(mapBodyEntityToDTO(inspection.getBody()));
        response.setInterior(mapInteriorEntityToDTO(inspection.getInterior()));

        return response;
    }

    private MechanicalInspectionDTO mapMechanicalEntityToDTO(MechanicalInspection entity) {
        if (entity == null) return null;
        MechanicalInspectionDTO dto = new MechanicalInspectionDTO();
        // Assuming entity uses standard boolean getters (isEngineCondition, etc.)
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

    private BodyInspectionDTO mapBodyEntityToDTO(BodyInspection entity) {
        if (entity == null) return null;
        BodyInspectionDTO dto = new BodyInspectionDTO();
        dto.setBodyCollision(mapItemConditionEntity(entity.getBodyCollision()));
        dto.setBodyScratches(mapItemConditionEntity(entity.getBodyScratches()));
        dto.setPaintCondition(mapItemConditionEntity(entity.getPaintCondition()));
        dto.setBreakages(mapItemConditionEntity(entity.getBreakages()));
        dto.setCracks(mapItemConditionEntity(entity.getCracks()));
        return dto;
    }

    private InteriorInspectionDTO mapInteriorEntityToDTO(InteriorInspection entity) {
        if (entity == null) return null;
        InteriorInspectionDTO dto = new InteriorInspectionDTO();
        dto.setEngineExhaust(mapItemConditionEntity(entity.getEngineExhaust()));
        dto.setSeatComfort(mapItemConditionEntity(entity.getSeatComfort()));
        dto.setSeatFabric(mapItemConditionEntity(entity.getSeatFabric()));
        dto.setFloorMat(mapItemConditionEntity(entity.getFloorMat()));
        dto.setRearViewMirror(mapItemConditionEntity(entity.getRearViewMirror()));
        dto.setCarTab(mapItemConditionEntity(entity.getCarTab()));
        dto.setMirrorAdjustment(mapItemConditionEntity(entity.getMirrorAdjustment()));
        dto.setDoorLock(mapItemConditionEntity(entity.getDoorLock()));
        dto.setVentilationSystem(mapItemConditionEntity(entity.getVentilationSystem()));
        dto.setDashboardDecoration(mapItemConditionEntity(entity.getDashboardDecoration()));
        dto.setSeatBelt(mapItemConditionEntity(entity.getSeatBelt()));
        dto.setSunshade(mapItemConditionEntity(entity.getSunshade()));
        dto.setWindowCurtain(mapItemConditionEntity(entity.getWindowCurtain()));
        dto.setInteriorRoof(mapItemConditionEntity(entity.getInteriorRoof()));
        dto.setCarIgnition(mapItemConditionEntity(entity.getCarIgnition()));
        dto.setFuelConsumption(mapItemConditionEntity(entity.getFuelConsumption()));
        dto.setHeadlights(mapItemConditionEntity(entity.getHeadlights()));
        dto.setRainWiper(mapItemConditionEntity(entity.getRainWiper()));
        dto.setTurnSignalLight(mapItemConditionEntity(entity.getTurnSignalLight()));
        dto.setBrakeLight(mapItemConditionEntity(entity.getBrakeLight()));
        dto.setLicensePlateLight(mapItemConditionEntity(entity.getLicensePlateLight()));
        dto.setClock(mapItemConditionEntity(entity.getClock()));
        dto.setRpm(mapItemConditionEntity(entity.getRpm()));
        dto.setBatteryStatus(mapItemConditionEntity(entity.getBatteryStatus()));
        dto.setChargingIndicator(mapItemConditionEntity(entity.getChargingIndicator()));
        return dto;
    }

    private ItemConditionDTO mapItemConditionEntity(ItemCondition entity) {
        if (entity == null) return null;
        ItemConditionDTO dto = new ItemConditionDTO();
        dto.setProblem(entity.isProblem()); // Assuming entity uses standard boolean getter
        // Safer String to Enum mapping
        try {
            dto.setSeverity(entity.getSeverity() != null ? ItemConditionDTO.Severity.valueOf(entity.getSeverity()) : null);
        } catch (IllegalArgumentException e) {
            log.warn("Could not map severity value '{}' from ItemCondition to DTO enum", entity.getSeverity(), e);
            dto.setSeverity(null);
        }
        dto.setNotes(entity.getNotes());
        return dto;
    }
}