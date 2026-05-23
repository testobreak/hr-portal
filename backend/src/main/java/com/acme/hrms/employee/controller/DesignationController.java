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
import com.acme.hrms.employee.dto.DesignationCreateRequest;
import com.acme.hrms.employee.dto.DesignationResponse;
import com.acme.hrms.employee.dto.DesignationUpdateRequest;
import com.acme.hrms.employee.service.DesignationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/designations")
@Tag(name = "Designations")
public class DesignationController {

    private final DesignationService service;

    public DesignationController(DesignationService service) {
        this.service = service;
    }

    @GetMapping
    public Page<DesignationResponse> list(@RequestParam(name = "q", required = false) String query,
                                          Pageable pageable) {
        return service.list(query, pageable);
    }

    @GetMapping("/{id}")
    public DesignationResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "')")
    @Operation(summary = "Create a designation")
    public ResponseEntity<DesignationResponse> create(@RequestBody @Valid DesignationCreateRequest request) {
        DesignationResponse created = service.create(request);
        return ResponseEntity
                .created(URI.create("/api/designations/" + created.id()))
                .body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "')")
    @Operation(summary = "Update a designation")
    public DesignationResponse update(@PathVariable UUID id,
                                      @RequestBody @Valid DesignationUpdateRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "')")
    @Operation(summary = "Soft-delete a designation")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
