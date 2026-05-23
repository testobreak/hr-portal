package com.acme.hrms.employee.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.acme.hrms.employee.dto.DesignationCreateRequest;
import com.acme.hrms.employee.dto.DesignationResponse;
import com.acme.hrms.employee.dto.DesignationUpdateRequest;

public interface DesignationService {

    Page<DesignationResponse> list(String query, Pageable pageable);

    DesignationResponse get(UUID id);

    DesignationResponse create(DesignationCreateRequest request);

    DesignationResponse update(UUID id, DesignationUpdateRequest request);

    void softDelete(UUID id);
}
