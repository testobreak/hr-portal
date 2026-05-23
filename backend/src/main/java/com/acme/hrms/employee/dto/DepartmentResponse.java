package com.acme.hrms.employee.dto;

import java.time.Instant;
import java.util.UUID;

public record DepartmentResponse(
        UUID id,
        String code,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
