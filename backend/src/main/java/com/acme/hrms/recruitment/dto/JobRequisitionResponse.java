package com.acme.hrms.recruitment.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record JobRequisitionResponse(
        UUID id,
        UUID jobOpeningId,
        String reqNumber,
        String jobTitle,
        UUID departmentId,
        String departmentName,
        UUID designationId,
        String designationTitle,
        UUID locationId,
        String locationName,
        UUID legalEntityId,
        String legalEntityName,
        String employmentType,
        Integer openingsCount,
        UUID hiringManagerId,
        String hiringManagerName,
        UUID recruiterId,
        String recruiterName,
        LocalDate targetStartDate,
        BigDecimal minSalary,
        BigDecimal maxSalary,
        String currencyCode,
        String requiredSkills,
        Integer minExperienceYears,
        String description,
        String justification,
        String status,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
