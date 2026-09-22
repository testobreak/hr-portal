package com.acme.hrms.manager.controller;

import com.acme.hrms.common.security.CurrentUser;
import com.acme.hrms.employee.dto.EmployeeSummary;
import com.acme.hrms.manager.entity.ManagerDelegation;
import com.acme.hrms.manager.service.ManagerService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.acme.hrms.common.error.NotFoundException;
import com.acme.hrms.employee.repository.EmployeeRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/me")
public class ManagerController {

    private final ManagerService managerService;
    private final EmployeeRepository employeeRepository;

    public ManagerController(ManagerService managerService, EmployeeRepository employeeRepository) {
        this.managerService = managerService;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping("/team")
    public ResponseEntity<List<EmployeeSummary>> getTeam(
            @RequestParam(name = "scope", defaultValue = "direct") String scope) {
        UUID subjectUuid = CurrentUser.fromSecurityContext()
                .map(CurrentUser::subjectUuid)
                .orElse(null);
        if (subjectUuid == null || !employeeRepository.existsById(subjectUuid)) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(managerService.getTeam(subjectUuid, scope));
    }

    @PostMapping("/delegations")
    public ResponseEntity<ManagerDelegation> createDelegation(
            @RequestParam(name = "delegateId") UUID delegateId,
            @RequestParam(name = "startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        UUID subjectUuid = CurrentUser.fromSecurityContext()
                .map(CurrentUser::subjectUuid)
                .orElseThrow(() -> new IllegalStateException("User not found in security context"));
        UUID managerId = subjectUuid;
        if (!employeeRepository.existsById(managerId)) {
            throw NotFoundException.of("Employee", managerId);
        }
        ManagerDelegation delegation = managerService.createDelegation(managerId, delegateId, startDate, endDate);
        return ResponseEntity.status(HttpStatus.CREATED).body(delegation);
    }

    @DeleteMapping("/delegations/{delegationId}")
    public ResponseEntity<Void> revokeDelegation(@PathVariable(name = "delegationId") UUID delegationId) {
        managerService.revokeDelegation(delegationId);
        return ResponseEntity.noContent().build();
    }
}
