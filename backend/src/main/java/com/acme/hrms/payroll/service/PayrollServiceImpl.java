package com.acme.hrms.payroll.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acme.hrms.attendance.entity.AttendanceRecord;
import com.acme.hrms.attendance.repository.AttendanceRecordRepository;
import com.acme.hrms.common.error.ConflictException;
import com.acme.hrms.common.error.NotFoundException;
import com.acme.hrms.common.tenant.TenantContext;
import com.acme.hrms.employee.entity.Employee;
import com.acme.hrms.employee.entity.EmploymentStatus;
import com.acme.hrms.employee.repository.EmployeeRepository;
import com.acme.hrms.leave.entity.LeaveRequest;
import com.acme.hrms.leave.repository.LeaveRequestRepository;
import com.acme.hrms.payroll.dto.PayrollRunCreateRequest;
import com.acme.hrms.payroll.dto.PayrollRunResponse;
import com.acme.hrms.payroll.dto.PayslipItemResponse;
import com.acme.hrms.payroll.dto.PayslipResponse;
import com.acme.hrms.payroll.entity.PayrollRun;
import com.acme.hrms.payroll.entity.Payslip;
import com.acme.hrms.payroll.entity.PayslipItem;
import com.acme.hrms.payroll.repository.PayrollRunRepository;
import com.acme.hrms.payroll.repository.PayslipItemRepository;
import com.acme.hrms.payroll.repository.PayslipRepository;
import com.acme.hrms.salary.entity.SalaryHistory;
import com.acme.hrms.salary.repository.SalaryHistoryRepository;

@Service
public class PayrollServiceImpl implements PayrollService {

    private static final Logger log = LoggerFactory.getLogger(PayrollServiceImpl.class);

    private final PayrollRunRepository payrollRunRepository;
    private final PayslipRepository payslipRepository;
    private final PayslipItemRepository payslipItemRepository;
    private final EmployeeRepository employeeRepository;
    private final SalaryHistoryRepository salaryHistoryRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final LeaveRequestRepository leaveRequestRepository;

    public PayrollServiceImpl(PayrollRunRepository payrollRunRepository,
                              PayslipRepository payslipRepository,
                              PayslipItemRepository payslipItemRepository,
                              EmployeeRepository employeeRepository,
                              SalaryHistoryRepository salaryHistoryRepository,
                              AttendanceRecordRepository attendanceRecordRepository,
                              LeaveRequestRepository leaveRequestRepository) {
        this.payrollRunRepository = payrollRunRepository;
        this.payslipRepository = payslipRepository;
        this.payslipItemRepository = payslipItemRepository;
        this.employeeRepository = employeeRepository;
        this.salaryHistoryRepository = salaryHistoryRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.leaveRequestRepository = leaveRequestRepository;
    }

    @Override
    @Transactional
    public PayrollRunResponse createPayrollRun(PayrollRunCreateRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        
        PayrollRun run = PayrollRun.builder()
                .periodStart(request.periodStart())
                .periodEnd(request.periodEnd())
                .payoutDate(request.payoutDate())
                .runType(request.runType())
                .status("DRAFT")
                .totalGross(BigDecimal.ZERO)
                .totalDeductions(BigDecimal.ZERO)
                .totalNet(BigDecimal.ZERO)
                .build();
        run.setTenantId(tenantId);

        PayrollRun saved = payrollRunRepository.save(run);
        return toRunResponse(saved);
    }

    @Override
    @Transactional
    public PayrollRunResponse calculatePayroll(UUID runId) {
        PayrollRun run = payrollRunRepository.findById(runId)
                .orElseThrow(() -> NotFoundException.of("PayrollRun", runId));

        if (!"DRAFT".equals(run.getStatus()) && !"COMPUTING".equals(run.getStatus())) {
            throw new ConflictException("Payroll calculation can only run on DRAFT or COMPUTING payroll periods.");
        }

        run.setStatus("COMPUTING");
        payrollRunRepository.saveAndFlush(run);

        // Clear existing payslips
        payslipRepository.findByPayrollRunId(runId).forEach(payslipRepository::delete);

        UUID tenantId = TenantContext.getTenantId();
        List<Employee> employees = employeeRepository.findAll().stream()
                .filter(e -> tenantId.equals(e.getTenantId()))
                .filter(e -> e.getEmploymentStatus() == EmploymentStatus.ACTIVE || e.getEmploymentStatus() == EmploymentStatus.ON_LEAVE)
                .collect(Collectors.toList());

        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal totalDeductions = BigDecimal.ZERO;
        BigDecimal totalNet = BigDecimal.ZERO;

        int totalPeriodWorkdays = countWorkdays(run.getPeriodStart(), run.getPeriodEnd());

        for (Employee emp : employees) {
            // 1. Fetch Basic Salary rate
            List<SalaryHistory> salaries = salaryHistoryRepository.findByEmployeeIdOrderByEffectiveFromDescCreatedAtDesc(emp.getId());
            BigDecimal basic = BigDecimal.valueOf(5000.00); // Default fallback rate
            for (SalaryHistory s : salaries) {
                if (!s.getEffectiveFrom().isAfter(run.getPeriodEnd()) && 
                        (s.getEffectiveTo() == null || !s.getEffectiveTo().isBefore(run.getPeriodStart()))) {
                    basic = s.getAmount();
                    break;
                }
            }

            // 2. Count approved leave days in period
            List<LeaveRequest> leaves = leaveRequestRepository.findByEmployeeId(emp.getId()).stream()
                    .filter(lr -> "APPROVED".equals(lr.getStatus()))
                    .collect(Collectors.toList());
            int leaveDays = 0;
            for (LeaveRequest lr : leaves) {
                LocalDate start = lr.getStartDate().isBefore(run.getPeriodStart()) ? run.getPeriodStart() : lr.getStartDate();
                LocalDate end = lr.getEndDate().isAfter(run.getPeriodEnd()) ? run.getPeriodEnd() : lr.getEndDate();
                if (!start.isAfter(end)) {
                    leaveDays += countWorkdays(start, end);
                }
            }

            // 3. Count absent days (daily logs marked as ABSENT)
            var startInstant = run.getPeriodStart().atStartOfDay(ZoneId.systemDefault()).toInstant();
            var endInstant = run.getPeriodEnd().atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
            List<AttendanceRecord> dailyRecords = attendanceRecordRepository.findLogsForPeriod(emp.getId(), startInstant, endInstant);
            int absentDays = (int) dailyRecords.stream()
                    .filter(r -> "ABSENT".equals(r.getStatus()))
                    .count();

            int presentDays = Math.max(0, totalPeriodWorkdays - leaveDays - absentDays);

            // 4. Calculate Allowances
            BigDecimal hra = basic.multiply(BigDecimal.valueOf(0.40)).setScale(2, RoundingMode.HALF_UP); // 40% HRA
            BigDecimal conveyance = BigDecimal.valueOf(1600.00); // Flat Conveyance
            BigDecimal gross = basic.add(hra).add(conveyance);

            // 5. Calculate Deductions
            BigDecimal pf = basic.multiply(BigDecimal.valueOf(0.12)).setScale(2, RoundingMode.HALF_UP); // 12% PF
            BigDecimal profTax = BigDecimal.valueOf(200.00); // flat prof tax
            BigDecimal unpaidLeavesDeduction = BigDecimal.ZERO;
            if (absentDays > 0) {
                unpaidLeavesDeduction = basic.divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(absentDays));
            }
            BigDecimal incomeTax = basic.multiply(BigDecimal.valueOf(0.10)).setScale(2, RoundingMode.HALF_UP); // 10% Flat income tax

            BigDecimal deductions = pf.add(profTax).add(unpaidLeavesDeduction).add(incomeTax);
            BigDecimal net = gross.subtract(deductions);
            if (net.compareTo(BigDecimal.ZERO) < 0) {
                net = BigDecimal.ZERO;
            }

            // 6. Save Payslip
            Payslip payslip = Payslip.builder()
                    .payrollRun(run)
                    .employee(emp)
                    .basicSalary(basic)
                    .allowances(hra.add(conveyance))
                    .deductions(pf.add(profTax).add(unpaidLeavesDeduction))
                    .taxDeductions(incomeTax)
                    .netSalary(net)
                    .workingDays(totalPeriodWorkdays)
                    .presentDays(presentDays)
                    .leaveDays(leaveDays)
                    .currencyCode("USD")
                    .status("DRAFT")
                    .build();
            payslip.setTenantId(tenantId);
            Payslip savedPayslip = payslipRepository.save(payslip);

            // 7. Save Payslip Items
            List<PayslipItem> items = new ArrayList<>();
            items.add(newPayslipItem(savedPayslip, "Basic Salary", "ALLOWANCE", basic));
            items.add(newPayslipItem(savedPayslip, "House Rent Allowance (HRA)", "ALLOWANCE", hra));
            items.add(newPayslipItem(savedPayslip, "Conveyance Allowance", "ALLOWANCE", conveyance));
            items.add(newPayslipItem(savedPayslip, "Provident Fund (PF)", "DEDUCTION", pf));
            items.add(newPayslipItem(savedPayslip, "Professional Tax", "DEDUCTION", profTax));
            items.add(newPayslipItem(savedPayslip, "Income Tax", "TAX", incomeTax));
            if (absentDays > 0) {
                items.add(newPayslipItem(savedPayslip, "Loss of Pay (" + absentDays + " Absent Days)", "DEDUCTION", unpaidLeavesDeduction));
            }
            payslipItemRepository.saveAll(items);

            totalGross = totalGross.add(gross);
            totalDeductions = totalDeductions.add(deductions);
            totalNet = totalNet.add(net);
        }

        run.setStatus("COMPLETED");
        run.setTotalGross(totalGross);
        run.setTotalDeductions(totalDeductions);
        run.setTotalNet(totalNet);
        PayrollRun computedRun = payrollRunRepository.save(run);

        return toRunResponse(computedRun);
    }

    @Override
    @Transactional
    public PayrollRunResponse approvePayrollRun(UUID runId) {
        PayrollRun run = payrollRunRepository.findById(runId)
                .orElseThrow(() -> NotFoundException.of("PayrollRun", runId));

        if (!"COMPLETED".equals(run.getStatus())) {
            throw new ConflictException("Payroll run must be completed/calculated before approving.");
        }

        run.setStatus("APPROVED");
        
        List<Payslip> payslips = payslipRepository.findByPayrollRunId(runId);
        payslips.forEach(p -> p.setStatus("APPROVED"));
        payslipRepository.saveAll(payslips);

        PayrollRun saved = payrollRunRepository.save(run);
        return toRunResponse(saved);
    }

    @Override
    @Transactional
    public PayrollRunResponse markPaidPayrollRun(UUID runId) {
        PayrollRun run = payrollRunRepository.findById(runId)
                .orElseThrow(() -> NotFoundException.of("PayrollRun", runId));

        if (!"APPROVED".equals(run.getStatus())) {
            throw new ConflictException("Payroll run must be approved before marking as paid.");
        }

        run.setStatus("PAID");
        
        List<Payslip> payslips = payslipRepository.findByPayrollRunId(runId);
        payslips.forEach(p -> p.setStatus("PAID"));
        payslipRepository.saveAll(payslips);

        PayrollRun saved = payrollRunRepository.save(run);
        return toRunResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollRunResponse> listPayrollRuns() {
        UUID tenantId = TenantContext.getTenantId();
        return payrollRunRepository.findByTenantIdOrderByPeriodStartDesc(tenantId).stream()
                .map(this::toRunResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayslipResponse> listPayslipsForRun(UUID runId) {
        return payslipRepository.findByPayrollRunId(runId).stream()
                .map(this::toPayslipResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PayslipResponse getPayslipForEmployee(UUID runId, UUID employeeId) {
        Payslip payslip = payslipRepository.findByPayrollRunIdAndEmployeeId(runId, employeeId)
                .orElseThrow(() -> NotFoundException.of("Payslip for employee", employeeId));
        return toPayslipResponse(payslip);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayslipResponse> listEmployeePayslips(UUID employeeId) {
        return payslipRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId).stream()
                .map(this::toPayslipResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public String exportLedgerCsv(UUID runId) {
        PayrollRun run = payrollRunRepository.findById(runId)
                .orElseThrow(() -> NotFoundException.of("PayrollRun", runId));
        
        List<Payslip> payslips = payslipRepository.findByPayrollRunId(runId);
        
        StringBuilder sb = new StringBuilder();
        sb.append("EmployeeCode,EmployeeName,Department,BasicSalary,Allowances,Deductions,Taxes,NetSalary\n");
        for (Payslip p : payslips) {
            String code = p.getEmployee().getEmployeeCode() != null ? p.getEmployee().getEmployeeCode() : "";
            String name = p.getEmployee().getFirstName() + " " + p.getEmployee().getLastName();
            String dept = p.getEmployee().getDepartment() != null ? p.getEmployee().getDepartment().getName() : "";
            sb.append(code).append(",")
              .append("\"").append(name.replace("\"", "\"\"")).append("\",")
              .append("\"").append(dept.replace("\"", "\"\"")).append("\",")
              .append(p.getBasicSalary()).append(",")
              .append(p.getAllowances()).append(",")
              .append(p.getDeductions()).append(",")
              .append(p.getTaxDeductions()).append(",")
              .append(p.getNetSalary()).append("\n");
        }
        return sb.toString();
    }

    @Override
    @Transactional(readOnly = true)
    public String exportBankDisbursementCsv(UUID runId) {
        PayrollRun run = payrollRunRepository.findById(runId)
                .orElseThrow(() -> NotFoundException.of("PayrollRun", runId));

        List<Payslip> payslips = payslipRepository.findByPayrollRunId(runId);

        StringBuilder sb = new StringBuilder();
        sb.append("AccountNumber,EmployeeName,BankDisbursementAmount,Currency,PayoutDate\n");
        String dateStr = run.getPayoutDate() != null ? run.getPayoutDate().toString() : LocalDate.now().toString();
        for (Payslip p : payslips) {
            String name = p.getEmployee().getFirstName() + " " + p.getEmployee().getLastName();
            // Stub/mock account number or read from employee metadata if exists
            String acctNum = "ACC-MOCK-" + p.getEmployee().getId().toString().substring(0, 8).toUpperCase();
            sb.append(acctNum).append(",")
              .append("\"").append(name.replace("\"", "\"\"")).append("\",")
              .append(p.getNetSalary()).append(",")
              .append(p.getCurrencyCode()).append(",")
              .append(dateStr).append("\n");
        }
        return sb.toString();
    }

    private int countWorkdays(LocalDate start, LocalDate end) {
        int count = 0;
        LocalDate cur = start;
        while (!cur.isAfter(end)) {
            DayOfWeek day = cur.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                count++;
            }
            cur = cur.plusDays(1);
        }
        return count;
    }

    private PayslipItem newPayslipItem(Payslip payslip, String name, String type, BigDecimal amount) {
        PayslipItem item = PayslipItem.builder()
                .payslip(payslip)
                .itemName(name)
                .itemType(type)
                .amount(amount)
                .build();
        item.setTenantId(payslip.getTenantId());
        return item;
    }

    private PayrollRunResponse toRunResponse(PayrollRun r) {
        return new PayrollRunResponse(
                r.getId(),
                r.getPeriodStart(),
                r.getPeriodEnd(),
                r.getStatus(),
                r.getPayoutDate(),
                r.getTotalGross(),
                r.getTotalDeductions(),
                r.getTotalNet(),
                r.getRunType()
        );
    }

    private PayslipResponse toPayslipResponse(Payslip p) {
        String empName = p.getEmployee().getFirstName() + " " + p.getEmployee().getLastName();
        
        List<PayslipItemResponse> items = p.getItems().stream()
                .map(i -> new PayslipItemResponse(
                        i.getId(),
                        i.getItemName(),
                        i.getItemType(),
                        i.getAmount()
                ))
                .collect(Collectors.toList());

        return new PayslipResponse(
                p.getId(),
                p.getPayrollRun().getId(),
                p.getEmployee().getId(),
                empName,
                p.getBasicSalary(),
                p.getAllowances(),
                p.getDeductions(),
                p.getTaxDeductions(),
                p.getNetSalary(),
                p.getWorkingDays(),
                p.getPresentDays(),
                p.getLeaveDays(),
                p.getCurrencyCode(),
                p.getStatus(),
                p.getSentAt(),
                items
        );
    }
}
