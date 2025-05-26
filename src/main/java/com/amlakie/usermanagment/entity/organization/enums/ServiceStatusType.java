package com.amlakie.usermanagment.entity.organization.enums;

public enum ServiceStatusType {
    PENDING, // Assuming database might store "PENDING"
    READY,
    READY_WITH_WARNING,
    NOT_READY,
    IN_SERVICE,
    OUT_OF_SERVICE,
    PENDING_MAINTENANCE,

    // Add any other statuses relevant to the service lifecycle of the car
}