package com.acme.hrms.recruitment.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acme.hrms.common.error.ConflictException;
import com.acme.hrms.common.error.NotFoundException;
import com.acme.hrms.recruitment.dto.JobOpeningResponse;
import com.acme.hrms.recruitment.dto.JobPostingCreateRequest;
import com.acme.hrms.recruitment.dto.JobPostingResponse;
import com.acme.hrms.recruitment.entity.JobOpening;
import com.acme.hrms.recruitment.entity.JobPosting;
import com.acme.hrms.recruitment.entity.JobRequisition;
import com.acme.hrms.recruitment.repository.JobOpeningRepository;
import com.acme.hrms.recruitment.repository.JobPostingRepository;
import com.acme.hrms.recruitment.repository.JobRequisitionRepository;

@Service
public class JobPostingServiceImpl implements JobPostingService {

    private final JobPostingRepository repository;
    private final JobOpeningRepository openingRepository;
    private final JobRequisitionRepository requisitionRepository;

    public JobPostingServiceImpl(JobPostingRepository repository,
                                 JobOpeningRepository openingRepository,
                                 JobRequisitionRepository requisitionRepository) {
        this.repository = repository;
        this.openingRepository = openingRepository;
        this.requisitionRepository = requisitionRepository;
    }

    @Override
    @Transactional
    public JobOpeningResponse createOpening(UUID requisitionId) {
        JobRequisition req = requisitionRepository.findById(requisitionId)
                .orElseThrow(() -> NotFoundException.of("JobRequisition", requisitionId));

        if (!"APPROVED".equals(req.getStatus())) {
            throw new ConflictException("Requisition must be approved to open a job slot");
        }

        JobOpening opening = JobOpening.builder()
                .jobRequisition(req)
                .status("OPEN")
                .build();
        opening.setTenantId(req.getTenantId());
        JobOpening saved = openingRepository.save(opening);

        return new JobOpeningResponse(
                saved.getId(),
                saved.getJobRequisition().getId(),
                saved.getStatus(),
                saved.getCreatedAt(),
                saved.getUpdatedAt(),
                saved.getVersion()
        );
    }

    @Override
    @Transactional
    public JobPostingResponse createPosting(UUID openingId, JobPostingCreateRequest request) {
        JobOpening opening = openingRepository.findById(openingId)
                .orElseThrow(() -> NotFoundException.of("JobOpening", openingId));

        JobPosting posting = JobPosting.builder()
                .jobOpening(opening)
                .title(request.title())
                .description(request.description())
                .locationName(request.locationName())
                .workArrangement(request.workArrangement())
                .employmentType(request.employmentType())
                .applicationDeadline(request.applicationDeadline())
                .status("DRAFT")
                .build();
        posting.setTenantId(opening.getTenantId());

        JobPosting saved = repository.save(posting);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public JobPostingResponse getPostingById(UUID id) {
        JobPosting posting = repository.findById(id)
                .orElseThrow(() -> NotFoundException.of("JobPosting", id));
        return toResponse(posting);
    }

    @Override
    @Transactional
    public void publish(UUID id) {
        JobPosting posting = repository.findById(id)
                .orElseThrow(() -> NotFoundException.of("JobPosting", id));
        posting.setStatus("PUBLISHED");
        repository.save(posting);
    }

    @Override
    @Transactional
    public void unpublish(UUID id) {
        JobPosting posting = repository.findById(id)
                .orElseThrow(() -> NotFoundException.of("JobPosting", id));
        posting.setStatus("UNPUBLISHED");
        repository.save(posting);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobPostingResponse> listAllPostings() {
        return repository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobPostingResponse> listPublicJobs() {
        return repository.findAllByStatus("PUBLISHED").stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public JobPostingResponse getPublicJobByPublicId(UUID publicId) {
        JobPosting posting = repository.findByPublicId(publicId)
                .orElseThrow(() -> NotFoundException.of("JobPosting", publicId));

        if (!"PUBLISHED".equals(posting.getStatus())) {
            throw new NotFoundException("JobPosting is not publicly accessible");
        }

        return toResponse(posting);
    }

    private JobPostingResponse toResponse(JobPosting entity) {
        return new JobPostingResponse(
                entity.getId(),
                entity.getJobOpening().getId(),
                entity.getPublicId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getLocationName(),
                entity.getWorkArrangement(),
                entity.getEmploymentType(),
                entity.getApplicationDeadline(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion()
        );
    }
}
