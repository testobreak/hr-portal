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
import com.acme.hrms.employee.dto.LegalEntityCreateRequest;
import com.acme.hrms.employee.dto.LegalEntityResponse;
import com.acme.hrms.employee.dto.LegalEntityUpdateRequest;
import com.acme.hrms.employee.service.LegalEntityService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/legal-entities")
@Tag(name = "Legal Entities")
public class LegalEntityController {

    private final LegalEntityService service;

    public LegalEntityController(LegalEntityService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List legal entities")
    public Page<LegalEntityResponse> list(@RequestParam(name = "q", required = false) String query,
                                         Pageable pageable) {
        return service.list(query, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Read a legal entity")
    public LegalEntityResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "')")
    @Operation(summary = "Create a legal entity")
    public ResponseEntity<LegalEntityResponse> create(@RequestBody @Valid LegalEntityCreateRequest request) {
        LegalEntityResponse created = service.create(request);
        return ResponseEntity
                .created(URI.create("/api/legal-entities/" + created.id()))
                .body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "')")
    @Operation(summary = "Update a legal entity")
    public LegalEntityResponse update(@PathVariable UUID id,
                                     @RequestBody @Valid LegalEntityUpdateRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "')")
    @Operation(summary = "Soft-delete a legal entity")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
