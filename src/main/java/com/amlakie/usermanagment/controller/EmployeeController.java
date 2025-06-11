package com.amlakie.usermanagment.controller;

import com.amlakie.usermanagment.dto.employee.AssignCarToEmployeeRequestDTO;
import com.amlakie.usermanagment.dto.employee.EmployeeResponseDTO;
import com.amlakie.usermanagment.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // Endpoint to get an employee by their ID (for the search bar)
    @GetMapping("/{employeeId}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(@PathVariable String employeeId) {
        EmployeeResponseDTO employee = employeeService.getEmployeeById(employeeId);
        return ResponseEntity.ok(employee);
    }

    // Endpoint to assign a car and village to an employee
    @PostMapping("/assign-car")
    public ResponseEntity<EmployeeResponseDTO> assignCarAndVillage(
            @RequestBody AssignCarToEmployeeRequestDTO requestDTO) {
        EmployeeResponseDTO updatedEmployee = employeeService.assignCarAndVillageToEmployee(requestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(updatedEmployee); // Explicitly return 200 OK
    }

    // Optional: Endpoint to create a new employee
    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> createEmployee(@RequestBody EmployeeResponseDTO employeeDTO) {
        EmployeeResponseDTO createdEmployee = employeeService.createEmployee(employeeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployee);
    }

    // Optional: Endpoint to get all employees
    @GetMapping
    public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployees() {
        List<EmployeeResponseDTO> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }
}