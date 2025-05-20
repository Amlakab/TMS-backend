package com.amlakie.usermanagment.repository;

import com.amlakie.usermanagment.entity.Vehicle; // Not directly used in query params now
import com.amlakie.usermanagment.entity.attendance.CarAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CarAttendanceRepository extends JpaRepository<CarAttendance, Long> {

    @Query("SELECT ca FROM CarAttendance ca WHERE ca.vehicle.id = :vehicleId AND ca.vehicleTypeDiscriminator = :vehicleTypeParam AND ca.date = :date AND ca.eveningKm IS NULL")
    Optional<CarAttendance> findByVehicleDetailsAndDateAndEveningKmIsNull(
            @Param("vehicleId") Long vehicleId,
            @Param("vehicleTypeParam") String vehicleType, // Renamed param to avoid conflict if a field was named vehicleType
            @Param("date") LocalDate date
    );

    @Query("SELECT ca FROM CarAttendance ca WHERE ca.vehicle.id = :vehicleId AND ca.vehicleTypeDiscriminator = :vehicleTypeParam AND ca.date < :date AND ca.eveningKm IS NOT NULL ORDER BY ca.date DESC, ca.updatedAt DESC LIMIT 1")
    Optional<CarAttendance> findTopByVehicleDetailsAndDateBeforeOrderByDateDesc(
            @Param("vehicleId") Long vehicleId,
            @Param("vehicleTypeParam") String vehicleType, // Renamed param
            @Param("date") LocalDate date
    );

    @Query("SELECT ca FROM CarAttendance ca WHERE ca.vehicle.id = :vehicleId AND ca.vehicleTypeDiscriminator = :vehicleTypeParam ORDER BY ca.date DESC, ca.createdAt DESC")
    List<CarAttendance> findAllByVehicleDetailsOrderByDateDesc(
            @Param("vehicleId") Long vehicleId,
            @Param("vehicleTypeParam") String vehicleType // Renamed param
    );
    @Query("SELECT ca FROM CarAttendance ca " +
            "WHERE ca.vehicle.id = :vehicleId " +
            "AND ca.vehicleTypeDiscriminator = :vehicleTypeDiscriminator " +
            "AND ca.date < :beforeDate " +
            "AND ca.eveningKm IS NOT NULL " +
            "ORDER BY ca.date DESC, ca.updatedAt DESC")
    List<CarAttendance> findCompletedDeparturesBeforeDateOrdered( // Renamed for clarity
                                                                  @Param("vehicleId") Long vehicleId,
                                                                  @Param("vehicleTypeDiscriminator") String vehicleTypeDiscriminator,
                                                                  @Param("beforeDate") LocalDate beforeDate
    );
    @Query("SELECT ca FROM CarAttendance ca WHERE ca.vehicle.id = :vehicleId AND ca.vehicleTypeDiscriminator = :vehicleTypeDiscriminator AND ca.date = :date")
    Optional<CarAttendance> findByVehicleDetailsAndDate(
            @Param("vehicleId") Long vehicleId,
            @Param("vehicleTypeDiscriminator") String vehicleTypeDiscriminator,
            @Param("date") LocalDate date
    );
}
    