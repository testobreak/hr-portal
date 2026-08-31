package com.acme.hrms.recruitment.service;

import java.util.List;
import java.util.UUID;

import com.acme.hrms.recruitment.dto.JobRequisitionCreateRequest;
import com.acme.hrms.recruitment.dto.JobRequisitionResponse;

public interface JobRequisitionService {
    JobRequisitionResponse create(JobRequisitionCreateRequest request);
    List<JobRequisitionResponse> listAll();
    JobRequisitionResponse getById(UUID id);
    JobRequisitionResponse update(UUID id, JobRequisitionCreateRequest request);
    void submit(UUID id);
    void approve(UUID id);
    void reject(UUID id);
    void close(UUID id);
}
