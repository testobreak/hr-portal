package com.acme.hrms.attendance.controller;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.acme.hrms.attendance.dto.AttendanceResponse;
import com.acme.hrms.attendance.dto.ClockInRequest;
import com.acme.hrms.attendance.dto.ClockOutRequest;
import com.acme.hrms.attendance.dto.TimesheetCreateRequest;
import com.acme.hrms.attendance.dto.TimesheetResponse;
import com.acme.hrms.attendance.service.AttendanceService;
import com.acme.hrms.common.security.CurrentUser;
import com.acme.hrms.common.security.Roles;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/attendance")
@Tag(name = "Attendance Management", description = "Endpoints for employee daily clock-in/out and timesheets")
public class AttendanceController {

    private final AttendanceService service;
    private final com.acme.hrms.attendance.service.TimesheetService timesheetService;

    @org.springframework.beans.factory.annotation.Autowired
    private com.acme.hrms.employee.repository.EmployeeRepository employeeRepository;

    public AttendanceController(AttendanceService service,
                                com.acme.hrms.attendance.service.TimesheetService timesheetService) {
        this.service = service;
        this.timesheetService = timesheetService;
    }

    private UUID getMyId() {
        UUID subjectUuid = CurrentUser.fromSecurityContext()
                .map(CurrentUser::subjectUuid)
                .orElseThrow(() -> new IllegalStateException("User not found in security context"));
        UUID myId = subjectUuid;
        if (!employeeRepository.existsById(myId)) {
            throw com.acme.hrms.common.error.NotFoundException.of("Employee", myId);
        }
        return myId;
    }

    @PostMapping("/me/clock-in")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.EMPLOYEE + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Record clock-in time for current logged-in employee")
    public ResponseEntity<AttendanceResponse> clockInMe(@RequestBody @Valid ClockInRequest request) {
        AttendanceResponse response = service.clockIn(getMyId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/me/clock-out")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.EMPLOYEE + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Record clock-out time for current logged-in employee")
    public AttendanceResponse clockOutMe(@RequestBody @Valid ClockOutRequest request) {
        return service.clockOut(getMyId(), request);
    }

    @GetMapping("/me/latest")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.EMPLOYEE + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Get the active clock-in session details for current logged-in employee")
    public AttendanceResponse getLatestSessionMe() {
        UUID subjectUuid = CurrentUser.fromSecurityContext()
                .map(CurrentUser::subjectUuid)
                .orElse(null);
        if (subjectUuid == null || !employeeRepository.existsById(subjectUuid)) {
            return null;
        }
        return service.getLatestLog(subjectUuid);
    }

    @GetMapping("/me/logs")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.EMPLOYEE + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Get daily clock logs for current logged-in employee within a period")
    public List<AttendanceResponse> getLogsMe(@RequestParam String startIso,
                                              @RequestParam String endIso) {
        UUID subjectUuid = CurrentUser.fromSecurityContext()
                .map(CurrentUser::subjectUuid)
                .orElse(null);
        if (subjectUuid == null || !employeeRepository.existsById(subjectUuid)) {
            return List.of();
        }
        return service.getLogsForPeriod(subjectUuid, Instant.parse(startIso), Instant.parse(endIso));
    }

    @PostMapping("/me/timesheets")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.EMPLOYEE + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Create or update timesheet draft/lines for current logged-in employee")
    public TimesheetResponse saveTimesheetMe(@RequestBody @Valid TimesheetCreateRequest request) {
        return timesheetService.createOrUpdateTimesheet(getMyId(), request);
    }

    @GetMapping("/me/timesheets")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.EMPLOYEE + "','" + Roles.MANAGER + "')")
    @Operation(summary = "List timesheets for current logged-in employee")
    public List<TimesheetResponse> getEmployeeTimesheetsMe() {
        UUID subjectUuid = CurrentUser.fromSecurityContext()
                .map(CurrentUser::subjectUuid)
                .orElse(null);
        if (subjectUuid == null || !employeeRepository.existsById(subjectUuid)) {
            return List.of();
        }
        return timesheetService.listEmployeeTimesheets(subjectUuid);
    }

    @PostMapping("/employees/{employeeId}/clock-in")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.EMPLOYEE + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Record clock-in time for an employee")
    public ResponseEntity<AttendanceResponse> clockIn(@PathVariable UUID employeeId,
                                                      @RequestBody @Valid ClockInRequest request) {
        AttendanceResponse response = service.clockIn(employeeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/employees/{employeeId}/clock-out")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.EMPLOYEE + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Record clock-out time for an employee")
    public AttendanceResponse clockOut(@PathVariable UUID employeeId,
                                       @RequestBody @Valid ClockOutRequest request) {
        return service.clockOut(employeeId, request);
    }

    @GetMapping("/employees/{employeeId}/latest")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.EMPLOYEE + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Get the active clock-in session details for an employee")
    public AttendanceResponse getLatestSession(@PathVariable UUID employeeId) {
        return service.getLatestLog(employeeId);
    }

    @GetMapping("/employees/{employeeId}/logs")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.EMPLOYEE + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Get daily clock logs for an employee within a period")
    public List<AttendanceResponse> getLogs(@PathVariable UUID employeeId,
                                            @RequestParam String startIso,
                                            @RequestParam String endIso) {
        return service.getLogsForPeriod(employeeId, Instant.parse(startIso), Instant.parse(endIso));
    }


    @GetMapping("/logs")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Get all tenant clock logs within a period")
    public List<AttendanceResponse> getTenantLogs(@RequestParam String startIso,
                                                  @RequestParam String endIso) {
        return service.getTenantLogsForPeriod(Instant.parse(startIso), Instant.parse(endIso));
    }
}
