package com.acme.hrms.recruitment.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record JobRequisitionCreateRequest(
        String jobTitle,
        UUID departmentId,
        UUID designationId,
        UUID locationId,
        UUID legalEntityId,
        String employmentType,
        Integer openingsCount,
        UUID hiringManagerId,
        LocalDate targetStartDate,
        BigDecimal minSalary,
        BigDecimal maxSalary,
        String currencyCode,
        String requiredSkills,
        Integer minExperienceYears,
        String description,
        String justification
) {
}
