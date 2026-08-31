package com.acme.hrms.recruitment.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acme.hrms.common.audit.AuditAction;
import com.acme.hrms.common.audit.AuditEvent;
import com.acme.hrms.common.audit.AuditService;
import com.acme.hrms.common.error.ConflictException;
import com.acme.hrms.common.error.NotFoundException;
import com.acme.hrms.recruitment.dto.CandidateApplicationCreateRequest;
import com.acme.hrms.recruitment.dto.CandidateApplicationResponse;
import com.acme.hrms.recruitment.entity.Candidate;
import com.acme.hrms.recruitment.entity.CandidateApplication;
import com.acme.hrms.recruitment.entity.JobOpening;
import com.acme.hrms.common.storage.StorageProperties;
import com.acme.hrms.common.storage.StorageService;
import com.acme.hrms.recruitment.repository.CandidateApplicationRepository;
import com.acme.hrms.recruitment.repository.CandidateRepository;
import com.acme.hrms.recruitment.repository.JobOpeningRepository;

@Service
public class CandidateApplicationServiceImpl implements CandidateApplicationService {

    private final CandidateApplicationRepository repository;
    private final CandidateRepository candidateRepository;
    private final JobOpeningRepository openingRepository;
    private final AuditService auditService;
    private final StorageService storageService;
    private final StorageProperties storageProperties;

    public CandidateApplicationServiceImpl(CandidateApplicationRepository repository,
                                           CandidateRepository candidateRepository,
                                           JobOpeningRepository openingRepository,
                                           AuditService auditService,
                                           StorageService storageService,
                                           StorageProperties storageProperties) {
        this.repository = repository;
        this.candidateRepository = candidateRepository;
        this.openingRepository = openingRepository;
        this.auditService = auditService;
        this.storageService = storageService;
        this.storageProperties = storageProperties;
    }

    @Override
    @Transactional
    public CandidateApplicationResponse apply(UUID jobOpeningId, CandidateApplicationCreateRequest request) {
        JobOpening opening = openingRepository.findById(jobOpeningId)
                .orElseThrow(() -> NotFoundException.of("JobOpening", jobOpeningId));

        // Find or create candidate
        Candidate candidate = candidateRepository.findByEmail(request.email().trim().toLowerCase())
                .orElseGet(() -> {
                    Candidate c = Candidate.builder()
                            .firstName(request.firstName().trim())
                            .lastName(request.lastName().trim())
                            .email(request.email().trim().toLowerCase())
                            .phone(request.phone())
                            .resumeStorageKey(request.resumeStorageKey())
                            .skills(request.skills())
                            .profileSummary(request.profileSummary())
                            .build();
                    c.setTenantId(opening.getTenantId());
                    return candidateRepository.save(c);
                });

        // Check duplicate application
        List<CandidateApplication> existing = repository.findByJobOpeningId(jobOpeningId);
        boolean alreadyApplied = existing.stream()
                .anyMatch(a -> a.getCandidate().getId().equals(candidate.getId()));

        if (alreadyApplied) {
            throw new ConflictException("You have already applied for this position.");
        }

        CandidateApplication app = CandidateApplication.builder()
                .candidate(candidate)
                .jobOpening(opening)
                .currentStage("APPLIED")
                .status("APPLIED")
                .source(request.source() != null ? request.source() : "DIRECT")
                .coverLetter(request.coverLetter())
                .build();
        app.setTenantId(opening.getTenantId());

        CandidateApplication saved = repository.save(app);

        auditService.record(AuditEvent.of(AuditAction.CREATE, "candidate_application")
                .withEntityId(saved.getId())
                .withDetail("Candidate " + candidate.getEmail() + " applied for JobOpening " + jobOpeningId));

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CandidateApplicationResponse> listApplicationsForOpening(UUID jobOpeningId) {
        return repository.findByJobOpeningId(jobOpeningId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CandidateApplicationResponse getApplicationById(UUID id) {
        CandidateApplication app = repository.findById(id)
                .orElseThrow(() -> NotFoundException.of("CandidateApplication", id));
        return toResponse(app);
    }

    @Override
    @Transactional
    public void moveStage(UUID id, String targetStage) {
        CandidateApplication app = repository.findById(id)
                .orElseThrow(() -> NotFoundException.of("CandidateApplication", id));

        String oldStage = app.getCurrentStage();
        app.setCurrentStage(targetStage);
        repository.save(app);

        auditService.record(AuditEvent.of(AuditAction.UPDATE, "candidate_application")
                .withEntityId(id)
                .withDetail("CandidateApplication stage changed from " + oldStage + " to " + targetStage));
    }

    @Override
    @Transactional
    public void reject(UUID id) {
        CandidateApplication app = repository.findById(id)
                .orElseThrow(() -> NotFoundException.of("CandidateApplication", id));

        app.setStatus("REJECTED");
        app.setCurrentStage("REJECTED");
        repository.save(app);

        auditService.record(AuditEvent.of(AuditAction.UPDATE, "candidate_application")
                .withEntityId(id)
                .withDetail("CandidateApplication marked as REJECTED"));
    }

    @Override
    @Transactional
    public void withdraw(UUID id) {
        CandidateApplication app = repository.findById(id)
                .orElseThrow(() -> NotFoundException.of("CandidateApplication", id));

        app.setStatus("WITHDRAWN");
        app.setCurrentStage("WITHDRAWN");
        repository.save(app);

        auditService.record(AuditEvent.of(AuditAction.UPDATE, "candidate_application")
                .withEntityId(id)
                .withDetail("CandidateApplication marked as WITHDRAWN"));
    }

    @Override
    @Transactional(readOnly = true)
    public String getResumeDownloadUrl(UUID id) {
        CandidateApplication app = repository.findById(id)
                .orElseThrow(() -> NotFoundException.of("CandidateApplication", id));

        String key = app.getCandidate().getResumeStorageKey();
        if (key == null || key.isBlank()) {
            throw new NotFoundException("No resume found for this candidate.");
        }

        java.time.Duration ttl = java.time.Duration.ofSeconds(storageProperties.getPresignTtlSeconds());
        return storageService.presignGet(storageProperties.getBucket(), key, ttl).toString();
    }

    private CandidateApplicationResponse toResponse(CandidateApplication entity) {
        String candidateName = entity.getCandidate().getFirstName() + " " + entity.getCandidate().getLastName();
        return new CandidateApplicationResponse(
                entity.getId(),
                entity.getCandidate().getId(),
                candidateName,
                entity.getCandidate().getEmail(),
                entity.getCandidate().getPhone(),
                entity.getJobOpening().getId(),
                entity.getJobOpening().getJobRequisition().getJobTitle(),
                entity.getCurrentStage(),
                entity.getStatus(),
                entity.getSource(),
                entity.getCandidate().getResumeStorageKey(),
                entity.getCoverLetter(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion()
        );
    }
}
