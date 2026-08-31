package com.acme.hrms.attendance.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AttendanceResponse(
        UUID id,
        UUID employeeId,
        String employeeName,
        Instant clockIn,
        Instant clockOut,
        String status,
        BigDecimal workingHours,
        String ipAddress,
        String notes
) {
}
