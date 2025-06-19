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
    private final EmailSentService emailSentService; // Inject EmailSentService
    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository,
                           OrganizationCarRepository organizationCarRepository,
                           EmailSentService emailSentService) { // Add EmailSentService to constructor
        this.employeeRepository = employeeRepository;
        this.organizationCarRepository = organizationCarRepository;
        this.emailSentService = emailSentService; // Initialize EmailSentService
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

        try {
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
                // Call the injected emailSentService
                emailSentService.sendSimpleMessage(updatedEmployee.getEmail(), subject, text);
                // EmailSentService logs success/failure of sending
            } else {
                log.warn("Cannot send assignment email: Employee {} (ID: {}) has no email address or email is empty.", updatedEmployee.getName(), updatedEmployee.getEmployeeId());
            }

            return convertToResponseDTO(updatedEmployee);
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            log.error("Validation or entity not found error during assignment for employee ID {}: {}", request.getEmployeeId(), e.getMessage());
            throw e; // Re-throw to be handled by controller advice or default error handling
        }
        catch (Exception e) {
            log.error("Unexpected error during assignment for employee ID {}: ", request.getEmployeeId(), e);
            throw new RuntimeException("Assignment failed due to an unexpected error.", e);
        }
    }
}