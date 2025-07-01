package com.amlakie.usermanagment.dto.fogrequest;

import lombok.Data;

@Data
public class RequestItemDTO {
    private Long id; // This ID would be for the RequestItem itself, if it's an existing item
    private String type; // e.g., "fuel", "motorOil", "steeringFluid" - used internally for mapping
    private FillDetailsDTO requested;
    private FillDetailsDTO filled;
    private String details;
}