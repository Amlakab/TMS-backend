package com.amlakie.usermanagment.repository;

import com.amlakie.usermanagment.entity.VehicleLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VehicleLocationRepository extends JpaRepository<VehicleLocation, Long> {

    @Query("SELECT vl FROM VehicleLocation vl WHERE " +
            "vl.vehicleId = :vehicleId AND vl.vehicleType = :vehicleType " +
            "ORDER BY vl.timestamp DESC")
    List<VehicleLocation> findByVehicle(@Param("vehicleId") Long vehicleId,
                                        @Param("vehicleType") String vehicleType);

    @Query("SELECT vl FROM VehicleLocation vl WHERE " +
            "vl.vehicleId = :vehicleId AND vl.vehicleType = :vehicleType " +
            "AND vl.timestamp >= :start AND vl.timestamp <= :end " +
            "ORDER BY vl.timestamp DESC")
    List<VehicleLocation> findByVehicleAndTimestampBetween(
            @Param("vehicleId") Long vehicleId,
            @Param("vehicleType") String vehicleType,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT vl FROM VehicleLocation vl WHERE " +
            "vl.deviceImei = :imei " +
            "ORDER BY vl.timestamp DESC LIMIT 1")
    VehicleLocation findLatestByImei(@Param("imei") String imei);

    @Query("SELECT vl FROM VehicleLocation vl WHERE " +
            "vl.vehicleType = :vehicleType " +
            "AND vl.timestamp = (SELECT MAX(vl2.timestamp) FROM VehicleLocation vl2 " +
            "WHERE vl2.vehicleId = vl.vehicleId AND vl2.vehicleType = vl.vehicleType) " +
            "ORDER BY vl.timestamp DESC")
    List<VehicleLocation> findLatestLocationsByType(@Param("vehicleType") String vehicleType);

    @Query("SELECT vl FROM VehicleLocation vl WHERE " +
            "vl.timestamp = (SELECT MAX(vl2.timestamp) FROM VehicleLocation vl2 " +
            "WHERE vl2.vehicleId = vl.vehicleId AND vl2.vehicleType = vl.vehicleType) " +
            "ORDER BY vl.timestamp DESC")
    List<VehicleLocation> findAllLatestLocations();
}