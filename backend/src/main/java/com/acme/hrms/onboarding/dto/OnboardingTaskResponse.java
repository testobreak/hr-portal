package com.acme.hrms.onboarding.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record OnboardingTaskResponse(
        UUID id,
        UUID onboardingPlanId,
        String taskName,
        String description,
        String assignedRole,
        String status,
        LocalDate dueDate,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
