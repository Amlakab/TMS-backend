package com.amlakie.usermanagment.dto.organization;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor // Useful for deserialization and frameworks
@JsonInclude(JsonInclude.Include.NON_NULL) // Omits null fields from JSON output
public class OrganizationCarInspectionListResponse {
    private int codStatus;
    private String message;
    private String error;
    private List<OrganizationCarInspectionReqRes> inspections;
}