package com.acme.hrms.employee.controller;

import java.net.URI;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.acme.hrms.common.security.Roles;
import com.acme.hrms.employee.dto.DepartmentCreateRequest;
import com.acme.hrms.employee.dto.DepartmentResponse;
import com.acme.hrms.employee.dto.DepartmentUpdateRequest;
import com.acme.hrms.employee.service.DepartmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Department CRUD. Read access is open to all authenticated users (it is
 * dropdown data); write access is HR or SUPER per the RBAC matrix §2.
 */
@RestController
@RequestMapping("/api/departments")
@Tag(name = "Departments")
public class DepartmentController {

    private final DepartmentService service;

    public DepartmentController(DepartmentService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List departments")
    public Page<DepartmentResponse> list(@RequestParam(name = "q", required = false) String query,
                                         Pageable pageable) {
        return service.list(query, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Read a department")
    public DepartmentResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "')")
    @Operation(summary = "Create a department")
    public ResponseEntity<DepartmentResponse> create(@RequestBody @Valid DepartmentCreateRequest request) {
        DepartmentResponse created = service.create(request);
        return ResponseEntity
                .created(URI.create("/api/departments/" + created.id()))
                .body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "')")
    @Operation(summary = "Update a department")
    public DepartmentResponse update(@PathVariable UUID id,
                                     @RequestBody @Valid DepartmentUpdateRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "')")
    @Operation(summary = "Soft-delete a department")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
