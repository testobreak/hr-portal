package com.acme.hrms.recruitment.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.acme.hrms.common.security.Roles;
import com.acme.hrms.recruitment.dto.JobPostingResponse;
import com.acme.hrms.recruitment.service.JobPostingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/recruitment/job-postings")
@Tag(name = "Job Postings", description = "Internal endpoints for managing job postings")
public class JobPostingController {

    private final JobPostingService service;

    public JobPostingController(JobPostingService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "List all job postings")
    public List<JobPostingResponse> list() {
        return service.listAllPostings();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Get internal job posting details by ID")
    public JobPostingResponse get(@PathVariable UUID id) {
        return service.getPostingById(id);
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Publish a job advertisement posting (Set to PUBLISHED)")
    public ResponseEntity<Void> publish(@PathVariable UUID id) {
        service.publish(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Unpublish a job advertisement posting (Set to UNPUBLISHED)")
    public ResponseEntity<Void> unpublish(@PathVariable UUID id) {
        service.unpublish(id);
        return ResponseEntity.noContent().build();
    }
}
