package com.amlakie.usermanagment.repository;

import com.amlakie.usermanagment.entity.AssignedRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignedRouteRepository extends JpaRepository<AssignedRoute, Long> {

    @Query("SELECT ar FROM AssignedRoute ar JOIN FETCH ar.organizationCar oc WHERE oc.plateNumber = :plateNumber")
    Optional<AssignedRoute> findByOrganizationCarPlateNumberWithWaypoints(@Param("plateNumber") String plateNumber);

    @Query("SELECT ar FROM AssignedRoute ar JOIN FETCH ar.organizationCar LEFT JOIN FETCH ar.waypoints WHERE ar.id = :id")
    Optional<AssignedRoute> findByIdWithWaypointsAndCar(@Param("id") Long id);

    @Query("SELECT ar FROM AssignedRoute ar JOIN FETCH ar.organizationCar LEFT JOIN FETCH ar.waypoints")
    List<AssignedRoute> findAllWithWaypointsAndCar();

    Optional<AssignedRoute> findByOrganizationCarId(Long carId);
}