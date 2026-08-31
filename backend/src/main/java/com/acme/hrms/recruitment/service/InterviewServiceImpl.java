package com.acme.hrms.recruitment.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acme.hrms.common.error.NotFoundException;
import com.acme.hrms.employee.entity.Employee;
import com.acme.hrms.employee.repository.EmployeeRepository;
import com.acme.hrms.recruitment.dto.InterviewFeedbackResponse;
import com.acme.hrms.recruitment.dto.InterviewFeedbackSubmitRequest;
import com.acme.hrms.recruitment.dto.InterviewResponse;
import com.acme.hrms.recruitment.dto.InterviewScheduleRequest;
import com.acme.hrms.recruitment.entity.CandidateApplication;
import com.acme.hrms.recruitment.entity.Interview;
import com.acme.hrms.recruitment.entity.InterviewFeedback;
import com.acme.hrms.recruitment.repository.CandidateApplicationRepository;
import com.acme.hrms.recruitment.repository.InterviewFeedbackRepository;
import com.acme.hrms.recruitment.repository.InterviewRepository;

@Service
public class InterviewServiceImpl implements InterviewService {

    private final InterviewRepository interviewRepository;
    private final InterviewFeedbackRepository feedbackRepository;
    private final CandidateApplicationRepository applicationRepository;
    private final EmployeeRepository employeeRepository;
    private final CalendarProvider calendarProvider;

    public InterviewServiceImpl(InterviewRepository interviewRepository,
                                InterviewFeedbackRepository feedbackRepository,
                                CandidateApplicationRepository applicationRepository,
                                EmployeeRepository employeeRepository,
                                CalendarProvider calendarProvider) {
        this.interviewRepository = interviewRepository;
        this.feedbackRepository = feedbackRepository;
        this.applicationRepository = applicationRepository;
        this.employeeRepository = employeeRepository;
        this.calendarProvider = calendarProvider;
    }

    @Override
    @Transactional
    public InterviewResponse scheduleInterview(UUID applicationId, InterviewScheduleRequest request) {
        CandidateApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> NotFoundException.of("CandidateApplication", applicationId));

        Interview interview = Interview.builder()
                .candidateApplication(app)
                .interviewType(request.interviewType())
                .scheduledTime(request.scheduledTime())
                .status("SCHEDULED")
                .build();
        interview.setTenantId(app.getTenantId());

        Interview saved = interviewRepository.save(interview);
        calendarProvider.sendInvite(saved.getId(), app.getCandidate().getEmail(), "recruiter@acme.com", saved.getInterviewType(), saved.getScheduledTime());
        return toInterviewResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterviewResponse> listInterviewsForApplication(UUID applicationId) {
        return interviewRepository.findByCandidateApplicationId(applicationId).stream()
                .map(this::toInterviewResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InterviewFeedbackResponse submitFeedback(UUID interviewId, InterviewFeedbackSubmitRequest request) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> NotFoundException.of("Interview", interviewId));

        Employee interviewer = employeeRepository.findById(request.interviewerId())
                .orElseThrow(() -> NotFoundException.of("Employee", request.interviewerId()));

        InterviewFeedback feedback = InterviewFeedback.builder()
                .interview(interview)
                .interviewer(interviewer)
                .score(request.score())
                .recommendation(request.recommendation())
                .comments(request.comments())
                .build();
        feedback.setTenantId(interview.getTenantId());

        InterviewFeedback saved = feedbackRepository.save(feedback);
        return toFeedbackResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterviewFeedbackResponse> listFeedbackForInterview(UUID interviewId) {
        return feedbackRepository.findByInterviewId(interviewId).stream()
                .map(this::toFeedbackResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterviewFeedbackResponse> listFeedbackForApplication(UUID applicationId) {
        return feedbackRepository.findByInterviewCandidateApplicationId(applicationId).stream()
                .map(this::toFeedbackResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void completeInterview(UUID id) {
        Interview interview = interviewRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Interview", id));
        interview.setStatus("COMPLETED");
        interviewRepository.save(interview);
    }

    @Override
    @Transactional
    public void cancelInterview(UUID id) {
        Interview interview = interviewRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Interview", id));
        interview.setStatus("CANCELLED");
        interviewRepository.save(interview);
    }

    private InterviewResponse toInterviewResponse(Interview entity) {
        return new InterviewResponse(
                entity.getId(),
                entity.getCandidateApplication().getId(),
                entity.getInterviewType(),
                entity.getScheduledTime(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion()
        );
    }

    private InterviewFeedbackResponse toFeedbackResponse(InterviewFeedback entity) {
        String interviewerName = entity.getInterviewer().getFirstName() + " " + entity.getInterviewer().getLastName();
        return new InterviewFeedbackResponse(
                entity.getId(),
                entity.getInterview().getId(),
                entity.getInterviewer().getId(),
                interviewerName,
                entity.getScore(),
                entity.getRecommendation(),
                entity.getComments(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion()
        );
    }
}
