package com.acme.hrms.project.dto;

import java.time.Instant;
import java.util.UUID;

public record ClientResponse(
        UUID id,
        String code,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
