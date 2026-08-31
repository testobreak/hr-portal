package com.acme.hrms.recruitment.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.acme.hrms.common.security.Roles;
import com.acme.hrms.recruitment.dto.InterviewFeedbackResponse;
import com.acme.hrms.recruitment.dto.InterviewFeedbackSubmitRequest;
import com.acme.hrms.recruitment.dto.InterviewResponse;
import com.acme.hrms.recruitment.dto.InterviewScheduleRequest;
import com.acme.hrms.recruitment.service.InterviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/recruitment")
@Tag(name = "Interviews & Evaluations", description = "Endpoints for scheduling interviews and recording scorecard feedback")
public class InterviewController {

    private final InterviewService service;

    public InterviewController(InterviewService service) {
        this.service = service;
    }

    @PostMapping("/applications/{applicationId}/interviews")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Schedule an interview slot for a candidate application")
    public ResponseEntity<InterviewResponse> schedule(@PathVariable UUID applicationId,
                                                      @RequestBody @Valid InterviewScheduleRequest request) {
        InterviewResponse response = service.scheduleInterview(applicationId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/applications/{applicationId}/interviews")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "List all scheduled interviews for an application")
    public List<InterviewResponse> listInterviews(@PathVariable UUID applicationId) {
        return service.listInterviewsForApplication(applicationId);
    }

    @PostMapping("/interviews/{id}/feedback")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Submit score feedback scorecard for an interview")
    public ResponseEntity<InterviewFeedbackResponse> submitFeedback(@PathVariable UUID id,
                                                                    @RequestBody @Valid InterviewFeedbackSubmitRequest request) {
        InterviewFeedbackResponse response = service.submitFeedback(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/interviews/{id}/feedback")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Get list of feedback scorecards for an interview slot")
    public List<InterviewFeedbackResponse> listFeedbackForInterview(@PathVariable UUID id) {
        return service.listFeedbackForInterview(id);
    }

    @GetMapping("/applications/{applicationId}/feedbacks")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Get list of all feedback scorecards for an application")
    public List<InterviewFeedbackResponse> listFeedbackForApplication(@PathVariable UUID applicationId) {
        return service.listFeedbackForApplication(applicationId);
    }

    @PostMapping("/interviews/{id}/complete")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Mark interview as COMPLETED")
    public ResponseEntity<Void> complete(@PathVariable UUID id) {
        service.completeInterview(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/interviews/{id}/cancel")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Mark interview as CANCELLED")
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        service.cancelInterview(id);
        return ResponseEntity.noContent().build();
    }
}
