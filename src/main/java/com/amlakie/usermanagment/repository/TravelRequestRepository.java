// Create this new file: D:/my git projects/TMS-backend/src/main/java/com/amlakie/usermanagment/repository/TravelRequestRepository.java
package com.amlakie.usermanagment.repository;

import com.amlakie.usermanagment.entity.TravelRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TravelRequestRepository extends JpaRepository<TravelRequest, Long> {

    List<TravelRequest> findByStatus(TravelRequest.RequestStatus status);

    List<TravelRequest> findByAssignedDriverAndStatus(String assignedDriver, TravelRequest.RequestStatus status);
}