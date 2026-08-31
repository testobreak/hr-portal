package com.acme.hrms.payroll.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PayslipResponse(
        UUID id,
        UUID payrollRunId,
        UUID employeeId,
        String employeeName,
        BigDecimal basicSalary,
        BigDecimal allowances,
        BigDecimal deductions,
        BigDecimal taxDeductions,
        BigDecimal netSalary,
        Integer workingDays,
        Integer presentDays,
        Integer leaveDays,
        String currencyCode,
        String status,
        Instant sentAt,
        List<PayslipItemResponse> items
) {
}
