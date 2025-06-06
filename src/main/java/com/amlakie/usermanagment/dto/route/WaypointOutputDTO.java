package com.amlakie.usermanagment.dto.route;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WaypointOutputDTO {
    private Long id; // Optional: if frontend needs waypoint ID
    private Double latitude;
    private Double longitude;
    private Integer sequenceOrder;
}