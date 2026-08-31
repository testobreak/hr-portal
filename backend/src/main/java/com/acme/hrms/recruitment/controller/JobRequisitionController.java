package com.acme.hrms.recruitment.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.acme.hrms.common.security.Roles;
import com.acme.hrms.recruitment.dto.JobRequisitionCreateRequest;
import com.acme.hrms.recruitment.dto.JobRequisitionResponse;
import com.acme.hrms.recruitment.service.JobRequisitionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/recruitment/requisitions")
@Tag(name = "Job Requisitions", description = "Endpoints for managing hiring requisitions")
public class JobRequisitionController {

    private final JobRequisitionService service;

    public JobRequisitionController(JobRequisitionService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Create a new hiring requisition")
    public ResponseEntity<JobRequisitionResponse> create(@RequestBody JobRequisitionCreateRequest request) {
        JobRequisitionResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "List all job requisitions")
    public List<JobRequisitionResponse> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Get detailed requisition info by ID")
    public JobRequisitionResponse get(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Update a requisition (Only permitted in DRAFT status)")
    public JobRequisitionResponse update(@PathVariable UUID id, @RequestBody JobRequisitionCreateRequest request) {
        return service.update(id, request);
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Submit requisition for approvals flow")
    public ResponseEntity<Void> submit(@PathVariable UUID id) {
        service.submit(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "')")
    @Operation(summary = "Approve requisition (HR/Super Admin only)")
    public ResponseEntity<Void> approve(@PathVariable UUID id) {
        service.approve(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "')")
    @Operation(summary = "Reject requisition (HR/Super Admin only)")
    public ResponseEntity<Void> reject(@PathVariable UUID id) {
        service.reject(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "')")
    @Operation(summary = "Close requisition")
    public ResponseEntity<Void> close(@PathVariable UUID id) {
        service.close(id);
        return ResponseEntity.noContent().build();
    }
}
