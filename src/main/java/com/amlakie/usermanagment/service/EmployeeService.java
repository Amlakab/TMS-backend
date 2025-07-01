package com.amlakie.usermanagment.service;

import com.amlakie.usermanagment.dto.employee.AssignCarToEmployeeRequestDTO;
import com.amlakie.usermanagment.dto.employee.EmployeeResponseDTO;
import com.amlakie.usermanagment.entity.Employee;
import com.amlakie.usermanagment.entity.OrganizationCar;
import com.amlakie.usermanagment.repository.EmployeeRepository;
import com.amlakie.usermanagment.repository.OrganizationCarRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final OrganizationCarRepository organizationCarRepository;
    private final EmailSentService emailSentService;
    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository,
                           OrganizationCarRepository organizationCarRepository,
                           EmailSentService emailSentService) {
        this.employeeRepository = employeeRepository;
        this.organizationCarRepository = organizationCarRepository;
        this.emailSentService = emailSentService;
    }

    @Transactional(readOnly = true)
    public EmployeeResponseDTO getEmployeeById(String employeeId) {
        Employee employee = employeeRepository.findByEmployeeIdWithAssignedCar(employeeId)
                .orElseThrow(() -> {
                    log.error("Employee not found with ID: {}", employeeId);
                    return new EntityNotFoundException("Employee not found with ID: " + employeeId);
                });
        return convertToResponseDTO(employee);
    }

    @Transactional
    public EmployeeResponseDTO createEmployee(EmployeeResponseDTO employeeDTO) {
        if(employeeRepository.findByEmployeeId(employeeDTO.getEmployeeId()).isPresent()){
            log.warn("Attempt to create employee with existing ID: {}", employeeDTO.getEmployeeId());
            throw new IllegalArgumentException("Employee with ID " + employeeDTO.getEmployeeId() + " already exists.");
        }
        Employee employee = new Employee();
        employee.setEmployeeId(employeeDTO.getEmployeeId());
        employee.setName(employeeDTO.getName());
        employee.setEmail(employeeDTO.getEmail());
        employee.setDepartment(employeeDTO.getDepartment());
        Employee savedEmployee = employeeRepository.save(employee);
        log.info("Created new employee: {}", savedEmployee.getEmployeeId());
        return convertToResponseDTO(savedEmployee);
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponseDTO> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    private EmployeeResponseDTO convertToResponseDTO(Employee employee) {
        if (employee == null) {
            return null;
        }
        String carPlateNumber = null;
        Long carId = null;
        if (employee.getAssignedCar() != null) {
            carPlateNumber = employee.getAssignedCar().getPlateNumber();
            carId = employee.getAssignedCar().getId();
        }
        return new EmployeeResponseDTO(
                employee.getEmployeeId(),
                employee.getName(),
                employee.getEmail(),
                employee.getDepartment(),
                employee.getVillage(),
                carPlateNumber,
                carId
        );
    }

    @Transactional
    public EmployeeResponseDTO assignCarAndVillageToEmployee(AssignCarToEmployeeRequestDTO request) {
        log.info("Received assignment request for employee ID '{}' to car plate '{}'", request.getEmployeeId(), request.getCarPlateNumber());

        Employee employee = employeeRepository.findByEmployeeId(request.getEmployeeId())
                .orElseThrow(() -> {
                    log.error("Employee not found for assignment: {}", request.getEmployeeId());
                    return new EntityNotFoundException("Employee not found with ID: " + request.getEmployeeId());
                });
        log.info("Retrieved employee for assignment: {} with email: {}", employee.getName(), employee.getEmail());

        if (request.getCarPlateNumber() == null || request.getCarPlateNumber().trim().isEmpty()) {
            log.error("Car plate number is null or empty in the assignment request for employee ID: {}", request.getEmployeeId());
            throw new IllegalArgumentException("Car plate number cannot be null or empty.");
        }

        OrganizationCar car = organizationCarRepository.findByPlateNumber(request.getCarPlateNumber())
                .orElseThrow(() -> {
                    log.error("Car not found for assignment with plate number: {}", request.getCarPlateNumber());
                    return new EntityNotFoundException("OrganizationCar not found with plate number: " + request.getCarPlateNumber());
                });
        log.info("Retrieved car for assignment: {}", car.getPlateNumber());

        // --- NEW LOGIC START: Car Capacity Check ---

        // Check if the employee is already assigned to this car.
        // If so, we don't need to check capacity for *this* specific assignment,
        // as they are already counted as occupying a seat. This prevents blocking
        // a "re-confirmation" or update of other employee details if the car is full.
        boolean isAlreadyAssignedToThisCar = employee.getAssignedCar() != null && employee.getAssignedCar().equals(car);

        if (!isAlreadyAssignedToThisCar) {
            // 1. Get the car's capacity using the correct getter.
            // This now returns an Integer (or int) directly, which is type-safe.
            Integer carCapacity = car.getLoadCapacity(); // Corrected: using getLoadCapacity()

            // It's good practice to handle the case where capacity might be null or zero.
            if (carCapacity == null || carCapacity <= 0) {
                log.error("Car {} has an invalid capacity of {}. Cannot assign.", car.getPlateNumber(), carCapacity);
                throw new IllegalStateException("Car " + car.getPlateNumber() + " has an invalid capacity configured.");
            }

            // 2. Count current employees assigned to this car.
            // This now works because the method exists in the repository.
            long currentOccupancy = employeeRepository.countByAssignedCar(car);

            log.info("Car {} (Plate: {}) has capacity {} and current occupancy {}. Attempting to assign employee {}.",
                    car.getId(), car.getPlateNumber(), carCapacity, currentOccupancy, employee.getEmployeeId());

            // 3. Check if the car is already full.
            if (currentOccupancy >= carCapacity) {
                log.warn("Attempt to assign employee {} to car {} failed. Car is already full ({} / {}).",
                        employee.getEmployeeId(), car.getPlateNumber(), currentOccupancy, carCapacity);
                throw new IllegalArgumentException("Car " + car.getPlateNumber() + " is already full. Cannot assign more employees.");
            }
        } else {
            log.info("Employee {} is already assigned to car {}. Skipping capacity check for this re-confirmation.",
                    employee.getEmployeeId(), car.getPlateNumber());
        }
        // --- NEW LOGIC END ---

        try {
            // If the employee is already assigned to this exact car, and we've passed the capacity check (or skipped it),
            // we can log and return early if no other changes are needed.
            // This check is now placed after the capacity check to ensure new assignments are always validated.
            if (employee.getAssignedCar() != null && employee.getAssignedCar().equals(car)) {
                log.info("Employee {} is already assigned to car {}. No change needed for car assignment.", employee.getEmployeeId(), car.getPlateNumber());
                // If only village is being updated, proceed. Otherwise, if no change, just return.
                if (employee.getVillage() != null && employee.getVillage().equals(request.getVillageName())) {
                    return convertToResponseDTO(employee); // No change at all
                }
            }

            employee.setVillage(request.getVillageName());
            employee.setAssignedCar(car);
            Employee updatedEmployee = employeeRepository.save(employee);
            log.info("Successfully assigned car {} to employee {}", car.getPlateNumber(), updatedEmployee.getName());

            // Send email notification
            if (updatedEmployee.getEmail() != null && !updatedEmployee.getEmail().isEmpty()) {
                String subject = "Service Car Assignment Update";
                String text = String.format(
                        "Dear %s,\n\nThis is to inform you that you have been assigned a service car." +
                                "\nCar Plate Number: %s\n\nRegards,\nINSA TMS Administration",
                        updatedEmployee.getName(),
                        car.getPlateNumber()
                );
                emailSentService.sendSimpleMessage(updatedEmployee.getEmail(), subject, text);
            } else {
                log.warn("Cannot send assignment email: Employee {} (ID: {}) has no email address or email is empty.", updatedEmployee.getName(), updatedEmployee.getEmployeeId());
            }

            return convertToResponseDTO(updatedEmployee);
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            log.error("Validation or entity not found error during assignment for employee ID {}: {}", request.getEmployeeId(), e.getMessage());
            throw e;
        }
        catch (Exception e) {
            log.error("Unexpected error during assignment for employee ID {}: ", request.getEmployeeId(), e);
            throw new RuntimeException("Assignment failed due to an unexpected error.", e);
        }
    }
}