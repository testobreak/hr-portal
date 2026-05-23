package com.acme.hrms.project.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.acme.hrms.common.security.CurrentUser;
import com.acme.hrms.project.dto.ClientCreateRequest;
import com.acme.hrms.project.dto.ClientResponse;
import com.acme.hrms.project.dto.ClientUpdateRequest;

public interface ClientService {

    Page<ClientResponse> list(CurrentUser caller, String query, Pageable pageable);

    ClientResponse get(CurrentUser caller, UUID id);

    ClientResponse create(ClientCreateRequest request);

    ClientResponse update(UUID id, ClientUpdateRequest request);

    void softDelete(UUID id);
}
