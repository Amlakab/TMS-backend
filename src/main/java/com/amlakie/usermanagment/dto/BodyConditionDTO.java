package com.amlakie.usermanagment.dto;

import lombok.Data;

@Data
public class BodyConditionDTO {
    private BodyProblemDTO bodyCollision;
    private BodyProblemDTO bodyScratches;
    private BodyProblemDTO paintCondition;
    private BodyProblemDTO breakages;
    private BodyProblemDTO cracks;
}