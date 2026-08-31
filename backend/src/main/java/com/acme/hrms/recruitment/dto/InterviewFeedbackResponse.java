package com.acme.hrms.recruitment.dto;

import java.time.Instant;
import java.util.UUID;

public record InterviewFeedbackResponse(
        UUID id,
        UUID interviewId,
        UUID interviewerId,
        String interviewerName,
        Integer score,
        String recommendation,
        String comments,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
