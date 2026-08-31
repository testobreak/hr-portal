package com.acme.hrms.workflow.service;

import java.util.UUID;

import com.acme.hrms.workflow.dto.ApprovalRequestCreateRequest;
import com.acme.hrms.workflow.dto.ApprovalRequestResponse;

public interface ApprovalRequestService {
    ApprovalRequestResponse create(ApprovalRequestCreateRequest request);
    ApprovalRequestResponse approve(UUID id);
    ApprovalRequestResponse reject(UUID id);
    ApprovalRequestResponse get(UUID id);
    java.util.List<ApprovalRequestResponse> getPendingRequests();
}
