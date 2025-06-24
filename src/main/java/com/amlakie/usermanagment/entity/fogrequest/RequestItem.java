package com.amlakie.usermanagment.entity.fogrequest;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.access.prepost.PreAuthorize;

@Entity
@Table(name = "request_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@PreAuthorize("hasAnyRole('HEAD_MECHANIC', 'NEZEK_OFFICIAL')")

public class RequestItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type;

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

    private String details;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private FuelOilGreaseRequest request;
}