package com.acme.hrms.recruitment.service;

import java.util.List;
import java.util.UUID;

import com.acme.hrms.recruitment.dto.InterviewFeedbackResponse;
import com.acme.hrms.recruitment.dto.InterviewFeedbackSubmitRequest;
import com.acme.hrms.recruitment.dto.InterviewResponse;
import com.acme.hrms.recruitment.dto.InterviewScheduleRequest;

public interface InterviewService {
    InterviewResponse scheduleInterview(UUID applicationId, InterviewScheduleRequest request);
    List<InterviewResponse> listInterviewsForApplication(UUID applicationId);
    InterviewFeedbackResponse submitFeedback(UUID interviewId, InterviewFeedbackSubmitRequest request);
    List<InterviewFeedbackResponse> listFeedbackForInterview(UUID interviewId);
    List<InterviewFeedbackResponse> listFeedbackForApplication(UUID applicationId);
    void completeInterview(UUID id);
    void cancelInterview(UUID id);
}
