package com.acme.hrms.recruitment.dto;

import java.time.Instant;
import java.util.UUID;

public record JobOpeningResponse(
        UUID id,
        UUID jobRequisitionId,
        String status,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
