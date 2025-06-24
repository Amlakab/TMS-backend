package com.amlakie.usermanagment.dto.fogrequest;

import com.amlakie.usermanagment.entity.fogrequest.ApprovalStatus;
import com.amlakie.usermanagment.entity.fogrequest.NezekStatus;
import com.amlakie.usermanagment.entity.fogrequest.RequestStatus;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuelOilGreaseRequestDTO {
    private Long id;
    private LocalDate requestDate;
    private String carType;
    private String plateNumber;
    private Double kmReading;
    private String shortExplanation;
    private List<RequestItemDTO> items;
    private String mechanicName;
    private String headMechanicName;
    private ApprovalStatus headMechanicApproval;
    private String nezekOfficialName;
    private NezekStatus nezekStatus;
    private Boolean isFulfilled;
    private RequestStatus status;
}
