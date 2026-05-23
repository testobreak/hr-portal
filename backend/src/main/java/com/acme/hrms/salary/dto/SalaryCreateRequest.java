package com.acme.hrms.salary.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SalaryCreateRequest(
        @NotNull UUID employeeId,
        @NotNull @DecimalMin(value = "0.0001") BigDecimal amount,
        @NotNull @Size(min = 3, max = 3) String currencyCode,
        @NotNull LocalDate effectiveFrom,
        LocalDate effectiveTo,
        @Size(max = 1024) String reason
) {
}
