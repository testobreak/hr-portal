package com.acme.hrms.attendance.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TimesheetResponse(
        UUID id,
        UUID employeeId,
        String employeeName,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal totalHours,
        String status,
        UUID approvedById,
        String approvedByName,
        Instant approvedAt,
        String submissionComments,
        String approvalComments,
        List<TimesheetLineResponse> lines
) {
}
