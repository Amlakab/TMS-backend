// Create this new file: D:/my git projects/TMS-backend/src/main/java/com/amlakie/usermanagment/dto/route/RentCarRouteDTO.java
package com.amlakie.usermanagment.dto;

import com.amlakie.usermanagment.dto.route.WaypointOutputDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentCarRouteDTO {
    private Long routeId;
    private String plateNumber;
    private Long rentCarId;
    private List<WaypointOutputDTO> waypoints; // We can reuse the WaypointOutputDTO
}