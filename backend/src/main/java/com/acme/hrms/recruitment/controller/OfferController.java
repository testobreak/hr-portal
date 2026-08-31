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
import com.acme.hrms.recruitment.dto.OfferCreateRequest;
import com.acme.hrms.recruitment.dto.OfferResponse;
import com.acme.hrms.recruitment.service.OfferService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/recruitment")
@Tag(name = "Job Offers", description = "Endpoints for creating and releasing employment offer letters")
public class OfferController {

    private final OfferService service;

    public OfferController(OfferService service) {
        this.service = service;
    }

    @PostMapping("/applications/{applicationId}/offers")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Create an offer letter draft for a candidate application")
    public ResponseEntity<OfferResponse> create(@PathVariable UUID applicationId,
                                                @RequestBody @Valid OfferCreateRequest request) {
        OfferResponse response = service.createOffer(applicationId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/applications/{applicationId}/offers")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "List all offers generated for a candidate application")
    public List<OfferResponse> list(@PathVariable UUID applicationId) {
        return service.listOffersForApplication(applicationId);
    }

    @PostMapping("/offers/{id}/submit")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Submit offer letter for HR approval")
    public ResponseEntity<Void> submit(@PathVariable UUID id) {
        service.submitOfferForApproval(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/offers/{id}/approve")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "')")
    @Operation(summary = "Approve offer letter")
    public ResponseEntity<Void> approve(@PathVariable UUID id) {
        service.approveOffer(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/offers/{id}/release")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Release/send offer letter to the candidate")
    public ResponseEntity<Void> release(@PathVariable UUID id) {
        service.releaseOffer(id);
        return ResponseEntity.noContent().build();
    }
}
