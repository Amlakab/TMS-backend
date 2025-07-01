package com.amlakie.usermanagment.entity.fogrequest;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable // This class will be embedded directly into RequestItem
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FillDetails {
    private String measurement;
    private Double amount;
    private Double price;
}