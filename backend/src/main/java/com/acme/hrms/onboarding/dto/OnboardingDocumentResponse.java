package com.acme.hrms.onboarding.dto;

import java.time.Instant;
import java.util.UUID;

public record OnboardingDocumentResponse(
        UUID id,
        UUID preHireId,
        String documentType,
        String storageKey,
        String status,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
