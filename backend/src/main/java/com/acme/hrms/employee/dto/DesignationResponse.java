package com.acme.hrms.employee.dto;

import java.time.Instant;
import java.util.UUID;

public record DesignationResponse(
        UUID id,
        String title,
        String level,
        String description,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
