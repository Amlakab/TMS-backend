package com.amlakie.usermanagment.dto.organization; // Adjust package name if needed

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

// Assuming ItemConditionDTO is similar to your previous BodyProblemDTO

@Data
@NoArgsConstructor
public class OrganizationBodyInspectionDTO {
    @NotNull(message = "Body collision details are required")
    @Valid // Correct: Enables validation of fields within ItemConditionDTO
    private OrganizationItemConditionDTO bodyCollision = new OrganizationItemConditionDTO();

    @NotNull(message = "Body scratches details are required") // Added message
    @Valid
    private OrganizationItemConditionDTO bodyScratches = new OrganizationItemConditionDTO();

    @NotNull(message = "Paint condition details are required") // Added message
    @Valid
    private OrganizationItemConditionDTO paintCondition = new OrganizationItemConditionDTO();

    @NotNull(message = "Breakages details are required") // Added message
    @Valid
    private OrganizationItemConditionDTO breakages = new OrganizationItemConditionDTO();

    @NotNull(message = "Cracks details are required") // Added message
    @Valid
    private OrganizationItemConditionDTO cracks = new OrganizationItemConditionDTO(); // Initialized directly
}