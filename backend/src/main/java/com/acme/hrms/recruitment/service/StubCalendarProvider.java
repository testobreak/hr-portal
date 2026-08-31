package com.acme.hrms.recruitment.service;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class StubCalendarProvider implements CalendarProvider {
    private static final Logger log = LoggerFactory.getLogger(StubCalendarProvider.class);

    @Override
    public void sendInvite(UUID interviewId, String candidateEmail, String interviewerEmail, String interviewType, Instant time) {
        log.info("CALENDAR INVITE SENT [Interview ID: {}]: Type: {}, Time: {}, Candidate: {}, Interviewer: {}",
                interviewId, interviewType, time, candidateEmail, interviewerEmail);
    }
}
