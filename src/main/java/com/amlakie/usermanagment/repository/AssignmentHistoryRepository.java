package com.amlakie.usermanagment.repository;

import com.amlakie.usermanagment.entity.AssignmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AssignmentHistoryRepository extends JpaRepository<AssignmentHistory, Long> {

    // Existing method
    List<AssignmentHistory> findByStatusIn(List<String> statuses);

    // New methods for license expiry checks
    @Query("SELECT a FROM AssignmentHistory a WHERE a.licenseExpiryDate BETWEEN :startDate AND :endDate")
    List<AssignmentHistory> findByLicenseExpiryDateBetween(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);

    @Query("SELECT a FROM AssignmentHistory a WHERE a.licenseExpiryDate < :currentDate")
    List<AssignmentHistory> findByLicenseExpiryDateBefore(
            @Param("currentDate") String currentDate);

    // Find assignments with expiring licenses (within 5 days)
    @Query("SELECT a FROM AssignmentHistory a WHERE a.licenseExpiryDate BETWEEN :today AND :warningDate")
    List<AssignmentHistory> findExpiringLicenses(
            @Param("today") String today,
            @Param("warningDate") String warningDate);

    // Find assignments with expired licenses
    @Query("SELECT a FROM AssignmentHistory a WHERE a.licenseExpiryDate < :today")
    List<AssignmentHistory> findExpiredLicenses(
            @Param("today") String today);

    // Find assignments by requester name containing (for search functionality)
    List<AssignmentHistory> findByRequesterNameContainingIgnoreCase(String requesterName);

    // Find assignments by plate number containing (for search functionality)
    @Query("SELECT a FROM AssignmentHistory a WHERE a.allPlateNumbers LIKE %:plateNumber%")
    List<AssignmentHistory> findByPlateNumberContaining(
            @Param("plateNumber") String plateNumber);

    // Find assignments by status and expiring soon
    @Query("SELECT a FROM AssignmentHistory a WHERE a.status IN :statuses AND a.licenseExpiryDate BETWEEN :today AND :warningDate")
    List<AssignmentHistory> findByStatusInAndLicenseExpiryDateBetween(
            @Param("statuses") List<String> statuses,
            @Param("today") String today,
            @Param("warningDate") String warningDate);

    // Find assignments by status and expired
    @Query("SELECT a FROM AssignmentHistory a WHERE a.status IN :statuses AND a.licenseExpiryDate < :today")
    List<AssignmentHistory> findByStatusInAndLicenseExpiryDateBefore(
            @Param("statuses") List<String> statuses,
            @Param("today") String today);
}