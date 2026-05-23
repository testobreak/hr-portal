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
import com.acme.hrms.employee.dto.LocationCreateRequest;
import com.acme.hrms.employee.dto.LocationResponse;
import com.acme.hrms.employee.dto.LocationUpdateRequest;
import com.acme.hrms.employee.entity.Location;
import com.acme.hrms.employee.mapper.LookupMapper;
import com.acme.hrms.employee.repository.LocationRepository;

@Service
public class LocationServiceImpl implements LocationService {

    private static final String ENTITY = "location";

    private final LocationRepository repository;
    private final LookupMapper mapper;
    private final AuditService auditService;
    private final Clock clock;

    public LocationServiceImpl(LocationRepository repository,
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
    public Page<LocationResponse> list(String query, Pageable pageable) {
        return repository.findAll(LookupSpecifications.locationSearch(query), pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public LocationResponse get(UUID id) {
        return mapper.toResponse(load(id));
    }

    @Override
    @Transactional
    public LocationResponse create(LocationCreateRequest request) {
        repository.findByCodeIgnoreCase(request.code()).ifPresent(d -> {
            throw new ConflictException("Location with code '" + request.code() + "' already exists");
        });
        Location saved = repository.save(mapper.toEntity(request));
        auditService.record(AuditEvent.of(AuditAction.CREATE, ENTITY)
                .withEntityId(saved.getId())
                .withDetail("code=" + saved.getCode()));
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public LocationResponse update(UUID id, LocationUpdateRequest request) {
        Location entity = load(id);
        entity.setVersion(request.version());
        mapper.apply(request, entity);
        Location saved = repository.save(entity);
        auditService.record(AuditEvent.of(AuditAction.UPDATE, ENTITY)
                .withEntityId(saved.getId()));
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        Location entity = load(id);
        entity.setDeletedAt(Instant.now(clock));
        repository.save(entity);
        auditService.record(AuditEvent.of(AuditAction.SOFT_DELETE, ENTITY)
                .withEntityId(id));
    }

    private Location load(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Location", id));
    }
}
