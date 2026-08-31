package com.acme.hrms.recruitment.dto;

import java.time.Instant;

public record InterviewScheduleRequest(
        String interviewType,
        Instant scheduledTime
) {
}
