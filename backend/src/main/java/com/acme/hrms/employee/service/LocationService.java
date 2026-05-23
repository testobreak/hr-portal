package com.acme.hrms.employee.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.acme.hrms.employee.dto.LocationCreateRequest;
import com.acme.hrms.employee.dto.LocationResponse;
import com.acme.hrms.employee.dto.LocationUpdateRequest;

public interface LocationService {

    Page<LocationResponse> list(String query, Pageable pageable);

    LocationResponse get(UUID id);

    LocationResponse create(LocationCreateRequest request);

    LocationResponse update(UUID id, LocationUpdateRequest request);

    void softDelete(UUID id);
}
