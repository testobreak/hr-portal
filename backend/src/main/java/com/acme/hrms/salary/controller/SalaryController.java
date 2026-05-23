package com.acme.hrms.salary.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.acme.hrms.common.security.CurrentUser;
import com.acme.hrms.common.security.Roles;
import com.acme.hrms.salary.dto.SalaryCreateRequest;
import com.acme.hrms.salary.dto.SalaryResponse;
import com.acme.hrms.salary.service.SalaryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/salaries")
@Tag(name = "Salaries")
public class SalaryController {

    private final SalaryService service;

    public SalaryController(SalaryService service) {
        this.service = service;
    }

    @GetMapping("/me")
    @Operation(summary = "Read own salary history")
    public List<SalaryResponse> me(@AuthenticationPrincipal Jwt jwt) {
        return service.listForSelf(CurrentUser.from(jwt));
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Read salary history for a specific employee")
    public List<SalaryResponse> byEmployee(@AuthenticationPrincipal Jwt jwt,
                                           @PathVariable UUID employeeId) {
        return service.listForEmployee(CurrentUser.from(jwt), employeeId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('"
            + Roles.SUPER_ADMIN + "','"
            + Roles.HR_ADMIN + "','"
            + Roles.FINANCE_ADMIN + "')")
    @Operation(summary = "Create a salary history record")
    public ResponseEntity<SalaryResponse> create(@AuthenticationPrincipal Jwt jwt,
                                                 @RequestBody @Valid SalaryCreateRequest request) {
        SalaryResponse created = service.create(CurrentUser.from(jwt), request);
        return ResponseEntity
                .created(URI.create("/api/salaries/" + created.id()))
                .body(created);
    }
}
