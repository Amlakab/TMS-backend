package com.amlakie.usermanagment.repository;

import com.amlakie.usermanagment.entity.CarInspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CarInspectionRepository extends JpaRepository<CarInspection, Long> {
    List<CarInspection> findByPlateNumber(String plateNumber);
    boolean existsByPlateNumber(String plateNumber);
}