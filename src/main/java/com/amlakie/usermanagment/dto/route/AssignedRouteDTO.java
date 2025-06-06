package com.amlakie.usermanagment.dto.route;

import lombok.Data;
import java.util.List;

@Data
public class AssignedRouteDTO {
    private Long id;
    private String plateNumber;
    private List<WaypointOutputDTO> waypoints;
}