package com.acme.hrms.dashboard.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.acme.hrms.common.security.CurrentUser;
import com.acme.hrms.common.security.Roles;
import com.acme.hrms.dashboard.dto.BenchMetricsResponse;
import com.acme.hrms.dashboard.dto.HrOverviewResponse;
import com.acme.hrms.dashboard.service.DashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/hr-overview")
    @PreAuthorize("hasAnyRole('"
            + Roles.SUPER_ADMIN + "','"
            + Roles.HR_ADMIN + "','"
            + Roles.LEADERSHIP + "','"
            + Roles.MANAGER + "')")
    @Operation(summary = "Headcount, joiners, and leavers (scoped per RBAC matrix §10)")
    public HrOverviewResponse hrOverview(@AuthenticationPrincipal Jwt jwt) {
        return dashboardService.hrOverview(CurrentUser.from(jwt));
    }

    @GetMapping("/bench")
    @PreAuthorize("hasAnyRole('"
            + Roles.SUPER_ADMIN + "','"
            + Roles.FINANCE_ADMIN + "','"
            + Roles.LEADERSHIP + "','"
            + Roles.PROJECT_MANAGER + "')")
    @Operation(summary = "Bench / utilization (org-wide or PM portfolio per matrix §10)")
    public BenchMetricsResponse bench(@AuthenticationPrincipal Jwt jwt) {
        return dashboardService.bench(CurrentUser.from(jwt));
    }
}
