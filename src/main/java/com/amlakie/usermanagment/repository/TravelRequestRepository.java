package com.amlakie.usermanagment.repository;

import com.amlakie.usermanagment.entity.TravelRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TravelRequestRepository extends JpaRepository<TravelRequest, Long> {
    List<TravelRequest> findByTravelerNameContainingOrTravelReasonContaining(String travelerName, String travelReason);
    List<TravelRequest> findByDepartment(String department);
    List<TravelRequest> findByJobStatus(String jobStatus);
}