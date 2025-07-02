package com.amlakie.usermanagment.dto.fogrequest;

import lombok.Data;

@Data
public class FillDetailsDTO {
    private String measurement;
    private Double amount; // Frontend sends string, but parses to float, so Double is appropriate
    private Double price; // Frontend sends string, but parses to float, so Double is appropriate
}