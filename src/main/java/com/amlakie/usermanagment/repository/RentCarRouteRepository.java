// In D:/my git projects/TMS-backend/src/main/java/com/amlakie/usermanagment/repository/RentCarRouteRepository.java
package com.amlakie.usermanagment.repository;

import com.amlakie.usermanagment.entity.RentCarRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List; // Import List
import java.util.Optional;

public interface RentCarRouteRepository extends JpaRepository<RentCarRoute, Long> {

    // This existing method is fine for finding a route to update
    Optional<RentCarRoute> findByRentCarId(Long rentCarId);

    // This existing method is perfect for getting a route by plate number
    @Query("SELECT rcr FROM RentCarRoute rcr JOIN FETCH rcr.waypoints WHERE rcr.rentCar.plateNumber = :plateNumber")
    Optional<RentCarRoute> findByRentCarPlateNumberWithWaypoints(@Param("plateNumber") String plateNumber);

    // NEW: Find by ID, eagerly fetching waypoints and the associated car
    @Query("SELECT rcr FROM RentCarRoute rcr JOIN FETCH rcr.rentCar c JOIN FETCH rcr.waypoints WHERE rcr.id = :id")
    Optional<RentCarRoute> findByIdWithWaypointsAndCar(@Param("id") Long id);

    // NEW: Find all routes, eagerly fetching waypoints and the associated car to prevent N+1 issues
    @Query("SELECT rcr FROM RentCarRoute rcr JOIN FETCH rcr.rentCar c JOIN FETCH rcr.waypoints")
    List<RentCarRoute> findAllWithWaypointsAndCar();
}