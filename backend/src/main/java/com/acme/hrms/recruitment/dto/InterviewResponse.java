package com.acme.hrms.recruitment.dto;

import java.time.Instant;
import java.util.UUID;

public record InterviewResponse(
        UUID id,
        UUID candidateApplicationId,
        String interviewType,
        Instant scheduledTime,
        String status,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
