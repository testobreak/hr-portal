package com.acme.hrms.recruitment.dto;

import java.time.LocalDate;

public record JobPostingCreateRequest(
        String title,
        String description,
        String locationName,
        String workArrangement, // REMOTE, ONSITE, HYBRID
        String employmentType,
        LocalDate applicationDeadline
) {
}
