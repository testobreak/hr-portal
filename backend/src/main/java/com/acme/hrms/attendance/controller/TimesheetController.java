package com.acme.hrms.attendance.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.acme.hrms.attendance.dto.TimesheetApproveRequest;
import com.acme.hrms.attendance.dto.TimesheetCreateRequest;
import com.acme.hrms.attendance.dto.TimesheetResponse;
import com.acme.hrms.attendance.service.TimesheetService;
import com.acme.hrms.common.security.CurrentUser;
import com.acme.hrms.common.security.Roles;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/attendance")
@Tag(name = "Timesheet Management", description = "Endpoints for employee timesheet creation, submission, and manager approvals")
public class TimesheetController {

    private final TimesheetService service;

    public TimesheetController(TimesheetService service) {
        this.service = service;
    }

    @PostMapping("/employees/{employeeId}/timesheets")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.EMPLOYEE + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Create or update timesheet draft/lines")
    public TimesheetResponse saveTimesheet(@PathVariable UUID employeeId,
                                           @RequestBody @Valid TimesheetCreateRequest request) {
        return service.createOrUpdateTimesheet(employeeId, request);
    }

    @PostMapping("/timesheets/{timesheetId}/submit")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.EMPLOYEE + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Submit a timesheet for approval")
    public TimesheetResponse submitTimesheet(@PathVariable UUID timesheetId) {
        return service.submitTimesheet(timesheetId);
    }

    @PostMapping("/timesheets/{timesheetId}/approve")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Approve a timesheet")
    public TimesheetResponse approveTimesheet(@PathVariable UUID timesheetId,
                                              @RequestBody @Valid TimesheetApproveRequest request) {
        UUID approverId = CurrentUser.fromSecurityContext()
                .map(CurrentUser::subjectUuid)
                .orElse(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        return service.approveTimesheet(timesheetId, approverId, request);
    }

    @PostMapping("/timesheets/{timesheetId}/reject")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Reject a timesheet")
    public TimesheetResponse rejectTimesheet(@PathVariable UUID timesheetId,
                                             @RequestBody @Valid TimesheetApproveRequest request) {
        UUID approverId = CurrentUser.fromSecurityContext()
                .map(CurrentUser::subjectUuid)
                .orElse(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        return service.rejectTimesheet(timesheetId, approverId, request);
    }

    @GetMapping("/employees/{employeeId}/timesheets")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.EMPLOYEE + "','" + Roles.MANAGER + "')")
    @Operation(summary = "List timesheets for a specific employee")
    public List<TimesheetResponse> getEmployeeTimesheets(@PathVariable UUID employeeId) {
        return service.listEmployeeTimesheets(employeeId);
    }

    @GetMapping("/timesheets/pending")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "List timesheets pending approval")
    public List<TimesheetResponse> getPendingApprovals() {
        return service.listPendingApprovals();
    }

    @GetMapping("/timesheets/{timesheetId}")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.EMPLOYEE + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Get details of a specific timesheet")
    public TimesheetResponse getTimesheet(@PathVariable UUID timesheetId) {
        return service.getTimesheet(timesheetId);
    }
}
