// Create this new file: D:/my git projects/TMS-backend/src/main/java/com/amlakie/usermanagment/entity/RentCarRoute.java
package com.amlakie.usermanagment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rent_car_routes")
@Getter
@Setter
public class RentCarRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // This is the hard-coded link to RentCar
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rent_car_id", unique = true, nullable = false)
    private RentCar rentCar;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime assignmentDate;

    @OneToMany(mappedBy = "rentCarRoute", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sequenceOrder ASC")
    private List<RentCarRouteWaypoint> waypoints = new ArrayList<>();

    // Helper methods
    public void addWaypoint(RentCarRouteWaypoint waypoint) {
        this.waypoints.add(waypoint);
        waypoint.setRentCarRoute(this);
    }

    public void clearWaypoints() {
        this.waypoints.clear();
    }
}