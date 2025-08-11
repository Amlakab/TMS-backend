package com.amlakie.usermanagment.repository;

import com.amlakie.usermanagment.entity.RentalMaintenanceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RentalMaintenanceRequestRepository extends JpaRepository<RentalMaintenanceRequest, Long> {
    List<RentalMaintenanceRequest> findByRentalCarId(Long rentalCarId);
    List<RentalMaintenanceRequest> findByCarId(Long carId);
    List<RentalMaintenanceRequest> findByStatus(String status);
}