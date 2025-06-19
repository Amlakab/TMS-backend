package com.amlakie.usermanagment.repository;

import com.amlakie.usermanagment.entity.RouteHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RouteHistoryRepository extends JpaRepository<RouteHistory, Long> {

    // Example query: Find history for a specific car
    @Query("SELECT rh FROM RouteHistory rh JOIN FETCH rh.organizationCar oc LEFT JOIN FETCH rh.waypoints WHERE oc.id = :carId ORDER BY rh.assignmentStartDate DESC")
    List<RouteHistory> findByOrganizationCarIdWithDetails(@Param("carId") Long carId);

    // Example query: Find history for a specific car within a date range
    @Query("SELECT rh FROM RouteHistory rh JOIN FETCH rh.organizationCar oc LEFT JOIN FETCH rh.waypoints " +
            "WHERE oc.id = :carId AND rh.assignmentStartDate >= :startDate AND rh.assignmentStartDate <= :endDate " +
            "ORDER BY rh.assignmentStartDate DESC")
    List<RouteHistory> findByOrganizationCarIdAndDateRangeWithDetails(
            @Param("carId") Long carId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // Find all history records with details (useful for general reports, but be mindful of data size)
    @Query("SELECT rh FROM RouteHistory rh JOIN FETCH rh.organizationCar LEFT JOIN FETCH rh.waypoints ORDER BY rh.assignmentStartDate DESC")
    List<RouteHistory> findAllWithDetails();
}