package com.acme.hrms.project.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.acme.hrms.common.security.CurrentUser;
import com.acme.hrms.project.dto.ProjectCreateRequest;
import com.acme.hrms.project.dto.ProjectResponse;
import com.acme.hrms.project.dto.ProjectUpdateRequest;

public interface ProjectService {

    Page<ProjectResponse> list(CurrentUser caller, String query, Pageable pageable);

    ProjectResponse get(CurrentUser caller, UUID id);

    ProjectResponse create(CurrentUser caller, ProjectCreateRequest request);

    ProjectResponse update(CurrentUser caller, UUID id, ProjectUpdateRequest request);

    void softDelete(CurrentUser caller, UUID id);
}
