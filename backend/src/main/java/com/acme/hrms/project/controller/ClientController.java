package com.acme.hrms.project.controller;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.acme.hrms.common.security.CurrentUser;
import com.acme.hrms.common.security.Roles;
import com.acme.hrms.project.dto.ClientCreateRequest;
import com.acme.hrms.project.dto.ClientResponse;
import com.acme.hrms.project.dto.ClientUpdateRequest;
import com.acme.hrms.project.service.ClientService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/clients")
@Tag(name = "Clients")
public class ClientController {

    private final ClientService service;

    public ClientController(ClientService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('"
            + Roles.SUPER_ADMIN + "','"
            + Roles.HR_ADMIN + "','"
            + Roles.FINANCE_ADMIN + "','"
            + Roles.LEADERSHIP + "','"
            + Roles.MANAGER + "','"
            + Roles.PROJECT_MANAGER + "')")
    @Operation(summary = "List clients within the caller's scope")
    public Page<ClientResponse> list(@AuthenticationPrincipal Jwt jwt,
                                     @RequestParam(name = "q", required = false) String query,
                                     Pageable pageable) {
        return service.list(CurrentUser.from(jwt), query, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('"
            + Roles.SUPER_ADMIN + "','"
            + Roles.HR_ADMIN + "','"
            + Roles.FINANCE_ADMIN + "','"
            + Roles.LEADERSHIP + "','"
            + Roles.MANAGER + "','"
            + Roles.PROJECT_MANAGER + "')")
    @Operation(summary = "Read a client within the caller's scope")
    public ClientResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return service.get(CurrentUser.from(jwt), id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.FINANCE_ADMIN + "')")
    @Operation(summary = "Create a client")
    public ResponseEntity<ClientResponse> create(@RequestBody @Valid ClientCreateRequest request) {
        ClientResponse created = service.create(request);
        return ResponseEntity
                .created(URI.create("/api/clients/" + created.id()))
                .body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.FINANCE_ADMIN + "')")
    @Operation(summary = "Update a client")
    public ClientResponse update(@PathVariable UUID id,
                                 @RequestBody @Valid ClientUpdateRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.FINANCE_ADMIN + "')")
    @Operation(summary = "Soft-delete a client")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
