package com.acme.hrms.salary.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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
import com.acme.hrms.salary.dto.SalaryCreateRequest;
import com.acme.hrms.salary.dto.SalaryResponse;
import com.acme.hrms.salary.entity.SalaryHistory;
import com.acme.hrms.salary.repository.SalaryHistoryRepository;

@Service
public class SalaryServiceImpl implements SalaryService {

    private static final String ENTITY = "salary_history";
    private static final LocalDate INFINITY_DATE = LocalDate.of(9999, 12, 31);

    private final SalaryHistoryRepository salaries;
    private final EmployeeRepository employees;
    private final AuditService auditService;

    public SalaryServiceImpl(SalaryHistoryRepository salaries,
                             EmployeeRepository employees,
                             AuditService auditService) {
        this.salaries = salaries;
        this.employees = employees;
        this.auditService = auditService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalaryResponse> listForSelf(CurrentUser user) {
        Employee self = loadEmployeeBySubject(user.subjectUuid());
        auditService.record(AuditEvent.of(AuditAction.READ_SENSITIVE, ENTITY)
                .withEntityId(self.getId())
                .withDetail("self salary history read"));
        return salaries.findByEmployeeIdOrderByEffectiveFromDescCreatedAtDesc(self.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalaryResponse> listForEmployee(CurrentUser user, UUID employeeId) {
        if (canReadAnySalary(user)) {
            auditService.record(AuditEvent.of(AuditAction.READ_SENSITIVE, ENTITY)
                    .withEntityId(employeeId)
                    .withDetail("salary history read"));
            return salaries.findByEmployeeIdOrderByEffectiveFromDescCreatedAtDesc(employeeId)
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        Employee self = loadEmployeeBySubject(user.subjectUuid());
        if (!self.getId().equals(employeeId)) {
            throw new ForbiddenAccessException("Salary can only be read for the authenticated employee");
        }
        return listForSelf(user);
    }

    @Override
    @Transactional
    public SalaryResponse create(CurrentUser user, SalaryCreateRequest request) {
        if (!canWriteSalary(user)) {
            throw new ForbiddenAccessException("Insufficient privileges to write salary history");
        }
        if (request.effectiveTo() != null && request.effectiveTo().isBefore(request.effectiveFrom())) {
            throw new ConflictException("effectiveTo must be on or after effectiveFrom");
        }
        Employee employee = loadEmployee(request.employeeId());
        boolean overlaps = salaries.existsOverlappingRange(
                employee.getId(),
                request.effectiveFrom(),
                request.effectiveTo(),
                INFINITY_DATE);
        if (overlaps) {
            throw new ConflictException("Salary effective date range overlaps with an existing record");
        }

        SalaryHistory created = SalaryHistory.builder()
                .employee(employee)
                .amount(request.amount())
                .currencyCode(request.currencyCode().trim().toUpperCase())
                .effectiveFrom(request.effectiveFrom())
                .effectiveTo(request.effectiveTo())
                .reason(request.reason())
                .build();
        SalaryHistory saved = salaries.save(created);
        auditService.record(AuditEvent.of(AuditAction.CREATE, ENTITY)
                .withEntityId(saved.getId())
                .withDetail("employeeId=" + employee.getId()));
        return toResponse(saved);
    }

    private boolean canReadAnySalary(CurrentUser user) {
        return user.hasAnyRole(Roles.SUPER_ADMIN, Roles.HR_ADMIN, Roles.FINANCE_ADMIN);
    }

    private boolean canWriteSalary(CurrentUser user) {
        return user.hasAnyRole(Roles.SUPER_ADMIN, Roles.HR_ADMIN, Roles.FINANCE_ADMIN);
    }

    private Employee loadEmployee(UUID id) {
        return employees.findById(id)
                .orElseThrow(() -> NotFoundException.of("Employee", id));
    }

    private Employee loadEmployeeBySubject(UUID subject) {
        if (subject == null) {
            throw new ForbiddenAccessException("Authenticated subject is not a UUID");
        }
        return employees.findById(subject)
                .orElseThrow(() -> new ForbiddenAccessException("No employee is linked to the authenticated subject"));
    }

    private SalaryResponse toResponse(SalaryHistory s) {
        return new SalaryResponse(
                s.getId(),
                s.getEmployee().getId(),
                s.getAmount(),
                s.getCurrencyCode(),
                s.getEffectiveFrom(),
                s.getEffectiveTo(),
                s.getReason(),
                s.getCreatedAt());
    }
}
