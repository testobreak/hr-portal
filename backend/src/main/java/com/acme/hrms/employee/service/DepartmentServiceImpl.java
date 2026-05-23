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
import com.acme.hrms.employee.dto.DepartmentCreateRequest;
import com.acme.hrms.employee.dto.DepartmentResponse;
import com.acme.hrms.employee.dto.DepartmentUpdateRequest;
import com.acme.hrms.employee.entity.Department;
import com.acme.hrms.employee.mapper.LookupMapper;
import com.acme.hrms.employee.repository.DepartmentRepository;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private static final String ENTITY = "department";

    private final DepartmentRepository repository;
    private final LookupMapper mapper;
    private final AuditService auditService;
    private final Clock clock;

    public DepartmentServiceImpl(DepartmentRepository repository,
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
    public Page<DepartmentResponse> list(String query, Pageable pageable) {
        return repository.findAll(LookupSpecifications.departmentSearch(query), pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse get(UUID id) {
        return mapper.toResponse(load(id));
    }

    @Override
    @Transactional
    public DepartmentResponse create(DepartmentCreateRequest request) {
        repository.findByCodeIgnoreCase(request.code()).ifPresent(d -> {
            throw new ConflictException("Department with code '" + request.code() + "' already exists");
        });
        Department saved = repository.save(mapper.toEntity(request));
        auditService.record(AuditEvent.of(AuditAction.CREATE, ENTITY)
                .withEntityId(saved.getId())
                .withDetail("code=" + saved.getCode()));
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public DepartmentResponse update(UUID id, DepartmentUpdateRequest request) {
        Department entity = load(id);

        // Explicit stale-write guard: reject if caller edited an older version.
        if (!Objects.equals(request.version(), entity.getVersion())) {
            throw new ConflictException("Department was modified by another request. Please reload and try again.");
        }

        mapper.apply(request, entity);
        Department saved = repository.saveAndFlush(entity);
        auditService.record(AuditEvent.of(AuditAction.UPDATE, ENTITY)
                .withEntityId(saved.getId()));
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        Department entity = load(id);
        entity.setDeletedAt(Instant.now(clock));
        repository.save(entity);
        auditService.record(AuditEvent.of(AuditAction.SOFT_DELETE, ENTITY)
                .withEntityId(id));
    }

    private Department load(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Department", id));
    }
}
