// Create this new file: D:/my git projects/TMS-backend/src/main/java/com/amlakie/usermanagment/entity/RentCarRouteWaypoint.java
package com.amlakie.usermanagment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "rent_car_route_waypoints")
@Getter
@Setter
public class RentCarRouteWaypoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rent_car_route_id", nullable = false)
    private RentCarRoute rentCarRoute;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Integer sequenceOrder;
}