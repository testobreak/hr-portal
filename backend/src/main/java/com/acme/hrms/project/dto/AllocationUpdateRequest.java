package com.acme.hrms.project.dto;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AllocationUpdateRequest(
        @NotNull UUID projectId,
        @NotNull UUID employeeId,
        @NotNull @Min(1) @Max(100) Integer allocationPercentage,
        @Size(max = 128) String roleTitle,
        @NotNull LocalDate startDate,
        LocalDate endDate,
        @NotNull Long version
) {
}
