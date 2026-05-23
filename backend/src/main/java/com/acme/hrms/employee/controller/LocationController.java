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
import com.acme.hrms.employee.dto.LocationCreateRequest;
import com.acme.hrms.employee.dto.LocationResponse;
import com.acme.hrms.employee.dto.LocationUpdateRequest;
import com.acme.hrms.employee.service.LocationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/locations")
@Tag(name = "Locations")
public class LocationController {

    private final LocationService service;

    public LocationController(LocationService service) {
        this.service = service;
    }

    @GetMapping
    public Page<LocationResponse> list(@RequestParam(name = "q", required = false) String query,
                                       Pageable pageable) {
        return service.list(query, pageable);
    }

    @GetMapping("/{id}")
    public LocationResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "')")
    @Operation(summary = "Create a location")
    public ResponseEntity<LocationResponse> create(@RequestBody @Valid LocationCreateRequest request) {
        LocationResponse created = service.create(request);
        return ResponseEntity
                .created(URI.create("/api/locations/" + created.id()))
                .body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "')")
    @Operation(summary = "Update a location")
    public LocationResponse update(@PathVariable UUID id,
                                   @RequestBody @Valid LocationUpdateRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "')")
    @Operation(summary = "Soft-delete a location")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
