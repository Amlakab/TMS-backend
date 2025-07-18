package com.amlakie.usermanagment.controller;

import com.amlakie.usermanagment.dto.employee.TransferEmployeesRequestDTO;
import com.amlakie.usermanagment.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final EmployeeService employeeService;

    @Autowired
    public AssignmentController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping(value = "/transfer-all", produces = "text/plain")
    public ResponseEntity<String> transferAllEmployees(@RequestBody TransferEmployeesRequestDTO requestDTO) {
        employeeService.transferAllEmployees(requestDTO);
        return ResponseEntity.ok("All employees were transferred successfully.");
    }
}