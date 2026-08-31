package com.acme.hrms.employee.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.acme.hrms.employee.dto.LegalEntityCreateRequest;
import com.acme.hrms.employee.dto.LegalEntityResponse;
import com.acme.hrms.employee.dto.LegalEntityUpdateRequest;

public interface LegalEntityService {
    Page<LegalEntityResponse> list(String query, Pageable pageable);
    LegalEntityResponse get(UUID id);
    LegalEntityResponse create(LegalEntityCreateRequest request);
    LegalEntityResponse update(UUID id, LegalEntityUpdateRequest request);
    void softDelete(UUID id);
}
