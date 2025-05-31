package com.amlakie.usermanagment.repository;

import com.amlakie.usermanagment.entity.Signature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SignatureRepository extends JpaRepository<Signature, Long> {
    void deleteByVehicleAcceptanceId(Long vehicleAcceptanceId);
}