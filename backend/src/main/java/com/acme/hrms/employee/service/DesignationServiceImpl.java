package com.acme.hrms.employee.service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acme.hrms.common.audit.AuditAction;
import com.acme.hrms.common.audit.AuditEvent;
import com.acme.hrms.common.audit.AuditService;
import com.acme.hrms.common.error.ConflictException;
import com.acme.hrms.common.error.NotFoundException;
import com.acme.hrms.employee.dto.DesignationCreateRequest;
import com.acme.hrms.employee.dto.DesignationResponse;
import com.acme.hrms.employee.dto.DesignationUpdateRequest;
import com.acme.hrms.employee.entity.Designation;
import com.acme.hrms.employee.mapper.LookupMapper;
import com.acme.hrms.employee.repository.DesignationRepository;

@Service
public class DesignationServiceImpl implements DesignationService {

    private static final String ENTITY = "designation";

    private final DesignationRepository repository;
    private final LookupMapper mapper;
    private final AuditService auditService;
    private final Clock clock;

    public DesignationServiceImpl(DesignationRepository repository,
                                  LookupMapper mapper,
                                  AuditService auditService,
                                  Clock clock) {
        this.repository = repository;
        this.mapper = mapper;
        this.auditService = auditService;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DesignationResponse> list(String query, Pageable pageable) {
        return repository.findAll(LookupSpecifications.designationSearch(query), pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DesignationResponse get(UUID id) {
        return mapper.toResponse(load(id));
    }

    @Override
    @Transactional
    public DesignationResponse create(DesignationCreateRequest request) {
        repository.findByTitleIgnoreCase(request.title()).ifPresent(d -> {
            throw new ConflictException("Designation '" + request.title() + "' already exists");
        });
        Designation saved = repository.save(mapper.toEntity(request));
        auditService.record(AuditEvent.of(AuditAction.CREATE, ENTITY)
                .withEntityId(saved.getId())
                .withDetail("title=" + saved.getTitle()));
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public DesignationResponse update(UUID id, DesignationUpdateRequest request) {
        Designation entity = load(id);
        entity.setVersion(request.version());
        mapper.apply(request, entity);
        Designation saved = repository.save(entity);
        auditService.record(AuditEvent.of(AuditAction.UPDATE, ENTITY)
                .withEntityId(saved.getId()));
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        Designation entity = load(id);
        entity.setDeletedAt(Instant.now(clock));
        repository.save(entity);
        auditService.record(AuditEvent.of(AuditAction.SOFT_DELETE, ENTITY)
                .withEntityId(id));
    }

    private Designation load(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Designation", id));
    }
}
