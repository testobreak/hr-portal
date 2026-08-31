package com.acme.hrms.onboarding.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OnboardingPlanResponse(
        UUID id,
        UUID preHireId,
        String templateName,
        List<OnboardingTaskResponse> tasks,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
