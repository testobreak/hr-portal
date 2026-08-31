package com.acme.hrms.recruitment.controller;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.acme.hrms.common.error.NotFoundException;
import com.acme.hrms.common.storage.StorageProperties;
import com.acme.hrms.common.storage.StorageService;
import com.acme.hrms.recruitment.dto.CandidateApplicationCreateRequest;
import com.acme.hrms.recruitment.dto.CandidateApplicationResponse;
import com.acme.hrms.recruitment.dto.JobPostingResponse;
import com.acme.hrms.recruitment.entity.JobPosting;
import com.acme.hrms.recruitment.repository.JobPostingRepository;
import com.acme.hrms.recruitment.service.CandidateApplicationService;
import com.acme.hrms.recruitment.service.JobPostingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/careers")
@Tag(name = "Careers Board (Public)", description = "Unauthenticated endpoints for public job application boards")
public class PublicCareersController {

    private final JobPostingService jobPostingService;
    private final JobPostingRepository jobPostingRepository;
    private final CandidateApplicationService applicationService;
    private final StorageService storageService;
    private final StorageProperties storageProperties;

    public PublicCareersController(JobPostingService jobPostingService,
                                   JobPostingRepository jobPostingRepository,
                                   CandidateApplicationService applicationService,
                                   StorageService storageService,
                                   StorageProperties storageProperties) {
        this.jobPostingService = jobPostingService;
        this.jobPostingRepository = jobPostingRepository;
        this.applicationService = applicationService;
        this.storageService = storageService;
        this.storageProperties = storageProperties;
    }

    @GetMapping("/jobs")
    @Operation(summary = "Get list of all active public postings")
    public List<JobPostingResponse> listPublicJobs() {
        return jobPostingService.listPublicJobs();
    }

    @GetMapping("/jobs/{publicId}")
    @Operation(summary = "Get public posting details by public UUID")
    public JobPostingResponse getPublicJob(@PathVariable UUID publicId) {
        return jobPostingService.getPublicJobByPublicId(publicId);
    }

    @PostMapping("/resume/presign-upload")
    @Operation(summary = "Create resume presigned upload URL for guests")
    public ResponseEntity<com.acme.hrms.document.dto.PresignUploadResponse> presignUpload(
            @RequestParam(name = "filename") String filename,
            @RequestParam(name = "contentType") String contentType) {
        
        String safeName = filename.replaceAll("[^a-zA-Z0-9.-]", "_");
        String storageKey = "careers/resumes/%s/%s"
                .formatted(UUID.randomUUID(), safeName);

        Duration ttl = Duration.ofSeconds(storageProperties.getPresignTtlSeconds());
        URL url = storageService.presignPut(
                storageProperties.getBucket(),
                storageKey,
                contentType.trim(),
                ttl);

        var response = com.acme.hrms.document.dto.PresignUploadResponse.of(UUID.randomUUID(), url, storageKey);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/jobs/{publicId}/applications")
    @Operation(summary = "Submit a public job application")
    public ResponseEntity<CandidateApplicationResponse> apply(
            @PathVariable UUID publicId,
            @RequestBody @Valid CandidateApplicationCreateRequest request) {

        JobPosting posting = jobPostingRepository.findByPublicId(publicId)
                .orElseThrow(() -> NotFoundException.of("JobPosting", publicId));

        CandidateApplicationResponse response = applicationService.apply(posting.getJobOpening().getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
