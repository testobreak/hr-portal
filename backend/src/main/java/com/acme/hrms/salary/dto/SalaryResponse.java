package com.acme.hrms.salary.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record SalaryResponse(
        UUID id,
        UUID employeeId,
        BigDecimal amount,
        String currencyCode,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        String reason,
        Instant createdAt
) {
}
