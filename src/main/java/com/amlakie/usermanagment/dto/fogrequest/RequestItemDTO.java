package com.amlakie.usermanagment.dto.fogrequest;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestItemDTO {
    private Long id;
    private String type;
    private FillDetailsDTO requested;
    private FillDetailsDTO filled;
    private String details;
}

