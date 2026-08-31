package com.acme.hrms.attendance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TimesheetLineResponse(
        UUID id,
        LocalDate dayDate,
        BigDecimal hoursWorked,
        String notes
) {
}
