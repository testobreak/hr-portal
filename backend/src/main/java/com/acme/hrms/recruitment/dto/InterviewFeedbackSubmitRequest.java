package com.acme.hrms.recruitment.dto;

import java.util.UUID;

public record InterviewFeedbackSubmitRequest(
        UUID interviewerId,
        Integer score,
        String recommendation,
        String comments
) {
}
