package com.amlakie.usermanagment.dto.fogrequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FillDetailsDTO {
    private String measurement;
    private Double amount;
    private Double price;
}
