package com.acme.hrms.recruitment.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acme.hrms.common.error.ConflictException;
import com.acme.hrms.common.error.NotFoundException;
import com.acme.hrms.onboarding.entity.PreHire;
import com.acme.hrms.onboarding.repository.PreHireRepository;
import com.acme.hrms.recruitment.dto.OfferCreateRequest;
import com.acme.hrms.recruitment.dto.OfferResponse;
import com.acme.hrms.recruitment.entity.CandidateApplication;
import com.acme.hrms.recruitment.entity.JobRequisition;
import com.acme.hrms.recruitment.entity.Offer;
import com.acme.hrms.recruitment.repository.CandidateApplicationRepository;
import com.acme.hrms.recruitment.repository.OfferRepository;
import com.acme.hrms.workflow.entity.ApprovalRequest;
import com.acme.hrms.workflow.entity.ApprovalStatus;
import com.acme.hrms.workflow.repository.ApprovalRequestRepository;
import com.acme.hrms.common.security.CurrentUser;

@Service
public class OfferServiceImpl implements OfferService {

    private final OfferRepository offerRepository;
    private final CandidateApplicationRepository applicationRepository;
    private final PreHireRepository preHireRepository;
    private final ApprovalRequestRepository approvalRequestRepository;

    public OfferServiceImpl(OfferRepository offerRepository,
                            CandidateApplicationRepository applicationRepository,
                            PreHireRepository preHireRepository,
                            ApprovalRequestRepository approvalRequestRepository) {
        this.offerRepository = offerRepository;
        this.applicationRepository = applicationRepository;
        this.preHireRepository = preHireRepository;
        this.approvalRequestRepository = approvalRequestRepository;
    }

    @Override
    @Transactional
    public OfferResponse createOffer(UUID applicationId, OfferCreateRequest request) {
        CandidateApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> NotFoundException.of("CandidateApplication", applicationId));

        Offer offer = Offer.builder()
                .candidateApplication(app)
                .salaryAmount(request.salaryAmount())
                .currencyCode(request.currencyCode())
                .startDate(request.startDate())
                .status("DRAFT")
                .build();
        offer.setTenantId(app.getTenantId());

        Offer saved = offerRepository.save(offer);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfferResponse> listOffersForApplication(UUID applicationId) {
        return offerRepository.findByCandidateApplicationId(applicationId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void approveOffer(UUID id) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Offer", id));
        if (!"DRAFT".equals(offer.getStatus()) && !"PENDING_APPROVAL".equals(offer.getStatus())) {
            throw new ConflictException("Offer cannot be approved in its current status: " + offer.getStatus());
        }
        offer.setStatus("APPROVED");
        offerRepository.save(offer);
    }

    @Override
    @Transactional
    public void submitOfferForApproval(UUID id) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Offer", id));
        if (!"DRAFT".equals(offer.getStatus())) {
            throw new ConflictException("Only DRAFT offers can be submitted for approval");
        }

        offer.setStatus("PENDING_APPROVAL");
        offerRepository.save(offer);

        UUID requesterId = CurrentUser.fromSecurityContext()
                .map(CurrentUser::subjectUuid)
                .orElse(UUID.fromString("00000000-0000-0000-0000-000000000000"));

        UUID hiringManagerId = offer.getCandidateApplication().getJobOpening().getJobRequisition().getHiringManager() != null ?
                offer.getCandidateApplication().getJobOpening().getJobRequisition().getHiringManager().getId() : requesterId;

        ApprovalRequest request = ApprovalRequest.builder()
                .employeeId(hiringManagerId)
                .requesterId(requesterId)
                .type("OFFER_APPROVAL")
                .changeJson("{\"offerId\":\"" + id + "\"}")
                .status(ApprovalStatus.PENDING)
                .build();
        request.setTenantId(offer.getTenantId());

        approvalRequestRepository.save(request);
    }

    @Override
    @Transactional
    public void releaseOffer(UUID id) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Offer", id));
        if (!"APPROVED".equals(offer.getStatus())) {
            throw new ConflictException("Offer must be approved before releasing to candidate.");
        }
        offer.setStatus("SENT");
        offerRepository.save(offer);
    }

    @Override
    @Transactional(readOnly = true)
    public OfferResponse getOfferBySecureToken(UUID secureToken) {
        Offer offer = offerRepository.findBySecureToken(secureToken)
                .orElseThrow(() -> NotFoundException.of("Offer", secureToken));
        return toResponse(offer);
    }

    @Override
    @Transactional
    public OfferResponse acceptOffer(UUID secureToken) {
        Offer offer = offerRepository.findBySecureToken(secureToken)
                .orElseThrow(() -> NotFoundException.of("Offer", secureToken));

        if (!"SENT".equals(offer.getStatus())) {
            throw new ConflictException("Offer cannot be accepted. Current status: " + offer.getStatus());
        }

        offer.setStatus("ACCEPTED");
        Offer savedOffer = offerRepository.save(offer);

        // Auto-create PreHire record
        CandidateApplication app = offer.getCandidateApplication();
        JobRequisition req = app.getJobOpening().getJobRequisition();

        PreHire preHire = PreHire.builder()
                .candidate(app.getCandidate())
                .acceptedOffer(savedOffer)
                .legalEntity(req.getLegalEntity())
                .department(req.getDepartment())
                .designation(req.getDesignation())
                .location(req.getLocation())
                .manager(req.getHiringManager())
                .startDate(savedOffer.getStartDate())
                .status("CREATED")
                .build();
        preHire.setTenantId(savedOffer.getTenantId());

        preHireRepository.save(preHire);

        // Advance Candidate Application to HIRED
        app.setCurrentStage("HIRED");
        app.setStatus("HIRED");
        applicationRepository.save(app);

        return toResponse(savedOffer);
    }

    @Override
    @Transactional
    public OfferResponse rejectOffer(UUID secureToken) {
        Offer offer = offerRepository.findBySecureToken(secureToken)
                .orElseThrow(() -> NotFoundException.of("Offer", secureToken));

        if (!"SENT".equals(offer.getStatus())) {
            throw new ConflictException("Offer cannot be rejected. Current status: " + offer.getStatus());
        }

        offer.setStatus("REJECTED");
        Offer saved = offerRepository.save(offer);

        // Update candidate application status
        CandidateApplication app = offer.getCandidateApplication();
        app.setStatus("REJECTED");
        app.setCurrentStage("REJECTED");
        applicationRepository.save(app);

        return toResponse(saved);
    }

    private OfferResponse toResponse(Offer entity) {
        String candidateName = entity.getCandidateApplication().getCandidate().getFirstName() + " " +
                entity.getCandidateApplication().getCandidate().getLastName();
        String candidateEmail = entity.getCandidateApplication().getCandidate().getEmail();
        String jobTitle = entity.getCandidateApplication().getJobOpening().getJobRequisition().getJobTitle();

        return new OfferResponse(
                entity.getId(),
                entity.getCandidateApplication().getId(),
                candidateName,
                candidateEmail,
                jobTitle,
                entity.getSalaryAmount(),
                entity.getCurrencyCode(),
                entity.getStartDate(),
                entity.getStatus(),
                entity.getSecureToken(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion()
        );
    }
}
