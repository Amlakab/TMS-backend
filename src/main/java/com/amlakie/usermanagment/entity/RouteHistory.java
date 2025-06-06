package com.amlakie.usermanagment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "route_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RouteHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // A car can have many historical routes
    @JoinColumn(name = "organization_car_id", nullable = false)
    private OrganizationCar organizationCar;

    @Column(name = "original_assigned_route_id") // Optional: to link back to the original AssignedRoute if needed
    private Long originalAssignedRouteId;

    @Column(name = "assignment_start_date", nullable = false)
    private LocalDateTime assignmentStartDate; // When this route became active

    @Column(name = "assignment_end_date") // Can be null if it's the most recent "historical" entry or if unassignment wasn't tracked
    private LocalDateTime assignmentEndDate; // When this route was replaced or completed

    @OneToMany(mappedBy = "routeHistory", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sequenceOrder ASC")
    private List<RouteHistoryWaypoint> waypoints = new ArrayList<>();

    // Helper methods
    public void addWaypoint(RouteHistoryWaypoint waypoint) {
        this.waypoints.add(waypoint);
        waypoint.setRouteHistory(this);
    }
}