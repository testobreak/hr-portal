package com.acme.hrms.project.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
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
import com.acme.hrms.project.dto.ProjectCreateRequest;
import com.acme.hrms.project.dto.ProjectResponse;
import com.acme.hrms.project.dto.ProjectUpdateRequest;
import com.acme.hrms.project.entity.Client;
import com.acme.hrms.project.entity.Project;
import com.acme.hrms.project.entity.ProjectStatus;
import com.acme.hrms.project.mapper.ProjectMapper;
import com.acme.hrms.project.repository.ClientRepository;
import com.acme.hrms.project.repository.ProjectRepository;

@Service
public class ProjectServiceImpl implements ProjectService {

    private static final String ENTITY = "project";

    private final ProjectRepository projects;
    private final ClientRepository clients;
    private final EmployeeRepository employees;
    private final ProjectMapper mapper;
    private final AuditService auditService;
    private final Clock clock;

    public ProjectServiceImpl(ProjectRepository projects,
                              ClientRepository clients,
                              EmployeeRepository employees,
                              ProjectMapper mapper,
                              AuditService auditService,
                              Clock clock) {
        this.projects = projects;
        this.clients = clients;
        this.employees = employees;
        this.mapper = mapper;
        this.auditService = auditService;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponse> list(CurrentUser caller, String query, Pageable pageable) {
        return projects.findAll(scopeFor(caller).and(ProjectSpecifications.textSearch(query)), pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse get(CurrentUser caller, UUID id) {
        Project row = projects.findOne(scopeFor(caller).and(ProjectSpecifications.idEquals(id)))
                .orElseThrow(() -> NotFoundException.of("Project", id));
        return mapper.toResponse(row);
    }

    @Override
    @Transactional
    public ProjectResponse create(CurrentUser caller, ProjectCreateRequest request) {
        assertCanManageProjectWrite(caller, request.projectManagerId());
        validateDateRange(request.startDate(), request.endDate(), "Project");
        projects.findByProjectCodeIgnoreCase(request.projectCode()).ifPresent(p -> {
            throw new ConflictException("Project with code '" + request.projectCode() + "' already exists");
        });

        Project entity = mapper.toEntity(request);
        if (entity.getStatus() == null) {
            entity.setStatus(ProjectStatus.ACTIVE);
        }
        attachClient(entity, request.clientId());
        attachProjectManager(entity, request.projectManagerId());

        Project saved = projects.save(entity);
        auditService.record(AuditEvent.of(AuditAction.CREATE, ENTITY)
                .withEntityId(saved.getId())
                .withDetail("code=" + saved.getProjectCode()));
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ProjectResponse update(CurrentUser caller, UUID id, ProjectUpdateRequest request) {
        Project entity = load(id);
        assertCanManageExistingProject(caller, entity);
        assertCanManageProjectWrite(caller, request.projectManagerId());
        validateDateRange(request.startDate(), request.endDate(), "Project");

        entity.setVersion(request.version());
        mapper.apply(request, entity);
        attachClient(entity, request.clientId());
        attachProjectManager(entity, request.projectManagerId());

        Project saved = projects.save(entity);
        auditService.record(AuditEvent.of(AuditAction.UPDATE, ENTITY)
                .withEntityId(saved.getId()));
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void softDelete(CurrentUser caller, UUID id) {
        Project entity = load(id);
        assertCanManageExistingProject(caller, entity);
        entity.setDeletedAt(Instant.now(clock));
        projects.save(entity);
        auditService.record(AuditEvent.of(AuditAction.SOFT_DELETE, ENTITY)
                .withEntityId(id));
    }

    private Project load(UUID id) {
        return projects.findById(id)
                .orElseThrow(() -> NotFoundException.of("Project", id));
    }

    private Specification<Project> scopeFor(CurrentUser caller) {
        if (caller == null) {
            return ProjectSpecifications.alwaysFalse();
        }
        if (caller.hasAnyRole(Roles.SUPER_ADMIN, Roles.HR_ADMIN, Roles.FINANCE_ADMIN,
                Roles.LEADERSHIP, Roles.MANAGER)) {
            return ProjectSpecifications.alwaysTrue();
        }
        if (caller.hasRole(Roles.PROJECT_MANAGER)) {
            return ProjectSpecifications.ownedOrMemberOf(caller.subjectUuid());
        }
        if (caller.hasRole(Roles.EMPLOYEE)) {
            return ProjectSpecifications.memberOf(caller.subjectUuid());
        }
        return ProjectSpecifications.alwaysFalse();
    }

    private void attachClient(Project entity, UUID id) {
        Client client = clients.findById(id)
                .orElseThrow(() -> NotFoundException.of("Client", id));
        entity.setClient(client);
    }

    private void attachProjectManager(Project entity, UUID id) {
        if (id == null) {
            entity.setProjectManager(null);
            return;
        }
        Employee manager = employees.findById(id)
                .orElseThrow(() -> NotFoundException.of("Employee", id));
        entity.setProjectManager(manager);
    }

    private void assertCanManageExistingProject(CurrentUser caller, Project project) {
        if (caller != null && caller.hasAnyRole(Roles.SUPER_ADMIN, Roles.FINANCE_ADMIN)) {
            return;
        }
        if (caller != null && caller.hasRole(Roles.PROJECT_MANAGER)
                && project.getProjectManager() != null
                && caller.subjectUuid() != null
                && caller.subjectUuid().equals(project.getProjectManager().getId())) {
            return;
        }
        throw new ForbiddenAccessException("Cannot manage this project");
    }

    private void assertCanManageProjectWrite(CurrentUser caller, UUID requestedProjectManagerId) {
        if (caller != null && caller.hasAnyRole(Roles.SUPER_ADMIN, Roles.FINANCE_ADMIN)) {
            return;
        }
        if (caller != null && caller.hasRole(Roles.PROJECT_MANAGER)) {
            Optional<Employee> currentEmployee = employees.findById(caller.subjectUuid());
            if (currentEmployee.isPresent() && currentEmployee.get().getId().equals(requestedProjectManagerId)) {
                return;
            }
        }
        throw new ForbiddenAccessException("PROJECT_MANAGER can only manage projects assigned to themselves");
    }

    private void validateDateRange(LocalDate start, LocalDate end, String label) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new ConflictException(label + " endDate cannot be before startDate");
        }
    }
}
