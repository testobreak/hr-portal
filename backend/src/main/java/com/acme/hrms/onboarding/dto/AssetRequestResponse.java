package com.acme.hrms.onboarding.dto;

import java.time.Instant;
import java.util.UUID;

public record AssetRequestResponse(
        UUID id,
        UUID preHireId,
        String assetType,
        String status,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
