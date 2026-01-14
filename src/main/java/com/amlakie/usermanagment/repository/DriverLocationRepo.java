package com.amlakie.usermanagment.repository;

import com.amlakie.usermanagment.entity.DriverLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DriverLocationRepo extends JpaRepository<DriverLocation, Long> {
    Optional<DriverLocation> findByDriverEmail(String driverEmail);
    Optional<DriverLocation> findByPlateNumber(String plateNumber);
    Optional<DriverLocation> findByImei(String imei);
    boolean existsByDriverEmail(String driverEmail);
}