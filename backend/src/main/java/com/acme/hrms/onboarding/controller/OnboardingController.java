package com.acme.hrms.onboarding.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.acme.hrms.common.security.Roles;
import com.acme.hrms.employee.dto.EmployeeResponse;
import com.acme.hrms.onboarding.dto.EmployeeActivationRequest;
import com.acme.hrms.onboarding.dto.OnboardingDocumentResponse;
import com.acme.hrms.onboarding.dto.OnboardingPlanResponse;
import com.acme.hrms.onboarding.dto.OnboardingTaskResponse;
import com.acme.hrms.onboarding.dto.PreHireResponse;
import com.acme.hrms.onboarding.dto.BackgroundCheckResponse;
import com.acme.hrms.onboarding.dto.AssetRequestResponse;
import com.acme.hrms.onboarding.service.OnboardingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/onboarding")
@Tag(name = "Pre-Hire Onboarding & Activation", description = "Endpoints for managing candidate pre-joining checklists and active employee conversion")
public class OnboardingController {

    private final OnboardingService service;

    public OnboardingController(OnboardingService service) {
        this.service = service;
    }

    @GetMapping("/pre-hires")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "List all pre-hire candidate profiles")
    public List<PreHireResponse> list() {
        return service.listPreHires();
    }

    @GetMapping("/pre-hires/{id}")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Get detailed pre-hire profile by ID")
    public PreHireResponse get(@PathVariable UUID id) {
        return service.getPreHire(id);
    }

    @GetMapping("/pre-hires/{id}/plan")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Get onboarding checklist plan and tasks for a pre-hire")
    public OnboardingPlanResponse getPlan(@PathVariable UUID id) {
        return service.getOnboardingPlan(id);
    }

    @PostMapping("/pre-hires/{id}/plan")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Initialize onboarding tasks checklist template for pre-hire")
    public ResponseEntity<OnboardingPlanResponse> createPlan(@PathVariable UUID id,
                                                             @RequestParam(defaultValue = "STANDARD") String templateName) {
        OnboardingPlanResponse response = service.createOnboardingPlan(id, templateName);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/tasks/{taskId}/status")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Update progress status of an onboarding task")
    public OnboardingTaskResponse updateTaskStatus(@PathVariable UUID taskId,
                                                   @RequestParam String status) {
        return service.updateTaskStatus(taskId, status);
    }

    @GetMapping("/pre-hires/{id}/documents")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "List all required and uploaded onboarding verification documents")
    public List<OnboardingDocumentResponse> listDocuments(@PathVariable UUID id) {
        return service.listDocuments(id);
    }

    @PostMapping("/pre-hires/{id}/documents")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "')")
    @Operation(summary = "Request a new verification document from pre-hire")
    public ResponseEntity<OnboardingDocumentResponse> requestDoc(@PathVariable UUID id,
                                                                 @RequestParam String documentType) {
        OnboardingDocumentResponse response = service.requestDocument(id, documentType);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/documents/{docId}/upload")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Upload verification document file")
    public OnboardingDocumentResponse uploadDoc(@PathVariable UUID docId,
                                                @RequestParam String storageKey) {
        return service.uploadDocument(docId, storageKey);
    }

    @PostMapping("/documents/{docId}/review")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "')")
    @Operation(summary = "Approve or reject verification document")
    public OnboardingDocumentResponse reviewDoc(@PathVariable UUID docId,
                                                @RequestParam String status) {
        return service.reviewDocument(docId, status);
    }

    @PostMapping("/pre-hires/{id}/activate")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "')")
    @Operation(summary = "Convert pre-hire candidate profile into active full Employee")
    public ResponseEntity<EmployeeResponse> activate(@PathVariable UUID id,
                                                     @RequestBody @Valid EmployeeActivationRequest request) {
        EmployeeResponse employee = service.activatePreHire(id, request);
        return ResponseEntity.ok(employee);
    }

    @GetMapping("/pre-hires/{id}/background-checks")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "List background check verifications for pre-hire")
    public List<BackgroundCheckResponse> listBackgroundChecks(@PathVariable UUID id) {
        return service.listBackgroundChecks(id);
    }

    @PostMapping("/pre-hires/{id}/background-checks")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "')")
    @Operation(summary = "Trigger background check verification for pre-hire")
    public ResponseEntity<BackgroundCheckResponse> triggerBackgroundCheck(@PathVariable UUID id) {
        BackgroundCheckResponse response = service.triggerBackgroundCheck(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/background-checks/{checkId}/status")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "')")
    @Operation(summary = "Update status of background verification")
    public BackgroundCheckResponse updateBackgroundCheckStatus(@PathVariable UUID checkId,
                                                               @RequestParam String status) {
        return service.updateBackgroundCheckStatus(checkId, status);
    }

    @GetMapping("/pre-hires/{id}/assets")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "List asset provisioning allocations for pre-hire")
    public List<AssetRequestResponse> listAssetRequests(@PathVariable UUID id) {
        return service.listAssetRequests(id);
    }

    @PostMapping("/pre-hires/{id}/assets")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Request asset allocation for pre-hire")
    public ResponseEntity<AssetRequestResponse> requestAsset(@PathVariable UUID id,
                                                             @RequestParam String assetType) {
        AssetRequestResponse response = service.requestAsset(id, assetType);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/assets/{assetId}/status")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Update status of asset allocation")
    public AssetRequestResponse updateAssetStatus(@PathVariable UUID assetId,
                                                  @RequestParam String status) {
        return service.updateAssetStatus(assetId, status);
    }
}
