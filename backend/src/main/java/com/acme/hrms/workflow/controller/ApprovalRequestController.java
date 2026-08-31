package com.acme.hrms.workflow.controller;

import java.net.URI;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.acme.hrms.common.security.Roles;
import com.acme.hrms.workflow.dto.ApprovalRequestCreateRequest;
import com.acme.hrms.workflow.dto.ApprovalRequestResponse;
import com.acme.hrms.workflow.service.ApprovalRequestService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/approvals/requests")
@Tag(name = "Approval Requests")
public class ApprovalRequestController {

    private final ApprovalRequestService service;

    public ApprovalRequestController(ApprovalRequestService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create an approval request (Manager initiates)")
    public ResponseEntity<ApprovalRequestResponse> create(@RequestBody @Valid ApprovalRequestCreateRequest request) {
        ApprovalRequestResponse created = service.create(request);
        return ResponseEntity
                .created(URI.create("/api/approvals/requests/" + created.id()))
                .body(created);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "')")
    @Operation(summary = "Approve an approval request (HR Admin action)")
    public ApprovalRequestResponse approve(@PathVariable UUID id) {
        return service.approve(id);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "')")
    @Operation(summary = "Reject an approval request (HR Admin action)")
    public ApprovalRequestResponse reject(@PathVariable UUID id) {
        return service.reject(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Read an approval request")
    public ApprovalRequestResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "List all pending approval requests")
    public java.util.List<ApprovalRequestResponse> list() {
        return service.getPendingRequests();
    }
}
