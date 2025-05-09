package com.amlakie.usermanagment.service;

import com.amlakie.usermanagment.dto.AssignmentRequest;
import com.amlakie.usermanagment.dto.CarReqRes;
import com.amlakie.usermanagment.entity.AssignmentHistory;
import com.amlakie.usermanagment.entity.Car;
import com.amlakie.usermanagment.entity.CarInspection; // Import CarInspection
import com.amlakie.usermanagment.entity.RentCar;
import com.amlakie.usermanagment.entity.TravelRequest;
import com.amlakie.usermanagment.repository.AssignmentHistoryRepository;
import com.amlakie.usermanagment.repository.CarInspectionRepository; // Import CarInspectionRepository
import com.amlakie.usermanagment.repository.CarRepository;
import com.amlakie.usermanagment.repository.RentCarRepository;
import org.slf4j.Logger; // Added for logging
import org.slf4j.LoggerFactory; // Added for logging
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException; // For specific FK constraint errors
import org.springframework.http.HttpStatus; // For ResponseStatusException
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // For @Transactional
import org.springframework.web.server.ResponseStatusException; // For better error responses

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CarManagementService {

    private static final Logger log = LoggerFactory.getLogger(CarManagementService.class); // Logger for this class

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private RentCarRepository rentCarRepository;

    @Autowired
    private AssignmentHistoryRepository assignmentHistoryRepository;

    @Autowired
    private CarInspectionRepository carInspectionRepository; // Inject CarInspectionRepository

    public CarReqRes registerCar(CarReqRes registrationRequest) {
        CarReqRes response = new CarReqRes();
        try {
            Car car = new Car();
            // Consider using a mapper (e.g., MapStruct) for DTO to Entity conversion
            car.setPlateNumber(registrationRequest.getPlateNumber());
            car.setOwnerName(registrationRequest.getOwnerName());
            car.setOwnerPhone(registrationRequest.getOwnerPhone());
            car.setModel(registrationRequest.getModel());
            car.setCarType(registrationRequest.getCarType());
            if (registrationRequest.getManufactureYear() != null && !registrationRequest.getManufactureYear().isBlank()) {
                car.setManufactureYear(Integer.parseInt(registrationRequest.getManufactureYear()));
            }
            car.setMotorCapacity(registrationRequest.getMotorCapacity());
            if (registrationRequest.getKmPerLiter() != null && !registrationRequest.getKmPerLiter().isBlank()) {
                car.setKmPerLiter(Float.parseFloat(registrationRequest.getKmPerLiter()));
            }
            car.setTotalKm(registrationRequest.getTotalKm());
            car.setFuelType(registrationRequest.getFuelType());
            car.setStatus(registrationRequest.getStatus() != null ? registrationRequest.getStatus() : "Pending"); // Default status
            car.setParkingLocation(registrationRequest.getParkingLocation());
            car.setInspected(false); // Default inspected status
            car.setRegisteredDate(LocalDateTime.now()); // Set registration date

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
                car.setCreatedBy(authentication.getName());
            } else {
                car.setCreatedBy("SYSTEM_ANONYMOUS"); // Or handle as an error if user must be authenticated
            }


            Car savedCar = carRepository.save(car);
            response.setCar(mapCarToCarReqRes(savedCar).getCar()); // Map entity back to DTO for response
            response.setMessage("Car Registered Successfully");
            response.setCodStatus(HttpStatus.CREATED.value()); // Use HttpStatus for status codes
        } catch (NumberFormatException e) {
            log.error("Error parsing number in car registration: {}", e.getMessage());
            response.setCodStatus(HttpStatus.BAD_REQUEST.value());
            response.setError("Invalid number format for year or km/liter: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error during car registration: {}", e.getMessage(), e); // Log the full exception
            response.setCodStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setError("An unexpected error occurred during car registration: " + e.getMessage());
        }
        return response;
    }

    public CarReqRes getAllCars() {
        CarReqRes response = new CarReqRes();
        try {
            List<Car> cars = carRepository.findAll();
            // It's better to return a list of DTOs rather than entities directly
            // For now, keeping it as is to match existing structure, but consider mapping
            response.setCarList(cars);
            response.setCodStatus(HttpStatus.OK.value());
            response.setMessage("All cars retrieved successfully");
        } catch (Exception e) {
            log.error("Error retrieving all cars: {}", e.getMessage(), e);
            response.setCodStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setError("An unexpected error occurred: " + e.getMessage());
        }
        return response;
    }

    public CarReqRes getCarById(Long id) {
        CarReqRes response = new CarReqRes();
        try {
            Optional<Car> carOptional = carRepository.findById(id);
            if (carOptional.isPresent()) {
                // Map entity to DTO for response
                response.setCar(mapCarToCarReqRes(carOptional.get()).getCar());
                response.setCodStatus(HttpStatus.OK.value());
                response.setMessage("Car retrieved successfully");
            } else {
                response.setCodStatus(HttpStatus.NOT_FOUND.value());
                response.setMessage("Car not found with id: " + id);
            }
        } catch (Exception e) {
            log.error("Error retrieving car by id {}: {}", id, e.getMessage(), e);
            response.setCodStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setError("An unexpected error occurred: " + e.getMessage());
        }
        return response;
    }

    @Transactional // Ensure update is atomic
    public CarReqRes updateCar(Long id, CarReqRes updateRequest) {
        CarReqRes response = new CarReqRes();
        try {
            Car existingCar = carRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found with id: " + id));

            // Update fields (consider a mapper here too)
            existingCar.setPlateNumber(updateRequest.getPlateNumber());
            existingCar.setOwnerName(updateRequest.getOwnerName());
            existingCar.setOwnerPhone(updateRequest.getOwnerPhone());
            existingCar.setModel(updateRequest.getModel());
            existingCar.setCarType(updateRequest.getCarType());
            if (updateRequest.getManufactureYear() != null && !updateRequest.getManufactureYear().isBlank()) {
                existingCar.setManufactureYear(Integer.parseInt(updateRequest.getManufactureYear()));
            }
            existingCar.setMotorCapacity(updateRequest.getMotorCapacity());
            if (updateRequest.getKmPerLiter() != null && !updateRequest.getKmPerLiter().isBlank()) {
                existingCar.setKmPerLiter(Float.parseFloat(updateRequest.getKmPerLiter()));
            }
            existingCar.setTotalKm(updateRequest.getTotalKm());
            existingCar.setFuelType(updateRequest.getFuelType());
            existingCar.setStatus(updateRequest.getStatus());
            existingCar.setParkingLocation(updateRequest.getParkingLocation());
            // existingCar.setInspected(updateRequest.isInspected()); // If you want to update this here
            // existingCar.setLatestInspectionId(updateRequest.getLatestInspectionId()); // If you want to update this here


            Car updatedCar = carRepository.save(existingCar);
            response.setCar(mapCarToCarReqRes(updatedCar).getCar());
            response.setCodStatus(HttpStatus.OK.value());
            response.setMessage("Car updated successfully");

        } catch (ResponseStatusException e) { // Catch specific exception for not found
            log.warn("Update failed: {}", e.getMessage());
            response.setCodStatus(e.getStatusCode().value());
            response.setMessage(e.getReason());
        } catch (NumberFormatException e) {
            log.error("Error parsing number in car update: {}", e.getMessage());
            response.setCodStatus(HttpStatus.BAD_REQUEST.value());
            response.setError("Invalid number format for year or km/liter: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error updating car with id {}: {}", id, e.getMessage(), e);
            response.setCodStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setError("An unexpected error occurred during car update: " + e.getMessage());
        }
        return response;
    }

    @Transactional // This operation modifies data and should be transactional
    public CarReqRes deleteCar(Long id) {
        CarReqRes response = new CarReqRes();
        try {
            // 1. Fetch the car to ensure it exists and to get its details if needed
            Car carToDelete = carRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Car with ID {} not found for deletion.", id);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found with id: " + id);
                    });

            // 2. Find and delete associated CarInspections
            //    Using the car object (carToDelete) is more robust for querying related entities.
            //    You'll need a method in CarInspectionRepository like: List<CarInspection> findByCar(Car car);
            //    For now, using plate number as per existing CarInspectionRepository method.
            List<CarInspection> inspections = carInspectionRepository.findByCar_PlateNumber(carToDelete.getPlateNumber());
            if (!inspections.isEmpty()) {
                log.info("Deleting {} associated inspections for car ID: {}", inspections.size(), id);
                carInspectionRepository.deleteAllInBatch(inspections); // More efficient for multiple deletions
            } else {
                log.info("No associated inspections found for car ID: {}", id);
            }

            // 3. Delete associated AssignmentHistories (if a car can be in multiple, or just the one it's linked to)
            //    This depends on your business logic. If a car can only be in one active assignment,
            //    you might fetch by car. If it can be in many historical ones, you might need to iterate.
            //    For simplicity, let's assume we delete all history linked to this car.
            //    You'd need a method like: List<AssignmentHistory> findByCar(Car car); in AssignmentHistoryRepository
            List<AssignmentHistory> assignments = assignmentHistoryRepository.findByCar(carToDelete); // Assuming this method exists
            if (!assignments.isEmpty()) {
                log.info("Deleting {} associated assignment histories for car ID: {}", assignments.size(), id);
                assignmentHistoryRepository.deleteAllInBatch(assignments);
            } else {
                log.info("No associated assignment histories found for car ID: {}", id);
            }


            // 4. Now delete the car itself
            carRepository.delete(carToDelete);
            log.info("Successfully deleted car with ID: {}", id);

            response.setCodStatus(HttpStatus.OK.value());
            response.setMessage("Car and associated records deleted successfully");

        } catch (ResponseStatusException e) { // Catch specific exception for not found
            log.warn("Deletion failed: {}", e.getMessage());
            response.setCodStatus(e.getStatusCode().value());
            response.setMessage(e.getReason());
        } catch (DataIntegrityViolationException e) { // Catch foreign key constraint violations specifically
            log.error("Data integrity violation while deleting car ID {}: {}. This might indicate other related data still exists.", id, e.getMessage(), e);
            response.setCodStatus(HttpStatus.CONFLICT.value()); // 409 Conflict
            response.setError("Cannot delete car. It is still referenced by other records (e.g., rent_cars or other unhandled relations). " + e.getMostSpecificCause().getMessage());
        } catch (Exception e) {
            log.error("Error deleting car with id {}: {}", id, e.getMessage(), e);
            response.setCodStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setError("An unexpected error occurred during car deletion: " + e.getMessage());
        }
        return response;
    }

    public CarReqRes searchCars(String query) {
        CarReqRes response = new CarReqRes();
        try {
            List<Car> cars = carRepository.findByPlateNumberContainingOrOwnerNameContainingOrModelContaining(
                    query, query, query);
            response.setCarList(cars); // Consider mapping to DTOs
            response.setCodStatus(HttpStatus.OK.value());
            response.setMessage("Search results retrieved successfully");
        } catch (Exception e) {
            log.error("Error searching cars with query '{}': {}", query, e.getMessage(), e);
            response.setCodStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setError("An unexpected error occurred during search: " + e.getMessage());
        }
        return response;
    }

    @Transactional // This operation modifies data
    public CarReqRes updateStatus(String plateNumber, CarReqRes updateRequest) {
        CarReqRes response = new CarReqRes();
        try {
            Car existingCar = carRepository.findByPlateNumber(plateNumber)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found with plate number: " + plateNumber));

            existingCar.setStatus(updateRequest.getStatus());
            // If you also want to update 'inspected' and 'latestInspectionId' via this endpoint:
            // existingCar.setInspected(updateRequest.isInspected());
            // existingCar.setLatestInspectionId(updateRequest.getLatestInspectionId());

            Car updatedCar = carRepository.save(existingCar);
            response.setCar(mapCarToCarReqRes(updatedCar).getCar());
            response.setCodStatus(HttpStatus.OK.value());
            response.setMessage("Car status updated successfully");

        } catch (ResponseStatusException e) {
            log.warn("Update status failed: {}", e.getMessage());
            response.setCodStatus(e.getStatusCode().value());
            response.setMessage(e.getReason());
        } catch (Exception e) {
            log.error("Error updating status for car with plate number {}: {}", plateNumber, e.getMessage(), e);
            response.setCodStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setError("An unexpected error occurred: " + e.getMessage());
        }
        return response;
    }


    public CarReqRes getApprovedCars() {
        CarReqRes response = new CarReqRes();
        try {
            List<Car> cars = carRepository.findByStatus("Approved");
            response.setCarList(cars); // Consider mapping to DTOs
            response.setCodStatus(HttpStatus.OK.value());
            response.setMessage("Approved cars retrieved successfully");
        } catch (Exception e) {
            log.error("Error retrieving approved cars: {}", e.getMessage(), e);
            response.setCodStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setError("An unexpected error occurred: " + e.getMessage());
        }
        return response;
    }

    @Transactional // This operation modifies data
    public CarReqRes createAssignment(AssignmentRequest request) {
        CarReqRes response = new CarReqRes();
        try {
            LocalDateTime requestDateTime = null;
            if (request.getRequestDate() != null && !request.getRequestDate().isBlank()) {
                requestDateTime = LocalDate.parse(request.getRequestDate()).atStartOfDay();
            }


            AssignmentHistory history = new AssignmentHistory();
            history.setRequestLetterNo(request.getRequestLetterNo());
            history.setRequestDate(requestDateTime); // Can be null if not provided
            history.setAssignedDate(requestDateTime); // Should this be LocalDateTime.now() on assignment?
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
            history.setPlateNumber(request.getPlateNumber()); // Plate number of the assigned car
            history.setStatus(request.getStatus() != null ? request.getStatus() : "Pending Assignment"); // Default status

            if (request.getCarId() != null) {
                Car car = carRepository.findById(request.getCarId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found with id: " + request.getCarId()));
                history.setCar(car);
                // Optionally update car status to "Assigned"
                // car.setStatus("Assigned");
                // carRepository.save(car);
            }

            if (request.getRentCarId() != null) {
                RentCar rentCar = rentCarRepository.findById(request.getRentCarId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rent car not found with id: " + request.getRentCarId()));
                history.setCars(rentCar); // Assuming 'cars' is the correct field name in AssignmentHistory for RentCar
            }

            assignmentHistoryRepository.save(history);

            response.setCodStatus(HttpStatus.CREATED.value());
            response.setMessage("Assignment created successfully");
        } catch (ResponseStatusException e) {
            log.warn("Assignment creation failed: {}", e.getMessage());
            response.setCodStatus(e.getStatusCode().value());
            response.setMessage(e.getReason());
        } catch (Exception e) {
            log.error("Error creating assignment: {}", e.getMessage(), e);
            response.setCodStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setError("An unexpected error occurred: " + e.getMessage());
        }
        return response;
    }

    public CarReqRes getAllAssignmentHistories() {
        CarReqRes response = new CarReqRes();
        try {
            List<AssignmentHistory> histories = assignmentHistoryRepository.findAll();
            response.setAssignmentHistoryList(histories); // Consider mapping to DTOs
            response.setCodStatus(HttpStatus.OK.value());
            response.setMessage("All assignment histories retrieved successfully");
        } catch (Exception e) {
            log.error("Error retrieving all assignment histories: {}", e.getMessage(), e);
            response.setCodStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setError("An unexpected error occurred: " + e.getMessage());
        }
        return response;
    }

    public CarReqRes getPendingCars() { // This method name is confusing, it returns AssignmentHistory
        CarReqRes response = new CarReqRes();
        try {
            // Assuming "Not Assigned" is a status for AssignmentHistory, not Car
            List<AssignmentHistory> histories = assignmentHistoryRepository.findByStatus("Not Assigned");
            response.setAssignmentHistoryList(histories); // Consider mapping to DTOs
            response.setCodStatus(HttpStatus.OK.value());
            response.setMessage("Pending assignment histories retrieved successfully");
        } catch (Exception e) {
            log.error("Error retrieving pending assignment histories: {}", e.getMessage(), e);
            response.setCodStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setError("An unexpected error occurred: " + e.getMessage());
        }
        return response;
    }

    public CarReqRes getAssignmentHistoryById(Long id) {
        CarReqRes response = new CarReqRes();
        try {
            Optional<AssignmentHistory> historyOptional = assignmentHistoryRepository.findById(id);
            if (historyOptional.isPresent()) {
                response.setAssignmentHistory(historyOptional.get()); // Consider mapping to DTO
                response.setCodStatus(HttpStatus.OK.value());
                response.setMessage("Assignment history retrieved successfully");
            } else {
                response.setCodStatus(HttpStatus.NOT_FOUND.value());
                response.setMessage("Assignment history not found with id: " + id);
            }
        } catch (Exception e) {
            log.error("Error retrieving assignment history by id {}: {}", id, e.getMessage(), e);
            response.setCodStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setError("An unexpected error occurred: " + e.getMessage());
        }
        return response;
    }

    @Transactional // This operation modifies data
    public CarReqRes updateAssignmentHistory(Long id, AssignmentRequest updateRequest) {
        CarReqRes response = new CarReqRes();
        try {
            AssignmentHistory history = assignmentHistoryRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment history not found with id: " + id));

            // Update history fields
            history.setAssignedDate(LocalDateTime.now()); // Or parse from request if provided
            history.setRentalType(updateRequest.getRentalType());
            history.setPlateNumber(updateRequest.getPlateNumber());
            history.setStatus(updateRequest.getStatus());

            // Potentially release old car and assign new one
            Car oldCar = history.getCar();
            if (oldCar != null && (updateRequest.getCarId() == null || !oldCar.getId().equals(updateRequest.getCarId()))) {
                // If car is being changed or removed, set old car status back to "Approved" or "Available"
                oldCar.setStatus("Approved"); // Or your equivalent available status
                carRepository.save(oldCar);
                history.setCar(null); // Decouple old car
            }


            if (updateRequest.getCarId() != null) {
                Car carToAssign = carRepository.findById(updateRequest.getCarId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found with id: " + updateRequest.getCarId()));
                history.setCar(carToAssign);
                // Optionally update car status to "Assigned"
                // carToAssign.setStatus("Assigned");
                // carRepository.save(carToAssign);
            }

            // Similar logic for RentCar if it needs to be unassigned/reassigned
            // ...

            assignmentHistoryRepository.save(history);

            response.setCodStatus(HttpStatus.OK.value());
            response.setMessage("Assignment history updated successfully");
        } catch (ResponseStatusException e) {
            log.warn("Assignment update failed: {}", e.getMessage());
            response.setCodStatus(e.getStatusCode().value());
            response.setMessage(e.getReason());
        } catch (Exception e) {
            log.error("Error updating assignment history for id {}: {}", id, e.getMessage(), e);
            response.setCodStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setError("An unexpected error occurred: " + e.getMessage());
        }
        return response;
    }

    @Transactional // This operation modifies data
    public CarReqRes deleteAssignmentHistory(Long id) {
        CarReqRes response = new CarReqRes();
        try {
            AssignmentHistory history = assignmentHistoryRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment history not found with id: " + id));

            // Release the associated car, if any
            Car car = history.getCar();
            if (car != null) {
                car.setStatus("Approved"); // Or your equivalent available status
                carRepository.save(car);
                log.info("Car {} status set to Approved after deleting assignment history {}.", car.getPlateNumber(), id);
            }

            // Delete the history
            assignmentHistoryRepository.delete(history);
            log.info("Successfully deleted assignment history with ID: {}", id);

            response.setCodStatus(HttpStatus.OK.value());
            response.setMessage("Assignment history deleted successfully");
        } catch (ResponseStatusException e) {
            log.warn("Assignment deletion failed: {}", e.getMessage());
            response.setCodStatus(e.getStatusCode().value());
            response.setMessage(e.getReason());
        } catch (Exception e) {
            log.error("Error deleting assignment history for id {}: {}", id, e.getMessage(), e);
            response.setCodStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setError("An unexpected error occurred: " + e.getMessage());
        }
        return response;
    }

    // Helper method to map Car entity to CarReqRes (specifically the 'car' field)
    // This is a simplified mapper. Consider using MapStruct for more complex scenarios.
    private CarReqRes mapCarToCarReqRes(Car car) {
        CarReqRes carDto = new CarReqRes();
        if (car != null) {
            // Create a new Car object for the DTO to avoid sending the managed entity directly
            // if CarReqRes.car is also of type Car.
            // If CarReqRes.car is a different DTO type, map accordingly.
            // For simplicity, assuming CarReqRes.setCar(Car car) takes the entity.
            // In a real app, you'd map to a CarDTO.
            Car carData = new Car();
            carData.setId(car.getId());
            carData.setPlateNumber(car.getPlateNumber());
            carData.setOwnerName(car.getOwnerName());
            carData.setOwnerPhone(car.getOwnerPhone());
            carData.setModel(car.getModel());
            carData.setCarType(car.getCarType());
            carData.setManufactureYear(car.getManufactureYear());
            carData.setMotorCapacity(car.getMotorCapacity());
            carData.setKmPerLiter(car.getKmPerLiter());
            carData.setTotalKm(car.getTotalKm());
            carData.setFuelType(car.getFuelType());
            carData.setStatus(car.getStatus());
            carData.setRegisteredDate(car.getRegisteredDate());
            carData.setParkingLocation(car.getParkingLocation());
            carData.setCreatedBy(car.getCreatedBy());
            carData.setInspected(car.isInspected());
            carData.setLatestInspectionId(car.getLatestInspectionId());
            carDto.setCar(carData);
        }
        return carDto;
    }
}