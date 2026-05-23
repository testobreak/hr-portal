package com.acme.hrms.project.service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acme.hrms.common.audit.AuditAction;
import com.acme.hrms.common.audit.AuditEvent;
import com.acme.hrms.common.audit.AuditService;
import com.acme.hrms.common.error.ConflictException;
import com.acme.hrms.common.error.NotFoundException;
import com.acme.hrms.common.security.CurrentUser;
import com.acme.hrms.common.security.Roles;
import com.acme.hrms.project.dto.ClientCreateRequest;
import com.acme.hrms.project.dto.ClientResponse;
import com.acme.hrms.project.dto.ClientUpdateRequest;
import com.acme.hrms.project.entity.Client;
import com.acme.hrms.project.mapper.ProjectMapper;
import com.acme.hrms.project.repository.ClientRepository;

@Service
public class ClientServiceImpl implements ClientService {

    private static final String ENTITY = "client";

    private final ClientRepository clients;
    private final ProjectMapper mapper;
    private final AuditService auditService;
    private final Clock clock;

    public ClientServiceImpl(ClientRepository clients,
                             ProjectMapper mapper,
                             AuditService auditService,
                             Clock clock) {
        this.clients = clients;
        this.mapper = mapper;
        this.auditService = auditService;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientResponse> list(CurrentUser caller, String query, Pageable pageable) {
        return clients.findAll(scopeFor(caller).and(ClientSpecifications.textSearch(query)), pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponse get(CurrentUser caller, UUID id) {
        Client row = clients.findOne(scopeFor(caller).and(ClientSpecifications.idEquals(id)))
                .orElseThrow(() -> NotFoundException.of("Client", id));
        return mapper.toResponse(row);
    }

    @Override
    @Transactional
    public ClientResponse create(ClientCreateRequest request) {
        clients.findByCodeIgnoreCase(request.code()).ifPresent(c -> {
            throw new ConflictException("Client with code '" + request.code() + "' already exists");
        });
        Client saved = clients.save(mapper.toEntity(request));
        auditService.record(AuditEvent.of(AuditAction.CREATE, ENTITY)
                .withEntityId(saved.getId())
                .withDetail("code=" + saved.getCode()));
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ClientResponse update(UUID id, ClientUpdateRequest request) {
        Client entity = load(id);
        entity.setVersion(request.version());
        mapper.apply(request, entity);
        Client saved = clients.save(entity);
        auditService.record(AuditEvent.of(AuditAction.UPDATE, ENTITY)
                .withEntityId(saved.getId()));
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        Client entity = load(id);
        entity.setDeletedAt(Instant.now(clock));
        clients.save(entity);
        auditService.record(AuditEvent.of(AuditAction.SOFT_DELETE, ENTITY)
                .withEntityId(id));
    }

    private Client load(UUID id) {
        return clients.findById(id)
                .orElseThrow(() -> NotFoundException.of("Client", id));
    }

    private Specification<Client> scopeFor(CurrentUser caller) {
        if (caller == null) {
            return ClientSpecifications.alwaysFalse();
        }
        if (caller.hasAnyRole(Roles.SUPER_ADMIN, Roles.HR_ADMIN, Roles.FINANCE_ADMIN,
                Roles.LEADERSHIP, Roles.MANAGER)) {
            return ClientSpecifications.alwaysTrue();
        }
        if (caller.hasRole(Roles.PROJECT_MANAGER)) {
            return ClientSpecifications.managedBy(caller.subjectUuid());
        }
        return ClientSpecifications.alwaysFalse();
    }
}
