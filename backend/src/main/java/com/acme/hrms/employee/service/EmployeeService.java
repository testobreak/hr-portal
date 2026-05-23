package com.acme.hrms.employee.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.acme.hrms.common.security.CurrentUser;
import com.acme.hrms.employee.dto.EmployeeContactUpdateRequest;
import com.acme.hrms.employee.dto.EmployeeCreateRequest;
import com.acme.hrms.employee.dto.EmployeeResponse;
import com.acme.hrms.employee.dto.EmployeeSummary;
import com.acme.hrms.employee.dto.EmployeeUpdateRequest;

public interface EmployeeService {

    Page<EmployeeSummary> list(CurrentUser caller, String query, Pageable pageable);

    EmployeeResponse get(CurrentUser caller, UUID id);

    EmployeeResponse create(EmployeeCreateRequest request);

    EmployeeResponse update(UUID id, EmployeeUpdateRequest request);

    EmployeeResponse updateContact(CurrentUser caller, UUID id, EmployeeContactUpdateRequest request);

    void softDelete(UUID id);

    void hardDelete(UUID id);
}
