package com.amlakie.usermanagment.repository;

import com.amlakie.usermanagment.entity.RouteHistoryWaypoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteHistoryWaypointRepository extends JpaRepository<RouteHistoryWaypoint, Long> {

    // Example: Find waypoints for a specific route history ID
    List<RouteHistoryWaypoint> findByRouteHistoryIdOrderBySequenceOrderAsc(Long routeHistoryId);
}