package com.acme.hrms.recruitment.dto;

import java.time.Instant;
import java.util.UUID;

public record CandidateApplicationResponse(
        UUID id,
        UUID candidateId,
        String candidateName,
        String candidateEmail,
        String candidatePhone,
        UUID jobOpeningId,
        String jobTitle,
        String currentStage,
        String status,
        String source,
        String resumeStorageKey,
        String coverLetter,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
