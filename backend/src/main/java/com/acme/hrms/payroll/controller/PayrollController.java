package com.acme.hrms.payroll.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.acme.hrms.common.security.Roles;
import com.acme.hrms.payroll.dto.PayrollRunCreateRequest;
import com.acme.hrms.payroll.dto.PayrollRunResponse;
import com.acme.hrms.payroll.dto.PayslipResponse;
import com.acme.hrms.payroll.service.PayrollService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/payroll")
@Tag(name = "Payroll Management", description = "Endpoints for monthly payroll calculations, payslip lookups, and direct deposit ledger exports")
public class PayrollController {

    private final PayrollService service;

    @org.springframework.beans.factory.annotation.Autowired
    private com.acme.hrms.employee.repository.EmployeeRepository employeeRepository;

    public PayrollController(PayrollService service) {
        this.service = service;
    }

    private UUID getMyId() {
        UUID subjectUuid = com.acme.hrms.common.security.CurrentUser.fromSecurityContext()
                .map(com.acme.hrms.common.security.CurrentUser::subjectUuid)
                .orElseThrow(() -> new IllegalStateException("User not found in security context"));
        UUID myId = subjectUuid;
        if (!employeeRepository.existsById(myId)) {
            throw com.acme.hrms.common.error.NotFoundException.of("Employee", myId);
        }
        return myId;
    }

    @PostMapping("/runs")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "')")
    @Operation(summary = "Initialize a new draft payroll calculation period")
    public ResponseEntity<PayrollRunResponse> createRun(@RequestBody @Valid PayrollRunCreateRequest request) {
        PayrollRunResponse response = service.createPayrollRun(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/runs/{runId}/calculate")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "')")
    @Operation(summary = "Trigger gross-to-net computation for all active tenant employees")
    public PayrollRunResponse calculatePayroll(@PathVariable UUID runId) {
        return service.calculatePayroll(runId);
    }

    @PostMapping("/runs/{runId}/approve")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "')")
    @Operation(summary = "Approve computed payroll run (locks values)")
    public PayrollRunResponse approvePayroll(@PathVariable UUID runId) {
        return service.approvePayrollRun(runId);
    }

    @PostMapping("/runs/{runId}/pay")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "')")
    @Operation(summary = "Mark payroll run as PAID and finalize ledger records")
    public PayrollRunResponse payPayroll(@PathVariable UUID runId) {
        return service.markPaidPayrollRun(runId);
    }

    @GetMapping("/runs")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "List all payroll periods for tenant")
    public List<PayrollRunResponse> listRuns() {
        return service.listPayrollRuns();
    }

    @GetMapping("/runs/{runId}/payslips")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.MANAGER + "')")
    @Operation(summary = "List all individual payslips generated inside a payroll run")
    public List<PayslipResponse> listPayslips(@PathVariable UUID runId) {
        return service.listPayslipsForRun(runId);
    }

    @GetMapping("/runs/{runId}/payslips/employees/{employeeId}")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.EMPLOYEE + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Get specific employee payslip details inside a payroll run")
    public PayslipResponse getEmployeePayslip(@PathVariable UUID runId, @PathVariable UUID employeeId) {
        return service.getPayslipForEmployee(runId, employeeId);
    }

    @GetMapping("/employees/{employeeId}/payslips")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.EMPLOYEE + "','" + Roles.MANAGER + "')")
    @Operation(summary = "List historical payslips for an employee")
    public List<PayslipResponse> getEmployeePayslipsHistory(@PathVariable UUID employeeId) {
        return service.listEmployeePayslips(employeeId);
    }

    @GetMapping("/me/payslips")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.EMPLOYEE + "','" + Roles.MANAGER + "')")
    @Operation(summary = "List historical payslips for the current logged-in employee")
    public List<PayslipResponse> getMyPayslipsHistory() {
        return service.listEmployeePayslips(getMyId());
    }

    @GetMapping("/runs/{runId}/payslips/me")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "','" + Roles.EMPLOYEE + "','" + Roles.MANAGER + "')")
    @Operation(summary = "Get current employee's payslip details inside a payroll run")
    public PayslipResponse getMyPayslip(@PathVariable UUID runId) {
        return service.getPayslipForEmployee(runId, getMyId());
    }

    @GetMapping("/runs/{runId}/export/ledger")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "')")
    @Operation(summary = "Export general ledger postings CSV file for approved payroll run")
    public ResponseEntity<byte[]> exportLedger(@PathVariable UUID runId) {
        String csv = service.exportLedgerCsv(runId);
        byte[] data = csv.getBytes();
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ledger_payroll_" + runId + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
    }

    @GetMapping("/runs/{runId}/export/bank")
    @PreAuthorize("hasAnyRole('" + Roles.SUPER_ADMIN + "','" + Roles.HR_ADMIN + "')")
    @Operation(summary = "Export bank direct deposit disbursement CSV file for approved payroll run")
    public ResponseEntity<byte[]> exportBankDisbursement(@PathVariable UUID runId) {
        String csv = service.exportBankDisbursementCsv(runId);
        byte[] data = csv.getBytes();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bank_disbursements_" + runId + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
    }
}
