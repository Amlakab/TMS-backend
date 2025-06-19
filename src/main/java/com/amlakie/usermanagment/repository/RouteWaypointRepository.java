package com.amlakie.usermanagment.repository;

import com.amlakie.usermanagment.entity.RouteWaypoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteWaypointRepository extends JpaRepository<RouteWaypoint, Long> {
    // Custom queries for waypoints if needed
}