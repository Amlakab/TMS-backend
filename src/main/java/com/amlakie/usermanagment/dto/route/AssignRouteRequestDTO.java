package com.amlakie.usermanagment.dto.route;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignRouteRequestDTO {
    private String plateNumber; // Plate number of the OrganizationCar
    private List<WaypointInputDTO> waypoints;
}