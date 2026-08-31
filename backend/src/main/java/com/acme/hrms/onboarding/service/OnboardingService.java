package com.acme.hrms.onboarding.service;

import java.util.List;
import java.util.UUID;

import com.acme.hrms.employee.dto.EmployeeResponse;
import com.acme.hrms.onboarding.dto.EmployeeActivationRequest;
import com.acme.hrms.onboarding.dto.OnboardingDocumentResponse;
import com.acme.hrms.onboarding.dto.OnboardingPlanResponse;
import com.acme.hrms.onboarding.dto.OnboardingTaskResponse;
import com.acme.hrms.onboarding.dto.PreHireResponse;

public interface OnboardingService {
    PreHireResponse getPreHire(UUID id);
    List<PreHireResponse> listPreHires();
    OnboardingPlanResponse getOnboardingPlan(UUID preHireId);
    OnboardingPlanResponse createOnboardingPlan(UUID preHireId, String templateName);
    OnboardingTaskResponse updateTaskStatus(UUID taskId, String status);
    List<OnboardingDocumentResponse> listDocuments(UUID preHireId);
    OnboardingDocumentResponse requestDocument(UUID preHireId, String documentType);
    OnboardingDocumentResponse uploadDocument(UUID docId, String storageKey);
    OnboardingDocumentResponse reviewDocument(UUID docId, String status);
    EmployeeResponse activatePreHire(UUID preHireId, EmployeeActivationRequest request);

    List<com.acme.hrms.onboarding.dto.BackgroundCheckResponse> listBackgroundChecks(UUID preHireId);
    com.acme.hrms.onboarding.dto.BackgroundCheckResponse triggerBackgroundCheck(UUID preHireId);
    com.acme.hrms.onboarding.dto.BackgroundCheckResponse updateBackgroundCheckStatus(UUID checkId, String status);

    List<com.acme.hrms.onboarding.dto.AssetRequestResponse> listAssetRequests(UUID preHireId);
    com.acme.hrms.onboarding.dto.AssetRequestResponse requestAsset(UUID preHireId, String assetType);
    com.acme.hrms.onboarding.dto.AssetRequestResponse updateAssetStatus(UUID assetId, String status);
}
