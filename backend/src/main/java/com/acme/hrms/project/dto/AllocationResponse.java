package com.acme.hrms.project.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AllocationResponse(
        UUID id,
        UUID projectId,
        String projectCode,
        String projectName,
        UUID employeeId,
        String employeeName,
        Integer allocationPercentage,
        String roleTitle,
        LocalDate startDate,
        LocalDate endDate,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
