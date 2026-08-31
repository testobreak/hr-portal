package com.acme.hrms.attendance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TimesheetLineCreateRequest(
        LocalDate dayDate,
        BigDecimal hoursWorked,
        String notes
) {
}
