package com.amlakie.usermanagment.repository;

import com.amlakie.usermanagment.entity.MaintenanceRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, Long> {

    List<MaintenanceRequest> findByReportingDriver(String driverName);
    List<MaintenanceRequest> findByStatus(MaintenanceRequest.RequestStatus status);
    Optional<MaintenanceRequest> findFirstByPlateNumberOrderByCreatedAtDesc(String plateNumber);
    List<MaintenanceRequest> findByReportingDriverAndStatus(String driverName, MaintenanceRequest.RequestStatus status);
}