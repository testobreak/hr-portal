package com.acme.hrms.employee.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.acme.hrms.employee.dto.DepartmentCreateRequest;
import com.acme.hrms.employee.dto.DepartmentResponse;
import com.acme.hrms.employee.dto.DepartmentUpdateRequest;

public interface DepartmentService {

    Page<DepartmentResponse> list(String query, Pageable pageable);

    DepartmentResponse get(UUID id);

    DepartmentResponse create(DepartmentCreateRequest request);

    DepartmentResponse update(UUID id, DepartmentUpdateRequest request);

    void softDelete(UUID id);
}
