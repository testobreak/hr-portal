package com.acme.hrms.onboarding.dto;

import java.time.Instant;
import java.util.UUID;

public record BackgroundCheckResponse(
        UUID id,
        UUID preHireId,
        String status,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
