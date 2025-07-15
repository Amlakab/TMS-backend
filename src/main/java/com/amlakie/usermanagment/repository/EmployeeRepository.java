package com.amlakie.usermanagment.repository;

import com.amlakie.usermanagment.entity.Employee;
import com.amlakie.usermanagment.entity.OrganizationCar;
import com.amlakie.usermanagment.entity.RentCar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> { // Primary key is String (employeeId)

    // Find by employeeId (already provided by JpaRepository if PK is employeeId, but good for clarity)
    Optional<Employee> findByEmployeeId(String employeeId);

    // Example: Find employee with their assigned car details eagerly fetched
    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.assignedCar WHERE e.employeeId = :employeeId")
    Optional<Employee> findByEmployeeIdWithAssignedCar(@Param("employeeId") String employeeId);
    List<Employee> findByDepartment(String department);
    Optional<Employee> findByEmail(String email);
    List<Employee> findByVillage(String village);
    long countByAssignedCar(OrganizationCar car);
    long countByAssignedRentCar(RentCar assignedRentCar);
}