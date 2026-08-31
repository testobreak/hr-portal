package com.acme.hrms.recruitment.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record JobPostingResponse(
        UUID id,
        UUID jobOpeningId,
        UUID publicId,
        String title,
        String description,
        String locationName,
        String workArrangement,
        String employmentType,
        LocalDate applicationDeadline,
        String status,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
