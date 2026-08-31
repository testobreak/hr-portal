package com.acme.hrms.recruitment.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.acme.hrms.common.security.Roles;
import com.acme.hrms.recruitment.dto.CandidateApplicationResponse;
import com.acme.hrms.recruitment.service.CandidateApplicationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/recruitment")
@Tag(name = "Candidate Applications", description = "Endpoints for managing applicant evaluations and pipelines")
public class CandidateApplicationController {

    private final CandidateApplicationService service;

    public CandidateApplicationController(CandidateApplicationService service) {
        this.service = service;
    }

    @GetMapping("/job-openings/{openingId}/applications")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "List all applications submitted for a job opening")
    public List<CandidateApplicationResponse> listForOpening(@PathVariable UUID openingId) {
        return service.listApplicationsForOpening(openingId);
    }

    @GetMapping("/applications/{id}")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Get application details by ID")
    public CandidateApplicationResponse get(@PathVariable UUID id) {
        return service.getApplicationById(id);
    }

    @GetMapping("/applications/{id}/resume-download")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Get presigned download URL for candidate resume")
    public ResponseEntity<java.util.Map<String, String>> getResumeDownloadUrl(@PathVariable UUID id) {
        String url = service.getResumeDownloadUrl(id);
        return ResponseEntity.ok(java.util.Map.of("downloadUrl", url));
    }

    @PostMapping("/applications/{id}/move-stage")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Move candidate application to a different pipeline stage")
    public ResponseEntity<Void> moveStage(@PathVariable UUID id, @RequestParam(name = "stage") String stage) {
        service.moveStage(id, stage);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/applications/{id}/reject")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Reject candidate application")
    public ResponseEntity<Void> reject(@PathVariable UUID id) {
        service.reject(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/applications/{id}/withdraw")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Withdraw candidate application")
    public ResponseEntity<Void> withdraw(@PathVariable UUID id) {
        service.withdraw(id);
        return ResponseEntity.noContent().build();
    }
}
