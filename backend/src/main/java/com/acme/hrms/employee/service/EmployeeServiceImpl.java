package com.acme.hrms.employee.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import com.acme.hrms.employee.entity.LegalEntity;
import com.acme.hrms.employee.mapper.EmployeeMapper;
import com.acme.hrms.employee.repository.DepartmentRepository;
import com.acme.hrms.employee.repository.DesignationRepository;
import com.acme.hrms.employee.repository.EmployeeRepository;
import com.acme.hrms.employee.repository.LocationRepository;
import com.acme.hrms.employee.repository.LegalEntityRepository;
import com.acme.hrms.manager.service.ManagerService;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final String ENTITY = "employee";

    private final EmployeeRepository employees;
    private final DepartmentRepository departments;
    private final DesignationRepository designations;
    private final LocationRepository locations;
    private final LegalEntityRepository legalEntities;
    private final EmployeeMapper mapper;
    private final AuditService auditService;
    private final EmployeeAssignmentHistoryService assignmentHistoryService;
    private final ManagerService managerService;
    private final Clock clock;
    private final PasswordEncoder passwordEncoder;

    public EmployeeServiceImpl(EmployeeRepository employees,
                               DepartmentRepository departments,
                               DesignationRepository designations,
                               LocationRepository locations,
                               LegalEntityRepository legalEntities,
                               EmployeeMapper mapper,
                               AuditService auditService,
                               EmployeeAssignmentHistoryService assignmentHistoryService,
                               ManagerService managerService,
                               Clock clock,
                               PasswordEncoder passwordEncoder) {
        this.employees = employees;
        this.departments = departments;
        this.designations = designations;
        this.locations = locations;
        this.legalEntities = legalEntities;
        this.mapper = mapper;
        this.auditService = auditService;
        this.assignmentHistoryService = assignmentHistoryService;
        this.managerService = managerService;
        this.clock = clock;
        this.passwordEncoder = passwordEncoder;
    }

    // -- Reads -------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeSummary> list(CurrentUser caller, String query, Pageable pageable) {
        Specification<Employee> spec = scopeFor(caller);
        if (query != null && !query.isBlank()) {
            spec = spec.and(EmployeeSpecifications.textSearch(query));
        }
        return employees.findAll(spec, pageable).map(mapper::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse get(CurrentUser caller, UUID id) {
        Employee row = loadOrThrow(id);
        if (!isVisible(caller, row)) {
            // Emulate 404 on out-of-scope read to deny record existence.
            throw NotFoundException.of(ENTITY, id);
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
        if (employees.findByEmailIgnoreCase(request.email()).isPresent()) {
            throw new ConflictException("An employee already exists with email '" + request.email() + "'");
        }

        Employee entity = mapper.toEntity(request);
        if (request.keycloakUserId() != null) {
            entity.setId(request.keycloakUserId());
        }
        if (entity.getEmploymentStatus() == null) {
            entity.setEmploymentStatus(com.acme.hrms.employee.entity.EmploymentStatus.ACTIVE);
        }
        attachDepartment(entity, request.departmentId());
        attachDesignation(entity, request.designationId());
        attachLocation(entity, request.locationId());
        attachLegalEntity(entity, request.legalEntityId());
        attachManager(entity, request.managerId());

        String tempPassword = null;
        if (request.keycloakPassword() != null && !request.keycloakPassword().isBlank()) {
            entity.setPasswordHash(passwordEncoder.encode(request.keycloakPassword()));
        } else {
            String plainPassword = "Temp#" + UUID.randomUUID().toString().substring(0, 8);
            entity.setPasswordHash(passwordEncoder.encode(plainPassword));
            tempPassword = plainPassword;
        }

        java.util.Set<String> rolesSet = new java.util.HashSet<>();
        List<String> assignedRoles = request.roles();
        if (assignedRoles != null && !assignedRoles.isEmpty()) {
            rolesSet.addAll(assignedRoles);
        } else {
            rolesSet.add(Roles.EMPLOYEE);
            assignedRoles = List.of(Roles.EMPLOYEE);
        }
        entity.setRoles(rolesSet);

        Employee saved = employees.save(entity);
        assignmentHistoryService.recordAssignment(saved, saved.getDateOfJoining());
        managerService.rebuildHierarchy(saved);
        
        auditService.record(AuditEvent.of(AuditAction.CREATE, ENTITY)
                .withEntityId(saved.getId())
                .withDetail("code=" + saved.getEmployeeCode()));
        
        EmployeeResponse baseRes = mapper.toResponse(saved);
        return new EmployeeResponse(
                baseRes.id(),
                baseRes.employeeCode(),
                baseRes.firstName(),
                baseRes.lastName(),
                baseRes.email(),
                baseRes.phoneNumber(),
                baseRes.dateOfBirth(),
                baseRes.dateOfJoining(),
                baseRes.employmentStatus(),
                baseRes.keycloakUserId(),
                baseRes.departmentId(),
                baseRes.departmentName(),
                baseRes.designationId(),
                baseRes.designationTitle(),
                baseRes.locationId(),
                baseRes.locationName(),
                baseRes.legalEntityId(),
                baseRes.legalEntityName(),
                baseRes.managerId(),
                baseRes.managerName(),
                baseRes.createdAt(),
                baseRes.updatedAt(),
                baseRes.version(),
                assignedRoles,
                tempPassword
        );
    }

    @Override
    @Transactional
    public EmployeeResponse update(UUID id, EmployeeUpdateRequest request) {
        Employee entity = loadOrThrow(id);

        Optional<Employee> emailOwner = employees.findByEmailIgnoreCase(request.email());
        if (emailOwner.isPresent() && !emailOwner.get().getId().equals(id)) {
            throw new ConflictException("Another employee already uses email '" + request.email() + "'");
        }

        entity.setVersion(request.version());
        mapper.apply(request, entity);
        attachDepartment(entity, request.departmentId());
        attachDesignation(entity, request.designationId());
        attachLocation(entity, request.locationId());
        attachLegalEntity(entity, request.legalEntityId());
        if (request.managerId() != null && request.managerId().equals(id)) {
            throw new ConflictException("Employee cannot be their own manager");
        }
        attachManager(entity, request.managerId());

        Employee saved = employees.save(entity);
        managerService.rebuildHierarchy(saved);
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
                && caller.subjectUuid().equals(entity.getId());
        if (!isHrOrSuper && !isSelf) {
            throw new ForbiddenAccessException("Cannot update another employee's contact info");
        }

        entity.setVersion(request.version());
        mapper.applyContact(request, entity);
        Employee saved = employees.save(entity);
        auditService.record(AuditEvent.of(AuditAction.UPDATE, ENTITY)
                .withEntityId(saved.getId())
                .withDetail("contact-only"));
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        Employee entity = loadOrThrow(id);
        if (entity.getDeletedAt() != null) {
            return;
        }
        entity.setDeletedAt(Instant.now(clock));
        employees.save(entity);
        auditService.record(AuditEvent.of(AuditAction.SOFT_DELETE, ENTITY)
                .withEntityId(id));
    }

    @Override
    @Transactional
    public void hardDelete(UUID id) {
        Employee entity = loadOrThrow(id);
        employees.delete(entity);
        auditService.record(AuditEvent.of(AuditAction.HARD_DELETE, ENTITY)
                .withEntityId(id)
                .withDetail("permanent"));
    }

    // -- Utilities & Scopes -------------------------------------------------

    private Employee loadOrThrow(UUID id) {
        return employees.findById(id)
                .orElseThrow(() -> NotFoundException.of(ENTITY, id));
    }

    private LocalDate todayUtc() {
        return clock.instant().atOffset(ZoneOffset.UTC).toLocalDate();
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
        return employees.findById(caller.subjectUuid())
                .map(self -> EmployeeSpecifications.idIn(employees.findDescendantIds(self.getId())))
                .orElseGet(EmployeeSpecifications::alwaysFalse);
    }

    private Specification<Employee> selfSpec(CurrentUser caller) {
        return employees.findById(caller.subjectUuid())
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
            case REPORTS -> employees.findById(caller.subjectUuid())
                    .map(self -> employees.findDescendantIds(self.getId()).contains(row.getId()))
                    .orElse(false);
            case SELF -> caller.subjectUuid() != null
                    && caller.subjectUuid().equals(row.getId());
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
                && caller.subjectUuid().equals(row.getId());
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

    private void attachLegalEntity(Employee entity, UUID id) {
        if (id == null) {
            entity.setLegalEntity(null);
            return;
        }
        LegalEntity le = legalEntities.findById(id)
                .orElseThrow(() -> NotFoundException.of("LegalEntity", id));
        entity.setLegalEntity(le);
    }
}
