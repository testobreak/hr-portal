package com.acme.hrms.recruitment.service;

import java.util.List;
import java.util.UUID;

import com.acme.hrms.recruitment.dto.CandidateApplicationCreateRequest;
import com.acme.hrms.recruitment.dto.CandidateApplicationResponse;

public interface CandidateApplicationService {
    CandidateApplicationResponse apply(UUID jobOpeningId, CandidateApplicationCreateRequest request);
    List<CandidateApplicationResponse> listApplicationsForOpening(UUID jobOpeningId);
    CandidateApplicationResponse getApplicationById(UUID id);
    void moveStage(UUID id, String targetStage);
    void reject(UUID id);
    void withdraw(UUID id);
    String getResumeDownloadUrl(UUID id);
}
