package com.acme.hrms.employee.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
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
import com.acme.hrms.employee.dto.EmployeeContactUpdateRequest;
import com.acme.hrms.employee.dto.EmployeeCreateRequest;
import com.acme.hrms.employee.dto.EmployeeResponse;
import com.acme.hrms.employee.dto.EmployeeSummary;
import com.acme.hrms.employee.dto.EmployeeUpdateRequest;
import com.acme.hrms.employee.entity.Department;
import com.acme.hrms.employee.entity.Designation;
import com.acme.hrms.employee.entity.Employee;
import com.acme.hrms.employee.entity.EmploymentStatus;
import com.acme.hrms.employee.entity.Location;
import com.acme.hrms.employee.mapper.EmployeeMapper;
import com.acme.hrms.employee.repository.DepartmentRepository;
import com.acme.hrms.employee.repository.DesignationRepository;
import com.acme.hrms.employee.repository.EmployeeRepository;
import com.acme.hrms.employee.repository.LocationRepository;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final String ENTITY = "employee";

    private final EmployeeRepository employees;
    private final DepartmentRepository departments;
    private final DesignationRepository designations;
    private final LocationRepository locations;
    private final EmployeeMapper mapper;
    private final AuditService auditService;
    private final Clock clock;

    public EmployeeServiceImpl(EmployeeRepository employees,
                               DepartmentRepository departments,
                               DesignationRepository designations,
                               LocationRepository locations,
                               EmployeeMapper mapper,
                               AuditService auditService,
                               Clock clock) {
        this.employees = employees;
        this.departments = departments;
        this.designations = designations;
        this.locations = locations;
        this.mapper = mapper;
        this.auditService = auditService;
        this.clock = clock;
    }

    // -- Reads -------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeSummary> list(CurrentUser caller, String query, Pageable pageable) {
        Specification<Employee> spec = scopeFor(caller)
                .and(EmployeeSpecifications.textSearch(query));
        return employees.findAll(spec, pageable).map(mapper::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse get(CurrentUser caller, UUID id) {
        Employee row = employees.findById(id)
                .orElseThrow(() -> NotFoundException.of("Employee", id));
        if (!isVisible(caller, row)) {
            // Collapse "not visible to you" with "doesn't exist" to avoid
            // information leakage about whether a row exists at all.
            throw NotFoundException.of("Employee", id);
        }
        EmployeeResponse full = mapper.toResponse(row);
        if (canSeePersonalFields(caller, row)) {
            return full;
        }
        return mapper.redactPersonal(full);
    }

    // -- Writes ------------------------------------------------------------

    @Override
    @Transactional
    public EmployeeResponse create(EmployeeCreateRequest request) {
        employees.findByEmployeeCodeIgnoreCase(request.employeeCode()).ifPresent(e -> {
            throw new ConflictException("Employee with code '" + request.employeeCode() + "' already exists");
        });
        employees.findByEmailIgnoreCase(request.email()).ifPresent(e -> {
            throw new ConflictException("Employee with email '" + request.email() + "' already exists");
        });

        Employee entity = mapper.toEntity(request);
        if (entity.getEmploymentStatus() == null) {
            entity.setEmploymentStatus(EmploymentStatus.ACTIVE);
        }
        attachDepartment(entity, request.departmentId());
        attachDesignation(entity, request.designationId());
        attachLocation(entity, request.locationId());
        attachManager(entity, request.managerId());

        Employee saved = employees.save(entity);
        auditService.record(AuditEvent.of(AuditAction.CREATE, ENTITY)
                .withEntityId(saved.getId())
                .withDetail("code=" + saved.getEmployeeCode()));
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public EmployeeResponse update(UUID id, EmployeeUpdateRequest request) {
        Employee entity = loadOrThrow(id);

        // Email collision against another live row.
        Optional<Employee> emailOwner = employees.findByEmailIgnoreCase(request.email());
        if (emailOwner.isPresent() && !emailOwner.get().getId().equals(id)) {
            throw new ConflictException("Another employee already uses email '" + request.email() + "'");
        }

        entity.setVersion(request.version());
        mapper.apply(request, entity);
        attachDepartment(entity, request.departmentId());
        attachDesignation(entity, request.designationId());
        attachLocation(entity, request.locationId());
        if (request.managerId() != null && request.managerId().equals(id)) {
            throw new ConflictException("Employee cannot be their own manager");
        }
        attachManager(entity, request.managerId());

        Employee saved = employees.save(entity);
        auditService.record(AuditEvent.of(AuditAction.UPDATE, ENTITY)
                .withEntityId(saved.getId()));
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public EmployeeResponse updateContact(CurrentUser caller, UUID id, EmployeeContactUpdateRequest request) {
        Employee entity = loadOrThrow(id);
        boolean isHrOrSuper = caller != null
                && caller.hasAnyRole(Roles.SUPER_ADMIN, Roles.HR_ADMIN);
        boolean isSelf = caller != null
                && caller.subjectUuid() != null
                && caller.subjectUuid().equals(entity.getKeycloakUserId());
        if (!isHrOrSuper && !isSelf) {
            throw new ForbiddenAccessException("Cannot update another employee's contact info");
        }
        entity.setVersion(request.version());
        mapper.applyContact(request, entity);
        Employee saved = employees.save(entity);
        auditService.record(AuditEvent.of(AuditAction.UPDATE, ENTITY)
                .withEntityId(saved.getId())
                .withDetail("contact info"));
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        Employee entity = loadOrThrow(id);
        entity.setDeletedAt(Instant.now(clock));
        employees.save(entity);
        auditService.record(AuditEvent.of(AuditAction.SOFT_DELETE, ENTITY)
                .withEntityId(id));
    }

    @Override
    @Transactional
    public void hardDelete(UUID id) {
        Employee entity = employees.findById(id)
                .orElseThrow(() -> NotFoundException.of("Employee", id));
        employees.delete(entity);
        auditService.record(AuditEvent.of(AuditAction.HARD_DELETE, ENTITY)
                .withEntityId(id));
    }

    // -- Helpers -----------------------------------------------------------

    private LocalDate todayUtc() {
        return LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC);
    }

    private Employee loadOrThrow(UUID id) {
        return employees.findById(id)
                .orElseThrow(() -> NotFoundException.of("Employee", id));
    }

    private Specification<Employee> scopeFor(CurrentUser caller) {
        EmployeeScope scope = EmployeeScope.resolve(caller);
        return switch (scope) {
            case ALL -> EmployeeSpecifications.alwaysTrue();
            case REPORTS -> reportsTreeSpec(caller);
            case SELF -> selfSpec(caller);
            case PROJECT_MEMBERS -> projectMembersSpec(caller);
            case NONE -> EmployeeSpecifications.alwaysFalse();
        };
    }

    private Specification<Employee> reportsTreeSpec(CurrentUser caller) {
        return employees.findByKeycloakUserId(caller.subjectUuid())
                .map(self -> EmployeeSpecifications.idIn(employees.findDescendantIds(self.getId())))
                .orElseGet(EmployeeSpecifications::alwaysFalse);
    }

    private Specification<Employee> selfSpec(CurrentUser caller) {
        return employees.findByKeycloakUserId(caller.subjectUuid())
                .map(self -> EmployeeSpecifications.idEquals(self.getId()))
                .orElseGet(EmployeeSpecifications::alwaysFalse);
    }

    private Specification<Employee> projectMembersSpec(CurrentUser caller) {
        if (caller == null || caller.subjectUuid() == null) {
            return EmployeeSpecifications.alwaysFalse();
        }
        return EmployeeSpecifications.idIn(
                employees.findProjectMemberIdsForManager(caller.subjectUuid(), todayUtc()));
    }

    private boolean isVisible(CurrentUser caller, Employee row) {
        EmployeeScope scope = EmployeeScope.resolve(caller);
        return switch (scope) {
            case ALL -> true;
            case REPORTS -> employees.findByKeycloakUserId(caller.subjectUuid())
                    .map(self -> employees.findDescendantIds(self.getId()).contains(row.getId()))
                    .orElse(false);
            case SELF -> caller.subjectUuid() != null
                    && caller.subjectUuid().equals(row.getKeycloakUserId());
            case PROJECT_MEMBERS -> caller.subjectUuid() != null
                    && employees.findProjectMemberIdsForManager(caller.subjectUuid(), todayUtc())
                            .contains(row.getId());
            case NONE -> false;
        };
    }

    private boolean canSeePersonalFields(CurrentUser caller, Employee row) {
        if (caller == null) {
            return false;
        }
        if (caller.hasAnyRole(Roles.SUPER_ADMIN, Roles.HR_ADMIN, Roles.FINANCE_ADMIN)) {
            return true;
        }
        return caller.subjectUuid() != null
                && caller.subjectUuid().equals(row.getKeycloakUserId());
    }

    private void attachDepartment(Employee entity, UUID id) {
        if (id == null) {
            entity.setDepartment(null);
            return;
        }
        Department d = departments.findById(id)
                .orElseThrow(() -> NotFoundException.of("Department", id));
        entity.setDepartment(d);
    }

    private void attachDesignation(Employee entity, UUID id) {
        if (id == null) {
            entity.setDesignation(null);
            return;
        }
        Designation d = designations.findById(id)
                .orElseThrow(() -> NotFoundException.of("Designation", id));
        entity.setDesignation(d);
    }

    private void attachLocation(Employee entity, UUID id) {
        if (id == null) {
            entity.setLocation(null);
            return;
        }
        Location l = locations.findById(id)
                .orElseThrow(() -> NotFoundException.of("Location", id));
        entity.setLocation(l);
    }

    private void attachManager(Employee entity, UUID id) {
        if (id == null) {
            entity.setManager(null);
            return;
        }
        Employee m = employees.findById(id)
                .orElseThrow(() -> NotFoundException.of("Employee", id));
        entity.setManager(m);
    }
}
