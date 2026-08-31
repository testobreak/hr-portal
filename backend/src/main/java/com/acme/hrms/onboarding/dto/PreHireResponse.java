package com.acme.hrms.onboarding.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PreHireResponse(
        UUID id,
        UUID candidateId,
        String candidateName,
        String candidateEmail,
        UUID acceptedOfferId,
        String legalEntityName,
        String departmentName,
        String designationTitle,
        String locationName,
        String managerName,
        LocalDate startDate,
        String status,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
