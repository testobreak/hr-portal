package com.acme.hrms.employee.service;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
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
import com.acme.hrms.employee.dto.LegalEntityCreateRequest;
import com.acme.hrms.employee.dto.LegalEntityResponse;
import com.acme.hrms.employee.dto.LegalEntityUpdateRequest;
import com.acme.hrms.employee.entity.LegalEntity;
import com.acme.hrms.employee.mapper.LookupMapper;
import com.acme.hrms.employee.repository.LegalEntityRepository;

@Service
public class LegalEntityServiceImpl implements LegalEntityService {

    private static final String ENTITY = "legal_entity";

    private final LegalEntityRepository repository;
    private final LookupMapper mapper;
    private final AuditService auditService;
    private final Clock clock;

    public LegalEntityServiceImpl(LegalEntityRepository repository,
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
    public Page<LegalEntityResponse> list(String query, Pageable pageable) {
        return repository.findAll(LookupSpecifications.legalEntitySearch(query), pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public LegalEntityResponse get(UUID id) {
        return mapper.toResponse(load(id));
    }

    @Override
    @Transactional
    public LegalEntityResponse create(LegalEntityCreateRequest request) {
        repository.findByCodeIgnoreCase(request.code()).ifPresent(d -> {
            throw new ConflictException("Legal Entity with code '" + request.code() + "' already exists");
        });
        LegalEntity saved = repository.save(mapper.toEntity(request));
        auditService.record(AuditEvent.of(AuditAction.CREATE, ENTITY)
                .withEntityId(saved.getId())
                .withDetail("code=" + saved.getCode()));
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public LegalEntityResponse update(UUID id, LegalEntityUpdateRequest request) {
        LegalEntity entity = load(id);

        if (!Objects.equals(request.version(), entity.getVersion())) {
            throw new ConflictException("Legal Entity was modified by another request. Please reload and try again.");
        }

        mapper.apply(request, entity);
        LegalEntity saved = repository.saveAndFlush(entity);
        auditService.record(AuditEvent.of(AuditAction.UPDATE, ENTITY)
                .withEntityId(saved.getId()));
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        LegalEntity entity = load(id);
        entity.setDeletedAt(Instant.now(clock));
        repository.save(entity);
        auditService.record(AuditEvent.of(AuditAction.SOFT_DELETE, ENTITY)
                .withEntityId(id));
    }

    private LegalEntity load(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> NotFoundException.of("LegalEntity", id));
    }
}
