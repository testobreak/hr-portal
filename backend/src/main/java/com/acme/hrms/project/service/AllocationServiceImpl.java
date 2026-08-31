package com.acme.hrms.project.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
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
import com.acme.hrms.common.error.ForbiddenAccessException;
import com.acme.hrms.common.error.NotFoundException;
import com.acme.hrms.common.security.CurrentUser;
import com.acme.hrms.common.security.Roles;
import com.acme.hrms.employee.entity.Employee;
import com.acme.hrms.employee.repository.EmployeeRepository;
import com.acme.hrms.project.dto.AllocationCreateRequest;
import com.acme.hrms.project.dto.AllocationResponse;
import com.acme.hrms.project.dto.AllocationUpdateRequest;
import com.acme.hrms.project.entity.Allocation;
import com.acme.hrms.project.entity.Project;
import com.acme.hrms.project.mapper.ProjectMapper;
import com.acme.hrms.project.repository.AllocationRepository;
import com.acme.hrms.project.repository.ProjectRepository;

@Service
public class AllocationServiceImpl implements AllocationService {

    private static final String ENTITY = "allocation";

    private final AllocationRepository allocations;
    private final ProjectRepository projects;
    private final EmployeeRepository employees;
    private final ProjectMapper mapper;
    private final AuditService auditService;
    private final Clock clock;

    public AllocationServiceImpl(AllocationRepository allocations,
                                 ProjectRepository projects,
                                 EmployeeRepository employees,
                                 ProjectMapper mapper,
                                 AuditService auditService,
                                 Clock clock) {
        this.allocations = allocations;
        this.projects = projects;
        this.employees = employees;
        this.mapper = mapper;
        this.auditService = auditService;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AllocationResponse> list(CurrentUser caller, String query, Pageable pageable) {
        return allocations.findAll(scopeFor(caller).and(AllocationSpecifications.textSearch(query)), pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AllocationResponse get(CurrentUser caller, UUID id) {
        Allocation row = allocations.findOne(scopeFor(caller).and(AllocationSpecifications.idEquals(id)))
                .orElseThrow(() -> NotFoundException.of("Allocation", id));
        return mapper.toResponse(row);
    }

    @Override
    @Transactional
    public AllocationResponse create(CurrentUser caller, AllocationCreateRequest request) {
        validateDateRange(request.startDate(), request.endDate(), "Allocation");
        Project project = loadProject(request.projectId());
        assertCanManageAllocationWrite(caller, project);

        Allocation entity = mapper.toEntity(request);
        entity.setProject(project);
        entity.setEmployee(loadEmployee(request.employeeId()));

        Allocation saved = allocations.save(entity);
        auditService.record(AuditEvent.of(AuditAction.CREATE, ENTITY)
                .withEntityId(saved.getId())
                .withDetail("project=" + saved.getProject().getProjectCode()));
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AllocationResponse update(CurrentUser caller, UUID id, AllocationUpdateRequest request) {
        Allocation entity = load(id);
        assertCanManageAllocationWrite(caller, entity.getProject());
        validateDateRange(request.startDate(), request.endDate(), "Allocation");
        Project requestedProject = loadProject(request.projectId());
        assertCanManageAllocationWrite(caller, requestedProject);

        entity.setVersion(request.version());
        mapper.apply(request, entity);
        entity.setProject(requestedProject);
        entity.setEmployee(loadEmployee(request.employeeId()));

        Allocation saved = allocations.save(entity);
        auditService.record(AuditEvent.of(AuditAction.UPDATE, ENTITY)
                .withEntityId(saved.getId()));
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void softDelete(CurrentUser caller, UUID id) {
        Allocation entity = load(id);
        assertCanManageAllocationWrite(caller, entity.getProject());
        entity.setDeletedAt(Instant.now(clock));
        allocations.save(entity);
        auditService.record(AuditEvent.of(AuditAction.SOFT_DELETE, ENTITY)
                .withEntityId(id));
    }

    private Allocation load(UUID id) {
        return allocations.findById(id)
                .orElseThrow(() -> NotFoundException.of("Allocation", id));
    }

    private Project loadProject(UUID id) {
        return projects.findById(id)
                .orElseThrow(() -> NotFoundException.of("Project", id));
    }

    private Employee loadEmployee(UUID id) {
        return employees.findById(id)
                .orElseThrow(() -> NotFoundException.of("Employee", id));
    }

    private Specification<Allocation> scopeFor(CurrentUser caller) {
        if (caller == null) {
            return AllocationSpecifications.alwaysFalse();
        }
        if (caller.hasAnyRole(Roles.SUPER_ADMIN, Roles.HR_ADMIN, Roles.FINANCE_ADMIN, Roles.LEADERSHIP)) {
            return AllocationSpecifications.alwaysTrue();
        }
        if (caller.hasRole(Roles.MANAGER)) {
            return employees.findById(caller.subjectUuid())
                    .map(self -> AllocationSpecifications.employeeIdIn(employees.findDescendantIds(self.getId())))
                    .orElseGet(AllocationSpecifications::alwaysFalse);
        }
        if (caller.hasRole(Roles.PROJECT_MANAGER)) {
            return AllocationSpecifications.ownProjects(caller.subjectUuid());
        }
        if (caller.hasRole(Roles.EMPLOYEE)) {
            return AllocationSpecifications.self(caller.subjectUuid());
        }
        return AllocationSpecifications.alwaysFalse();
    }

    private void assertCanManageAllocationWrite(CurrentUser caller, Project project) {
        if (caller != null && caller.hasAnyRole(Roles.SUPER_ADMIN, Roles.HR_ADMIN, Roles.FINANCE_ADMIN)) {
            return;
        }
        if (caller != null && caller.hasRole(Roles.PROJECT_MANAGER)
                && project.getProjectManager() != null
                && caller.subjectUuid() != null
                && caller.subjectUuid().equals(project.getProjectManager().getId())) {
            return;
        }
        throw new ForbiddenAccessException("Cannot manage allocations for this project");
    }

    private void validateDateRange(LocalDate start, LocalDate end, String label) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new ConflictException(label + " endDate cannot be before startDate");
        }
    }
}
