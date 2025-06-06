package com.amlakie.usermanagment.dto.route;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WaypointInputDTO {
    private Double latitude;
    private Double longitude;
    // sequenceOrder could be implicitly derived from list order or explicitly passed
}