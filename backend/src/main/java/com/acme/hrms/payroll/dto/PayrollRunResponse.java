package com.acme.hrms.payroll.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PayrollRunResponse(
        UUID id,
        LocalDate periodStart,
        LocalDate periodEnd,
        String status,
        LocalDate payoutDate,
        BigDecimal totalGross,
        BigDecimal totalDeductions,
        BigDecimal totalNet,
        String runType
) {
}
