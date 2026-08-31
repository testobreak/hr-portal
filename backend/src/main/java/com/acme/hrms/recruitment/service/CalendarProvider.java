package com.acme.hrms.recruitment.service;

import java.time.Instant;
import java.util.UUID;

public interface CalendarProvider {
    void sendInvite(UUID interviewId, String candidateEmail, String interviewerEmail, String interviewType, Instant time);
}
