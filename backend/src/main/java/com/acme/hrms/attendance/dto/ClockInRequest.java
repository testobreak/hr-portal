package com.acme.hrms.attendance.dto;

public record ClockInRequest(
        String ipAddress,
        String notes
) {
}
