package com.amlakie.usermanagment.service;

import com.amlakie.usermanagment.dto.employee.AssignCarToEmployeeRequestDTO;
import com.amlakie.usermanagment.dto.employee.EmployeeResponseDTO;
import com.amlakie.usermanagment.entity.Employee;
import com.amlakie.usermanagment.entity.OrganizationCar;
import com.amlakie.usermanagment.entity.RentCar; // Import RentCar
import com.amlakie.usermanagment.repository.EmployeeRepository;
import com.amlakie.usermanagment.repository.OrganizationCarRepository;
import com.amlakie.usermanagment.repository.RentCarRepository; // Import RentCarRepository
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final OrganizationCarRepository organizationCarRepository;
    private final RentCarRepository rentCarRepository; // INJECT RENT CAR REPO
    private final EmailSentService emailSentService;
    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository,
                           OrganizationCarRepository organizationCarRepository,
                           RentCarRepository rentCarRepository, // ADD TO CONSTRUCTOR
                           EmailSentService emailSentService) {
        this.employeeRepository = employeeRepository;
        this.organizationCarRepository = organizationCarRepository;
        this.rentCarRepository = rentCarRepository; // INITIALIZE
        this.emailSentService = emailSentService;
    }

    @Transactional(readOnly = true)
    public EmployeeResponseDTO getEmployeeById(String employeeId) {
        // This method might need adjustment based on how you fetch assigned rent cars
        // For simplicity, we assume the DTO converter handles it.
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));
        return convertToResponseDTO(employee);
    }

    @Transactional
    public EmployeeResponseDTO createEmployee(EmployeeResponseDTO employeeDTO) {
        if (employeeRepository.findByEmployeeId(employeeDTO.getEmployeeId()).isPresent()) {
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

    /**
     * Converts an Employee entity to a response DTO.
     * This now checks which type of car is assigned and gets the plate number accordingly.
     * Assumes Employee entity has fields: getAssignedCar(), getAssignedRentCar(), and getAssignedCarType().
     */
    private EmployeeResponseDTO convertToResponseDTO(Employee employee) {
        if (employee == null) {
            return null;
        }
        String carPlateNumber = null;
        Long carId = null;
        String carType = employee.getAssignedCarType();

        if ("ORGANIZATION".equals(carType) && employee.getAssignedCar() != null) {
            carPlateNumber = employee.getAssignedCar().getPlateNumber();
            carId = employee.getAssignedCar().getId();
        } else if ("RENT".equals(carType) && employee.getAssignedRentCar() != null) {
            carPlateNumber = employee.getAssignedRentCar().getPlateNumber();
            carId = employee.getAssignedRentCar().getId();
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

    /**
     * Assigns a car (of any type) and village to an employee.
     * This method acts as a dispatcher, calling the appropriate helper based on carType.
     */
    @Transactional
    public EmployeeResponseDTO assignCarAndVillageToEmployee(AssignCarToEmployeeRequestDTO request) {
        log.info("Assignment request for employee ID '{}' to car plate '{}' of type '{}'",
                request.getEmployeeId(), request.getCarPlateNumber(), request.getCarType());

        if (request.getCarType() == null || request.getCarType().trim().isEmpty()) {
            throw new IllegalArgumentException("carType must be specified ('ORGANIZATION' or 'RENT').");
        }
        if (request.getCarPlateNumber() == null || request.getCarPlateNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Car plate number cannot be null or empty.");
        }

        // Step 1: Find the employee
        Employee employee = employeeRepository.findByEmployeeId(request.getEmployeeId())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + request.getEmployeeId()));
        log.info("Retrieved employee for assignment: {}", employee.getName());

        // Step 2: Branch logic based on carType
        String carType = request.getCarType().toUpperCase();
        if ("ORGANIZATION".equals(carType)) {
            assignOrganizationCar(employee, request);
        } else if ("RENT".equals(carType)) {
            assignRentCar(employee, request);
        } else {
            throw new IllegalArgumentException("Invalid carType specified: " + request.getCarType());
        }

        // Step 3: Save and send notification
        Employee updatedEmployee = employeeRepository.save(employee);
        log.info("Successfully assigned car {} to employee {}", request.getCarPlateNumber(), updatedEmployee.getName());

        sendAssignmentNotification(updatedEmployee, request.getCarPlateNumber());

        return convertToResponseDTO(updatedEmployee);
    }

    /**
     * Handles the specific logic for assigning an OrganizationCar.
     */
    private void assignOrganizationCar(Employee employee, AssignCarToEmployeeRequestDTO request) {
        OrganizationCar car = organizationCarRepository.findByPlateNumber(request.getCarPlateNumber())
                .orElseThrow(() -> new EntityNotFoundException("OrganizationCar not found with plate number: " + request.getCarPlateNumber()));
        log.info("Retrieved OrganizationCar for assignment: {}", car.getPlateNumber());

        boolean isAlreadyAssigned = car.equals(employee.getAssignedCar());

        if (!isAlreadyAssigned) {
            long currentOccupancy = employeeRepository.countByAssignedCar(car);
            if (currentOccupancy >= car.getLoadCapacity()) {
                throw new IllegalStateException("Organization Car " + car.getPlateNumber() + " is already full.");
            }
            // Ensure other assignment is cleared before setting the new one
            employee.setAssignedRentCar(null);
            employee.setAssignedCar(car);
            employee.setAssignedCarType("ORGANIZATION");
        }
        employee.setVillage(request.getVillageName());
    }

    /**
     * Handles the specific logic for assigning a RentCar.
     * Assumes RentCar entity has a getLoadCapacity() or similar method.
     */
    private void assignRentCar(Employee employee, AssignCarToEmployeeRequestDTO request) {
        RentCar car = rentCarRepository.findByPlateNumber(request.getCarPlateNumber())
                .orElseThrow(() -> new EntityNotFoundException("RentCar not found with plate number: " + request.getCarPlateNumber()));
        log.info("Retrieved RentCar for assignment: {}", car.getPlateNumber());

        boolean isAlreadyAssigned = car.equals(employee.getAssignedRentCar());

        if (!isAlreadyAssigned) {
            // Assumes RentCar has a compatible method for getting capacity
            long currentOccupancy = employeeRepository.countByAssignedRentCar(car);
            if (currentOccupancy >= car.getNumberOfSeats()) { // Using getNumberOfSeats() as an example for RentCar
                throw new IllegalStateException("Rent Car " + car.getPlateNumber() + " is already full.");
            }
            // Ensure other assignment is cleared before setting the new one
            employee.setAssignedCar(null);
            employee.setAssignedRentCar(car);
            employee.setAssignedCarType("RENT");
        }
        employee.setVillage(request.getVillageName());
    }

    /**
     * Helper method to send email notification after a successful assignment.
     */
    private void sendAssignmentNotification(Employee employee, String plateNumber) {
        if (employee.getEmail() != null && !employee.getEmail().isEmpty()) {
            String subject = "Service Car Assignment Update";
            String text = String.format(
                    "Dear %s,\n\nThis is to inform you that you have been assigned a service car." +
                            "\nCar Plate Number: %s\n\nRegards,\nINSA TMS Administration",
                    employee.getName(),
                    plateNumber
            );
            emailSentService.sendSimpleMessage(employee.getEmail(), subject, text);
        } else {
            log.warn("Cannot send assignment email: Employee {} (ID: {}) has no email address.", employee.getName(), employee.getEmployeeId());
        }
    }
}