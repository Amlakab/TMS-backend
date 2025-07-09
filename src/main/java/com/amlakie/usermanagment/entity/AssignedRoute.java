package com.amlakie.usermanagment.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assigned_routes")
@Getter
@Setter
public class AssignedRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_car_id", referencedColumnName = "id", unique = true)
    private OrganizationCar organizationCar;

    @CreationTimestamp
    @Column(name = "assignment_date", nullable = false, updatable = false) // Ensure nullable=false matches DB
    private LocalDateTime assignmentDate; // Or Date, Timestamp

    @OneToMany(mappedBy = "assignedRoute", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("sequenceOrder ASC") // Ensure waypoints are always ordered
    private List<RouteWaypoint> waypoints = new ArrayList<>();

    public void addWaypoint(RouteWaypoint waypoint) {
        waypoints.add(waypoint);
        waypoint.setAssignedRoute(this);
        waypoint.setSequenceOrder(waypoints.size() -1); // Simple 0-based ordering
    }

    public void clearWaypoints() {
        this.waypoints.forEach(wp -> wp.setAssignedRoute(null));
        this.waypoints.clear();
    }
}