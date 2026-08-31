package com.acme.hrms.recruitment.service;

import java.util.List;
import java.util.UUID;

import com.acme.hrms.recruitment.dto.JobOpeningResponse;
import com.acme.hrms.recruitment.dto.JobPostingCreateRequest;
import com.acme.hrms.recruitment.dto.JobPostingResponse;

public interface JobPostingService {
    JobOpeningResponse createOpening(UUID requisitionId);
    JobPostingResponse createPosting(UUID openingId, JobPostingCreateRequest request);
    JobPostingResponse getPostingById(UUID id);
    void publish(UUID id);
    void unpublish(UUID id);
    List<JobPostingResponse> listAllPostings();
    List<JobPostingResponse> listPublicJobs();
    JobPostingResponse getPublicJobByPublicId(UUID publicId);
}
