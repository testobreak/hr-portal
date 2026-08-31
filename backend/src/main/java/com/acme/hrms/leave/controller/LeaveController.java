package com.acme.hrms.leave.controller;

import com.acme.hrms.common.security.CurrentUser;
import com.acme.hrms.leave.dto.LeaveBalanceResponse;
import com.acme.hrms.leave.dto.LeaveLedgerEntryDto;
import com.acme.hrms.leave.service.LeaveService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.acme.hrms.common.error.NotFoundException;
import com.acme.hrms.employee.entity.Employee;
import com.acme.hrms.employee.repository.EmployeeRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class LeaveController {

    private final LeaveService leaveService;
    private final EmployeeRepository employeeRepository;

    public LeaveController(LeaveService leaveService, EmployeeRepository employeeRepository) {
        this.leaveService = leaveService;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping("/me/leave-balances")
    public ResponseEntity<List<LeaveBalanceResponse>> getMyBalances() {
        UUID subjectUuid = CurrentUser.fromSecurityContext()
                .map(CurrentUser::subjectUuid)
                .orElseThrow(() -> new IllegalStateException("User not found in security context"));
        UUID myId = subjectUuid;
        if (!employeeRepository.existsById(myId)) {
            throw NotFoundException.of("Employee", myId);
        }
        return ResponseEntity.ok(leaveService.getLeaveBalances(myId));
    }

    @GetMapping("/me/leave-balances/{leaveTypeId}/ledger")
    public ResponseEntity<List<LeaveLedgerEntryDto>> getMyLedger(@PathVariable UUID leaveTypeId) {
        UUID subjectUuid = CurrentUser.fromSecurityContext()
                .map(CurrentUser::subjectUuid)
                .orElseThrow(() -> new IllegalStateException("User not found in security context"));
        UUID myId = subjectUuid;
        if (!employeeRepository.existsById(myId)) {
            throw NotFoundException.of("Employee", myId);
        }
        return ResponseEntity.ok(leaveService.getLedger(myId, leaveTypeId));
    }

    @PostMapping("/admin/leave-adjustments")
    public ResponseEntity<Void> adjustLeave(
            @RequestParam(name = "employeeId") UUID employeeId,
            @RequestParam(name = "leaveTypeId") UUID leaveTypeId,
            @RequestParam(name = "quantity") BigDecimal quantity,
            @RequestParam(name = "reason") String reason) {
        leaveService.adjustLeaveBalance(employeeId, leaveTypeId, quantity, reason);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/leave-accrual-runs")
    public ResponseEntity<Void> runAccrual(@RequestParam(name = "period") String period) {
        leaveService.runAccrual(period);
        return ResponseEntity.ok().build();
    }
}
