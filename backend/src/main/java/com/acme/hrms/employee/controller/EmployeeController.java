package com.acme.hrms.employee.controller;

import java.net.URI;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.acme.hrms.common.security.CurrentUser;
import com.acme.hrms.common.security.Roles;
import com.acme.hrms.employee.dto.EmployeeContactUpdateRequest;
import com.acme.hrms.employee.dto.EmployeeCreateRequest;
import com.acme.hrms.employee.dto.EmployeeResponse;
import com.acme.hrms.employee.dto.EmployeeSummary;
import com.acme.hrms.employee.dto.EmployeeUpdateRequest;
import com.acme.hrms.employee.service.EmployeeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Employee endpoints. Endpoint-level role gates ({@code @PreAuthorize}) are
 * the first line; row-level visibility and field redaction live in
 * {@code EmployeeServiceImpl}. See RBAC matrix §1.
 */
@RestController
@RequestMapping("/api/employees")
@Tag(name = "Employees")
public class EmployeeController {

    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List employees within the caller's scope")
    public Page<EmployeeSummary> list(@AuthenticationPrincipal Jwt jwt,
                                      @RequestParam(name = "q", required = false) String query,
                                      Pageable pageable) {
        return service.list(CurrentUser.from(jwt), query, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Read a single employee (within scope, with field redaction)")
    public EmployeeResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return service.get(CurrentUser.from(jwt), id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "')")
    @Operation(summary = "Create an employee (HR / SUPER only)")
    public ResponseEntity<EmployeeResponse> create(@RequestBody @Valid EmployeeCreateRequest request) {
        EmployeeResponse created = service.create(request);
        return ResponseEntity
                .created(URI.create("/api/employees/" + created.id()))
                .body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "')")
    @Operation(summary = "Update an employee's HR fields (HR / SUPER only)")
    public EmployeeResponse update(@PathVariable UUID id,
                                   @RequestBody @Valid EmployeeUpdateRequest request) {
        return service.update(id, request);
    }

    /**
     * Self-service contact info update. EMPLOYEE may update their own;
     * HR/SUPER may update anyone's. The service layer enforces the
     * "self-only" rule for EMPLOYEE callers — endpoint-level
     * {@code @PreAuthorize} only screens the role membership.
     */
    @PatchMapping("/{id}/contact")
    @PreAuthorize("hasAnyRole('"
            + Roles.SUPER_ADMIN + "','"
            + Roles.HR_ADMIN + "','"
            + Roles.EMPLOYEE + "')")
    @Operation(summary = "Update contact info (self for EMPLOYEE, anyone for HR/SUPER)")
    public EmployeeResponse updateContact(@AuthenticationPrincipal Jwt jwt,
                                          @PathVariable UUID id,
                                          @RequestBody @Valid EmployeeContactUpdateRequest request) {
        return service.updateContact(CurrentUser.from(jwt), id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "')")
    @Operation(summary = "Soft-delete an employee record")
    public ResponseEntity<Void> softDelete(@PathVariable UUID id) {
        service.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/permanent")
    @PreAuthorize("hasRole('" + Roles.SUPER_ADMIN + "')")
    @Operation(summary = "Hard-delete an employee record (SUPER_ADMIN only). "
            + "Reserved for legal / compliance erasure; audited as HARD_DELETE.")
    public ResponseEntity<Void> hardDelete(@PathVariable UUID id) {
        service.hardDelete(id);
        return ResponseEntity.noContent().build();
    }
}
