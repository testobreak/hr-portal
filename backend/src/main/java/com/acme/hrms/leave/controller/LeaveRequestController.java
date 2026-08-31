package com.acme.hrms.leave.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.acme.hrms.common.error.NotFoundException;
import com.acme.hrms.common.security.CurrentUser;
import com.acme.hrms.employee.entity.Employee;
import com.acme.hrms.employee.repository.EmployeeRepository;
import com.acme.hrms.leave.dto.LeaveRequestResponseDto;
import com.acme.hrms.leave.service.LeaveRequestService;

@RestController
@RequestMapping("/api/v1/me")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;
    private final EmployeeRepository employeeRepository;

    public LeaveRequestController(LeaveRequestService leaveRequestService,
                                  EmployeeRepository employeeRepository) {
        this.leaveRequestService = leaveRequestService;
        this.employeeRepository = employeeRepository;
    }

    @PostMapping("/leave-requests")
    public ResponseEntity<LeaveRequestResponseDto> create(
            @RequestParam(name = "leaveTypeId") UUID leaveTypeId,
            @RequestParam(name = "startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "reason", required = false) String reason) {
        UUID subjectUuid = CurrentUser.fromSecurityContext()
                .map(CurrentUser::subjectUuid)
                .orElseThrow(() -> new IllegalStateException("User not found in security context"));
        UUID myId = subjectUuid;
        if (!employeeRepository.existsById(myId)) {
            throw NotFoundException.of("Employee", myId);
        }
        LeaveRequestResponseDto response = leaveRequestService.createRequest(myId, leaveTypeId, startDate, endDate, reason);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/leave-requests")
    public ResponseEntity<List<LeaveRequestResponseDto>> getMyRequests() {
        UUID subjectUuid = CurrentUser.fromSecurityContext()
                .map(CurrentUser::subjectUuid)
                .orElseThrow(() -> new IllegalStateException("User not found in security context"));
        UUID myId = subjectUuid;
        if (!employeeRepository.existsById(myId)) {
            throw NotFoundException.of("Employee", myId);
        }
        return ResponseEntity.ok(leaveRequestService.getMyRequests(myId));
    }

    @PostMapping("/leave-requests/{requestId}/submit")
    public ResponseEntity<Void> submit(@PathVariable UUID requestId) {
        leaveRequestService.submitRequest(requestId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/leave-requests/{requestId}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable UUID requestId) {
        leaveRequestService.cancelRequest(requestId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/team/leave-calendar")
    public ResponseEntity<List<LeaveRequestResponseDto>> getTeamCalendar(
            @RequestParam(name = "startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        UUID subjectUuid = CurrentUser.fromSecurityContext()
                .map(CurrentUser::subjectUuid)
                .orElseThrow(() -> new IllegalStateException("User not found in security context"));
        UUID managerId = subjectUuid;
        if (!employeeRepository.existsById(managerId)) {
            throw NotFoundException.of("Employee", managerId);
        }
        return ResponseEntity.ok(leaveRequestService.getTeamCalendar(managerId, startDate, endDate));
    }
}
