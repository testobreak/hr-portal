package com.acme.hrms.attendance.dto;

import java.time.LocalDate;
import java.util.List;

public record TimesheetCreateRequest(
        LocalDate startDate,
        LocalDate endDate,
        List<TimesheetLineCreateRequest> lines,
        String submissionComments
) {
}
