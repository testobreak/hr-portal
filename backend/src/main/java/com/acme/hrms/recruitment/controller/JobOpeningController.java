package com.acme.hrms.recruitment.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.acme.hrms.common.security.Roles;
import com.acme.hrms.recruitment.dto.JobOpeningResponse;
import com.acme.hrms.recruitment.dto.JobPostingCreateRequest;
import com.acme.hrms.recruitment.dto.JobPostingResponse;
import com.acme.hrms.recruitment.service.JobPostingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/recruitment/job-openings")
@Tag(name = "Job Openings", description = "Endpoints for managing vacancy openings")
public class JobOpeningController {

    private final JobPostingService service;

    public JobOpeningController(JobPostingService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "')")
    @Operation(summary = "Manually open a job vacancy slot from an approved requisition")
    public ResponseEntity<JobOpeningResponse> create(@RequestParam(name = "requisitionId") UUID requisitionId) {
        JobOpeningResponse response = service.createOpening(requisitionId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/postings")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Create a job advertisement posting (DRAFT status)")
    public ResponseEntity<JobPostingResponse> createPosting(@PathVariable UUID id, @RequestBody JobPostingCreateRequest request) {
        JobPostingResponse response = service.createPosting(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
