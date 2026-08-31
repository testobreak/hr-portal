package com.acme.hrms.payroll.dto;

import java.time.LocalDate;

public record PayrollRunCreateRequest(
        LocalDate periodStart,
        LocalDate periodEnd,
        LocalDate payoutDate,
        String runType
) {
}
