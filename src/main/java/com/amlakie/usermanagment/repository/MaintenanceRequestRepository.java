package com.amlakie.usermanagment.repository;

import com.amlakie.usermanagment.entity.MaintenanceRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, Long> {

    List<MaintenanceRequest> findByReportingDriver(String driverName);
    List<MaintenanceRequest> findByStatus(MaintenanceRequest.RequestStatus status);
    List<MaintenanceRequest> findByStatusIn(List<MaintenanceRequest.RequestStatus> statuses);
    List<MaintenanceRequest> findByReportingDriverAndStatus(String driverName, MaintenanceRequest.RequestStatus status);
}