package com.amlakie.usermanagment.entity.fogrequest;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FillDetails {
    private String measurement;
    private Double amount;
    private Double price;
}