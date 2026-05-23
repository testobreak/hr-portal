package com.acme.hrms.project.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.acme.hrms.common.security.CurrentUser;
import com.acme.hrms.project.dto.AllocationCreateRequest;
import com.acme.hrms.project.dto.AllocationResponse;
import com.acme.hrms.project.dto.AllocationUpdateRequest;

public interface AllocationService {

    Page<AllocationResponse> list(CurrentUser caller, String query, Pageable pageable);

    AllocationResponse get(CurrentUser caller, UUID id);

    AllocationResponse create(CurrentUser caller, AllocationCreateRequest request);

    AllocationResponse update(CurrentUser caller, UUID id, AllocationUpdateRequest request);

    void softDelete(CurrentUser caller, UUID id);
}
