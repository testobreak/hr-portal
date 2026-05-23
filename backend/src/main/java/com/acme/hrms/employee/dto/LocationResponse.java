package com.acme.hrms.employee.dto;

import java.time.Instant;
import java.util.UUID;

public record LocationResponse(
        UUID id,
        String code,
        String name,
        String city,
        String country,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
