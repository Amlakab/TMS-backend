package com.amlakie.usermanagment.repository;

import com.amlakie.usermanagment.entity.VehicleAcceptance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VehicleAcceptanceRepository extends JpaRepository<VehicleAcceptance, Long> {
    Optional<VehicleAcceptance> findTopByAssignmentHistoryIdOrderByCreatedAtDesc(Long assignmentHistoryId);

    // For getting all acceptances ordered by date
    List<VehicleAcceptance> findByAssignmentHistoryIdOrderByCreatedAtDesc(Long assignmentHistoryId);
    List<VehicleAcceptance> findByPlateNumberContainingIgnoreCase(String plateNumber);
    // For getting all acceptances with pagination
    Page<VehicleAcceptance> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // For getting all acceptances without pagination
    List<VehicleAcceptance> findAllByOrderByCreatedAtDesc();

    // Plate number queries
    Optional<VehicleAcceptance> findTopByPlateNumberOrderByCreatedAtDesc(String plateNumber);
    List<VehicleAcceptance> findByPlateNumberOrderByCreatedAtDesc(String plateNumber);
}