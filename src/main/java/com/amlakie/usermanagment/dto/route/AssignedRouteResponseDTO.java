package com.amlakie.usermanagment.dto.route;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignedRouteResponseDTO {
    private Long id;
    private String plateNumber;
    private Long organizationCarId;
    private List<WaypointOutputDTO> waypoints;
}