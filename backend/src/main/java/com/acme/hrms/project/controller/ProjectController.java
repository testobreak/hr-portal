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
import com.acme.hrms.common.web.PageableUtils;
import com.acme.hrms.project.dto.ProjectCreateRequest;
import com.acme.hrms.project.dto.ProjectResponse;
import com.acme.hrms.project.dto.ProjectUpdateRequest;
import com.acme.hrms.project.entity.Project;
import com.acme.hrms.project.service.ProjectService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "Projects")
public class ProjectController {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List projects within the caller's scope")
    public Page<ProjectResponse> list(@AuthenticationPrincipal Jwt jwt,
                                      @RequestParam(name = "q", required = false) String query,
                                      Pageable pageable) {
        return service.list(CurrentUser.from(jwt), query, PageableUtils.sanitize(pageable, Project.class));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Read a project within the caller's scope")
    public ProjectResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return service.get(CurrentUser.from(jwt), id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('"
            + Roles.SUPER_ADMIN + "','"
            + Roles.FINANCE_ADMIN + "','"
            + Roles.PROJECT_MANAGER + "')")
    @Operation(summary = "Create a project")
    public ResponseEntity<ProjectResponse> create(@AuthenticationPrincipal Jwt jwt,
                                                  @RequestBody @Valid ProjectCreateRequest request) {
        ProjectResponse created = service.create(CurrentUser.from(jwt), request);
        return ResponseEntity
                .created(URI.create("/api/projects/" + created.id()))
                .body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('"
            + Roles.SUPER_ADMIN + "','"
            + Roles.FINANCE_ADMIN + "','"
            + Roles.PROJECT_MANAGER + "')")
    @Operation(summary = "Update a project")
    public ProjectResponse update(@AuthenticationPrincipal Jwt jwt,
                                  @PathVariable UUID id,
                                  @RequestBody @Valid ProjectUpdateRequest request) {
        return service.update(CurrentUser.from(jwt), id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('"
            + Roles.SUPER_ADMIN + "','"
            + Roles.FINANCE_ADMIN + "','"
            + Roles.PROJECT_MANAGER + "')")
    @Operation(summary = "Soft-delete a project")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        service.softDelete(CurrentUser.from(jwt), id);
        return ResponseEntity.noContent().build();
    }
}
