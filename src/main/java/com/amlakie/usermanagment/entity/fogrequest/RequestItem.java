package com.amlakie.usermanagment.entity.fogrequest;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "request_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String category; // e.g., "fuel", "motorOil", "brakeFluid", "steeringFluid", "grease"

    @Column(nullable = false)
    private String type; // e.g., "petroleum", "diesel", "num30", "num90", "atf"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fuel_oil_grease_request_id", nullable = false)
    private FuelOilGreaseRequest request;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "measurement", column = @Column(name = "requested_measurement")),
            @AttributeOverride(name = "amount", column = @Column(name = "requested_amount")),
            @AttributeOverride(name = "price", column = @Column(name = "requested_price"))
    })
    private FillDetails requested;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "measurement", column = @Column(name = "filled_measurement")),
            @AttributeOverride(name = "amount", column = @Column(name = "filled_amount")),
            @AttributeOverride(name = "price", column = @Column(name = "filled_price"))
    })
    private FillDetails filled;

    @Column(columnDefinition = "TEXT")
    private String details;
}