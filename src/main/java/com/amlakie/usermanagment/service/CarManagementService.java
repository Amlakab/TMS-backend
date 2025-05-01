// C:/TMS-backendd/src/main/java/com/amlakie/usermanagment/service/CarManagementService.java
package com.amlakie.usermanagment.service;

// --- Necessary Imports ---
import com.amlakie.usermanagment.dto.AssignmentRequest;
import com.amlakie.usermanagment.dto.CarDto; // Import the new DTO
import com.amlakie.usermanagment.dto.CarReqRes;
import com.amlakie.usermanagment.entity.AssignmentHistory;
import com.amlakie.usermanagment.entity.Car;
import com.amlakie.usermanagment.entity.CarInspection; // Import CarInspection
// import com.amlakie.usermanagment.entity.TravelRequest; // Import if needed elsewhere, not used here
import com.amlakie.usermanagment.repository.AssignmentHistoryRepository;
import com.amlakie.usermanagment.repository.CarInspectionRepository; // Import repository
import com.amlakie.usermanagment.repository.CarRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException; // For delete constraint handling
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Good practice for methods modifying data

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // Import Collectors

@Service
public class CarManagementService {
    private static final Logger log = LoggerFactory.getLogger(CarManagementService.class);

    @Autowired
    private CarRepository carRepository;

    // --- CORRECTED: Add @Autowired for injection ---
    @Autowired
    private CarInspectionRepository inspectionRepository;

    @Autowired
    private AssignmentHistoryRepository assignmentHistoryRepository;


    // --- HELPER METHOD TO MAP Car entity to CarDto ---
    private CarDto mapCarToCarDto(Car car) {
        if (car == null) {
            return null;
        }
        CarDto dto = new CarDto();
        // --- Map fields from entity to DTO ---
        dto.setId(car.getId());
        dto.setPlateNumber(car.getPlateNumber());
        dto.setOwnerName(car.getOwnerName());
        dto.setOwnerPhone(car.getOwnerPhone());
        dto.setModel(car.getModel());
        dto.setCarType(car.getCarType());
        dto.setManufactureYear(car.getManufactureYear());
        dto.setMotorCapacity(car.getMotorCapacity());
        dto.setKmPerLiter(car.getKmPerLiter());
        dto.setTotalKm(car.getTotalKm());
        dto.setFuelType(car.getFuelType());
        dto.setStatus(car.getStatus());
        dto.setParkingLocation(car.getParkingLocation());
        dto.setInspected(car.isInspected());
        // Map other fields if you added them to CarDto (e.g., registeredDate, createdBy)

        // --- Find and set the latest inspection ID ---
        try {
            // Use the repository method added in Step 4
            Optional<CarInspection> latestInspectionOpt = inspectionRepository
                    .findFirstByCarOrderByInspectionDateDesc(car);

            latestInspectionOpt.ifPresent(inspection -> dto.setLatestInspectionId(inspection.getId()));
            // If no inspection found, latestInspectionId remains null (which is correct)
        } catch (Exception e) {
            // Log error but don't fail the whole mapping if inspection lookup fails
            log.error("Error finding latest inspection for car plate {}: {}", car.getPlateNumber(), e.getMessage());
            // latestInspectionId will remain null
        }

        return dto;
    }
    // --- END OF HELPER METHOD ---


    // --- MODIFIED: registerCar ---
    @Transactional // Add transactional annotation
    public CarReqRes registerCar(CarReqRes registrationRequest) {
        CarReqRes response = new CarReqRes();
        try {
            // Consider adding validation for registrationRequest fields here

            Car car = new Car();
            car.setPlateNumber(registrationRequest.getPlateNumber());
            car.setOwnerName(registrationRequest.getOwnerName());
            car.setOwnerPhone(registrationRequest.getOwnerPhone());
            car.setModel(registrationRequest.getModel());
            car.setCarType(registrationRequest.getCarType());
            // Use try-catch for parsing or ensure frontend sends correct types
            car.setManufactureYear(Integer.parseInt(registrationRequest.getManufactureYear()));
            car.setMotorCapacity(registrationRequest.getMotorCapacity());
            car.setKmPerLiter(Float.parseFloat(registrationRequest.getKmPerLiter()));
            car.setTotalKm(registrationRequest.getTotalKm());
            car.setFuelType(registrationRequest.getFuelType());
            // Set default status if not provided
            car.setStatus(registrationRequest.getStatus() != null ? registrationRequest.getStatus() : "Pending");
            car.setParkingLocation(registrationRequest.getParkingLocation());
            car.setInspected(false); // Explicitly false on creation

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
                car.setCreatedBy(authentication.getName());
            } else {
                car.setCreatedBy("SYSTEM"); // Fallback if no user context
                log.warn("Car registered without authenticated user context. CreatedBy set to SYSTEM.");
            }

            Car savedCar = carRepository.save(car);
            log.info("Successfully registered car with ID: {}", savedCar.getId());

            CarDto carDto = mapCarToCarDto(savedCar); // Map to DTO
            response.setCar(carDto); // Set DTO in response
            response.setMessage("Car Registered Successfully");
            response.setCodStatus(200);

        } catch (NumberFormatException e) {
            response.setCodStatus(400); // Bad Request for parsing errors
            response.setError("Invalid number format for year or km/liter: " + e.getMessage());
            log.error("Number format error during car registration: {}", e.getMessage());
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError("An unexpected error occurred during registration: " + e.getMessage());
            log.error("Error registering car: {}", e.getMessage(), e); // Log full stack trace
        }
        return response;
    }

    // --- MODIFIED: getAllCars ---
    public CarReqRes getAllCars() {
        CarReqRes response = new CarReqRes();
        try {
            List<Car> cars = carRepository.findAll(); // 1. Fetch entities

            // 2. Map entities to DTOs using the helper method
            List<CarDto> carDtos = cars.stream()
                    .map(this::mapCarToCarDto) // Use the mapping helper
                    .collect(Collectors.toList());

            response.setCarList(carDtos); // 3. Set the list of DTOs
            response.setCodStatus(200);
            response.setMessage("All cars retrieved successfully");
            log.info("Retrieved {} cars.", carDtos.size());

        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError("An unexpected error occurred while retrieving cars: " + e.getMessage());
            log.error("Error fetching all cars: {}", e.getMessage(), e); // Log full stack trace
        }
        return response;
    }

    // --- MODIFIED: getCarById ---
    public CarReqRes getCarById(Long id) {
        CarReqRes response = new CarReqRes();
        try {
            Optional<Car> carOptional = carRepository.findById(id);
            if (carOptional.isPresent()) {
                Car car = carOptional.get();
                CarDto carDto = mapCarToCarDto(car); // Map to DTO
                response.setCar(carDto); // Set DTO in response
                response.setCodStatus(200);
                response.setMessage("Car retrieved successfully");
                log.debug("Retrieved car by ID: {}", id);
            } else {
                response.setCodStatus(404);
                response.setMessage("Car not found with ID: " + id);
                log.warn("Car not found with ID: {}", id);
            }
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError("An unexpected error occurred retrieving car by ID: " + e.getMessage());
            log.error("Error fetching car by ID {}: {}", id, e.getMessage(), e); // Log full stack trace
        }
        return response;
    }

    // --- MODIFIED: updateCar ---
    @Transactional // Add transactional annotation
    public CarReqRes updateCar(Long id, CarReqRes updateRequest) {
        CarReqRes response = new CarReqRes();
        try {
            // Validate updateRequest fields if necessary

            Optional<Car> carOptional = carRepository.findById(id);
            if (carOptional.isPresent()) {
                Car existingCar = carOptional.get();

                // --- Update fields ---
                existingCar.setPlateNumber(updateRequest.getPlateNumber());
                existingCar.setOwnerName(updateRequest.getOwnerName());
                existingCar.setOwnerPhone(updateRequest.getOwnerPhone());
                existingCar.setModel(updateRequest.getModel());
                existingCar.setCarType(updateRequest.getCarType());
                existingCar.setManufactureYear(Integer.parseInt(updateRequest.getManufactureYear()));
                existingCar.setMotorCapacity(updateRequest.getMotorCapacity());
                existingCar.setKmPerLiter(Float.parseFloat(updateRequest.getKmPerLiter()));
                existingCar.setTotalKm(updateRequest.getTotalKm());
                existingCar.setFuelType(updateRequest.getFuelType());
                existingCar.setStatus(updateRequest.getStatus());
                existingCar.setParkingLocation(updateRequest.getParkingLocation());
                // Decide if updating should change 'inspected' status based on your rules
                // existingCar.setInspected(updateRequest.isInspected()); // If DTO included it

                Car updatedCar = carRepository.save(existingCar);
                log.info("Successfully updated car with ID: {}", id);

                CarDto carDto = mapCarToCarDto(updatedCar); // Map updated entity to DTO
                response.setCar(carDto); // Set DTO in response
                response.setCodStatus(200);
                response.setMessage("Car updated successfully");

            } else {
                response.setCodStatus(404);
                response.setMessage("Car not found with ID: " + id + " for update.");
                log.warn("Car not found with ID: {} for update.", id);
            }
        } catch (NumberFormatException e) {
            response.setCodStatus(400); // Bad Request for parsing errors
            response.setError("Invalid number format for year or km/liter: " + e.getMessage());
            log.error("Number format error during car update for ID {}: {}", id, e.getMessage());
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError("An unexpected error occurred during car update: " + e.getMessage());
            log.error("Error updating car ID {}: {}", id, e.getMessage(), e); // Log full stack trace
        }
        return response;
    }

    // --- MODIFIED: deleteCar ---
    @Transactional // Add transactional annotation
    public CarReqRes deleteCar(Long id) {
        CarReqRes response = new CarReqRes();
        try {
            if (carRepository.existsById(id)) {
                // --- IMPORTANT: Consider related data ---
                // Before deleting the car, you might need to delete or unlink related records
                // depending on your database constraints (ON DELETE RESTRICT/NO ACTION).
                // Example (Uncomment and adapt if needed):
                // log.info("Checking/Deleting related inspections for car ID: {}", id);
                // inspectionRepository.deleteByCarId(id); // Requires custom method in repo
                // log.info("Checking/Deleting related assignment history for car ID: {}", id);
                // assignmentHistoryRepository.deleteByCarId(id); // Requires custom method in repo

                carRepository.deleteById(id);
                response.setCodStatus(200);
                response.setMessage("Car deleted successfully"); // Corrected typo
                log.info("Deleted car with ID: {}", id);
            } else {
                response.setCodStatus(404);
                response.setMessage("Car not found for deletion with ID: " + id);
                log.warn("Attempted to delete non-existent car with ID: {}", id);
            }
        } catch (DataIntegrityViolationException e) {
            // Catch specific constraint violation exception (e.g., foreign key)
            response.setCodStatus(409); // 409 Conflict is appropriate
            response.setError("Cannot delete car: It is referenced by other records (e.g., inspections, assignments). Please remove references first.");
            log.error("Constraint violation while deleting car ID {}: {}", id, e.getMessage()); // Log only message for constraint violation usually
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError("An unexpected error occurred during deletion: " + e.getMessage());
            log.error("Error deleting car ID {}: {}", id, e.getMessage(), e); // Log full stack trace
        }
        return response;
    }

    // --- MODIFIED: searchCars ---
    public CarReqRes searchCars(String query) {
        CarReqRes response = new CarReqRes();
        try {
            List<Car> cars = carRepository.findByPlateNumberContainingOrOwnerNameContainingOrModelContaining(
                    query, query, query); // 1. Fetch entities

            // 2. Map entities to DTOs using the helper method
            List<CarDto> carDtos = cars.stream()
                    .map(this::mapCarToCarDto) // Use the mapping helper
                    .collect(Collectors.toList());

            response.setCarList(carDtos); // 3. Set the list of DTOs
            response.setCodStatus(200);
            response.setMessage("Search results retrieved successfully");
            log.info("Found {} cars matching query '{}'.", carDtos.size(), query);

        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError("An unexpected error occurred during search: " + e.getMessage());
            log.error("Error searching cars with query '{}': {}", query, e.getMessage(), e); // Log full stack trace
        }
        return response;
    }

    // --- MODIFIED: updateStatus ---
    @Transactional // Add transactional annotation
    public CarReqRes updateStatus(String plateNumber, CarReqRes updateRequest) {
        CarReqRes response = new CarReqRes();
        try {
            if (updateRequest.getStatus() == null || updateRequest.getStatus().trim().isEmpty()) {
                response.setCodStatus(400);
                response.setError("New status cannot be empty.");
                return response;
            }

            Optional<Car> carOptional = carRepository.findByPlateNumber(plateNumber);
            if (carOptional.isPresent()) {
                Car existingCar = carOptional.get();
                String oldStatus = existingCar.getStatus();
                existingCar.setStatus(updateRequest.getStatus());
                // Decide if changing status should affect 'inspected' flag based on your rules

                Car updatedCar = carRepository.save(existingCar);
                log.info("Successfully updated status for car plate {} from '{}' to '{}'", plateNumber, oldStatus, updatedCar.getStatus());

                CarDto carDto = mapCarToCarDto(updatedCar); // Map updated entity to DTO
                response.setCar(carDto); // Set DTO in response
                response.setCodStatus(200);
                response.setMessage("Car status updated successfully");

            } else {
                response.setCodStatus(404);
                response.setMessage("Car not found with plate number: " + plateNumber);
                log.warn("Car not found with plate number: {} for status update.", plateNumber);
            }
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError("An unexpected error occurred updating status: " + e.getMessage());
            log.error("Error updating status for car plate {}: {}", plateNumber, e.getMessage(), e); // Log full stack trace
        }
        return response;
    }


    // --- MODIFIED: getApprovedCars ---
    public CarReqRes getApprovedCars() {
        CarReqRes response = new CarReqRes();
        try {
            // Consider using a constant for "Approved" status
            List<Car> cars = carRepository.findByStatus("Approved");

            // Map to DTOs
            List<CarDto> carDtos = cars.stream()
                    .map(this::mapCarToCarDto)
                    .collect(Collectors.toList());

            response.setCarList(carDtos); // Return DTOs
            response.setCodStatus(200);
            response.setMessage("Approved cars retrieved successfully");
            log.info("Retrieved {} approved cars.", carDtos.size());

        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError("An unexpected error occurred retrieving approved cars: " + e.getMessage());
            log.error("Error fetching approved cars: {}", e.getMessage(), e); // Log full stack trace
        }
        return response;
    }

    // --- MODIFIED: createAssignment ---
    @Transactional // Add transactional annotation
    public CarReqRes createAssignment(AssignmentRequest request) {
        CarReqRes response = new CarReqRes();
        try {
            // --- Add Validation ---
            if (request == null || request.getCarId() == null) {
                response.setCodStatus(400);
                response.setError("Car ID is required for assignment.");
                return response;
            }
            // Add more validation for other required fields in AssignmentRequest if needed

            // Fetch car first to ensure it exists
            Car car = carRepository.findById(request.getCarId())
                    .orElseThrow(() -> new RuntimeException("Car not found with ID: " + request.getCarId())); // Use specific exception?

            // Optional: Check if car is already assigned or in a suitable status
            // if ("Assigned".equals(car.getStatus())) {
            //     throw new IllegalStateException("Car with ID " + request.getCarId() + " is already assigned.");
            // }
            // if (!"Approved".equals(car.getStatus()) && !"InspectedAndReady".equals(car.getStatus())) { // Example check
            //     throw new IllegalStateException("Car with ID " + request.getCarId() + " is not in an assignable status ('" + car.getStatus() + "').");
            // }

            // Create assignment history
            AssignmentHistory history = new AssignmentHistory();
            history.setRequestLetterNo(request.getRequestLetterNo());
            history.setRequestDate(request.getRequestDate());
            history.setRequesterName(request.getRequesterName());
            history.setRentalType(request.getRentalType());
            history.setPosition(request.getPosition());
            history.setDepartment(request.getDepartment());
            history.setPhoneNumber(request.getPhoneNumber());
            history.setTravelWorkPercentage(request.getTravelWorkPercentage());
            history.setShortNoticePercentage(request.getShortNoticePercentage());
            history.setMobilityIssue(request.getMobilityIssue());
            history.setGender(request.getGender());
            history.setTotalPercentage(request.getTotalPercentage());
            history.setCar(car); // Link to the fetched car

            assignmentHistoryRepository.save(history);
            log.info("Created assignment history ID {} for car ID {}", history.getId(), car.getId());

            // Update car status - Consider using a constant for "Assigned"
            String oldStatus = car.getStatus();
            car.setStatus("Assigned");
            carRepository.save(car);
            log.info("Updated status to Assigned for car ID {} (was '{}')", car.getId(), oldStatus);

            response.setCodStatus(200);
            response.setMessage("Assignment created successfully");

        } catch (RuntimeException e) { // Catch specific exceptions like RuntimeException from orElseThrow
            // Check if message indicates "Car not found"
            if (e.getMessage() != null && e.getMessage().startsWith("Car not found")) {
                response.setCodStatus(404); // Not Found if car doesn't exist
            } else {
                response.setCodStatus(500); // Treat other runtime exceptions as internal errors
            }
            response.setError(e.getMessage());
            log.warn("Failed to create assignment: {}", e.getMessage()); // Log as warning or error depending on exception type
            // } catch (IllegalStateException e) { // Catch status check exception if you add it
            //     response.setCodStatus(409); // Conflict if already assigned or wrong status
            //     response.setError(e.getMessage());
            //     log.warn("Failed to create assignment: {}", e.getMessage());
        } catch (Exception e) {
            response.setCodStatus(500);
            response.setError("An unexpected error occurred during assignment: " + e.getMessage());
            log.error("Error creating assignment: {}", e.getMessage(), e); // Log full stack trace
        }
        return response;
    }
}