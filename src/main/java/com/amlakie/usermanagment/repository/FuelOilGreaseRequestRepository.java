package com.amlakie.usermanagment.repository;

import com.amlakie.usermanagment.entity.fogrequest.ApprovalStatus;
import com.amlakie.usermanagment.entity.fogrequest.FuelOilGreaseRequest;
import com.amlakie.usermanagment.entity.fogrequest.NezekStatus;
import com.amlakie.usermanagment.entity.fogrequest.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FuelOilGreaseRequestRepository extends JpaRepository<FuelOilGreaseRequest, Long> {
    List<FuelOilGreaseRequest> findByStatus(RequestStatus status);
    List<FuelOilGreaseRequest> findByHeadMechanicApprovalAndStatus(ApprovalStatus approvalStatus, RequestStatus status);
    List<FuelOilGreaseRequest> findByNezekStatusAndStatusAndIsFulfilled(NezekStatus nezekStatus, RequestStatus status, Boolean isFulfilled);
    List<FuelOilGreaseRequest> findByMechanicName(String mechanicName);
}
