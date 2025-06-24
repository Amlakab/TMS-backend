package com.amlakie.usermanagment.repository; // Adjust package

import com.amlakie.usermanagment.entity.maintainance.MaintenanceRecord; // Adjust package
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long> {
    // Custom query methods can be added here if needed
}