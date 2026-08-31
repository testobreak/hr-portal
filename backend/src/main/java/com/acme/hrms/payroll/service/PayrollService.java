package com.acme.hrms.payroll.service;

import java.util.List;
import java.util.UUID;

import com.acme.hrms.payroll.dto.PayrollRunCreateRequest;
import com.acme.hrms.payroll.dto.PayrollRunResponse;
import com.acme.hrms.payroll.dto.PayslipResponse;

public interface PayrollService {
    PayrollRunResponse createPayrollRun(PayrollRunCreateRequest request);
    PayrollRunResponse calculatePayroll(UUID runId);
    PayrollRunResponse approvePayrollRun(UUID runId);
    PayrollRunResponse markPaidPayrollRun(UUID runId);
    List<PayrollRunResponse> listPayrollRuns();
    List<PayslipResponse> listPayslipsForRun(UUID runId);
    PayslipResponse getPayslipForEmployee(UUID runId, UUID employeeId);
    List<PayslipResponse> listEmployeePayslips(UUID employeeId);
    String exportLedgerCsv(UUID runId);
    String exportBankDisbursementCsv(UUID runId);
}
