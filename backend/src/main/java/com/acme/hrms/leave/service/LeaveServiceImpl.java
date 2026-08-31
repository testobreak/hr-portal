package com.acme.hrms.leave.service;

import com.acme.hrms.common.error.ConflictException;
import com.acme.hrms.common.error.NotFoundException;
import com.acme.hrms.employee.entity.Employee;
import com.acme.hrms.employee.repository.EmployeeRepository;
import com.acme.hrms.leave.dto.LeaveBalanceResponse;
import com.acme.hrms.leave.dto.LeaveLedgerEntryDto;
import com.acme.hrms.leave.entity.*;
import com.acme.hrms.leave.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LeaveServiceImpl implements LeaveService {

    private final LeaveTypeRepository leaveTypeRepository;
    private final LeavePolicyRepository leavePolicyRepository;
    private final LeavePolicyVersionRepository leavePolicyVersionRepository;
    private final LeavePolicyAssignmentRepository leavePolicyAssignmentRepository;
    private final LeaveLedgerEntryRepository ledgerRepository;
    private final LeaveAccrualRunRepository accrualRunRepository;
    private final LeaveAccrualRunItemRepository accrualRunItemRepository;
    private final EmployeeRepository employeeRepository;
    private final ObjectMapper objectMapper;

    public LeaveServiceImpl(LeaveTypeRepository leaveTypeRepository,
                            LeavePolicyRepository leavePolicyRepository,
                            LeavePolicyVersionRepository leavePolicyVersionRepository,
                            LeavePolicyAssignmentRepository leavePolicyAssignmentRepository,
                            LeaveLedgerEntryRepository ledgerRepository,
                            LeaveAccrualRunRepository accrualRunRepository,
                            LeaveAccrualRunItemRepository accrualRunItemRepository,
                            EmployeeRepository employeeRepository,
                            ObjectMapper objectMapper) {
        this.leaveTypeRepository = leaveTypeRepository;
        this.leavePolicyRepository = leavePolicyRepository;
        this.leavePolicyVersionRepository = leavePolicyVersionRepository;
        this.leavePolicyAssignmentRepository = leavePolicyAssignmentRepository;
        this.ledgerRepository = ledgerRepository;
        this.accrualRunRepository = accrualRunRepository;
        this.accrualRunItemRepository = accrualRunItemRepository;
        this.employeeRepository = employeeRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public List<LeaveBalanceResponse> getLeaveBalances(UUID employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> NotFoundException.of("Employee", employeeId));

        initDefaults(employee.getTenantId(), employeeId);

        List<LeaveType> leaveTypes = leaveTypeRepository.findAll().stream()
                .filter(t -> employee.getTenantId().equals(t.getTenantId()))
                .collect(Collectors.toList());

        List<LeaveLedgerEntry> entries = ledgerRepository.findByEmployeeId(employeeId);

        List<LeaveBalanceResponse> balances = new ArrayList<>();
        for (LeaveType type : leaveTypes) {
            BigDecimal sum = entries.stream()
                    .filter(e -> type.getId().equals(e.getLeaveTypeId()))
                    .map(LeaveLedgerEntry::getQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            balances.add(LeaveBalanceResponse.builder()
                    .leaveTypeId(type.getId())
                    .leaveTypeCode(type.getCode())
                    .leaveTypeName(type.getName())
                    .balance(sum)
                    .build());
        }
        return balances;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveLedgerEntryDto> getLedger(UUID employeeId, UUID leaveTypeId) {
        return ledgerRepository.findByEmployeeIdAndLeaveTypeId(employeeId, leaveTypeId).stream()
                .map(e -> LeaveLedgerEntryDto.builder()
                        .id(e.getId())
                        .transactionType(e.getTransactionType())
                        .quantity(e.getQuantity())
                        .effectiveDate(e.getEffectiveDate())
                        .sourceReference(e.getSourceReference())
                        .createdAt(e.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void adjustLeaveBalance(UUID employeeId, UUID leaveTypeId, BigDecimal quantity, String reason) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> NotFoundException.of("Employee", employeeId));

        LeaveType leaveType = leaveTypeRepository.findById(leaveTypeId)
                .orElseThrow(() -> NotFoundException.of("LeaveType", leaveTypeId));

        LeaveLedgerEntry entry = LeaveLedgerEntry.builder()
                .tenantId(employee.getTenantId())
                .employeeId(employeeId)
                .leaveTypeId(leaveTypeId)
                .transactionType("ADJUSTMENT")
                .quantity(quantity)
                .effectiveDate(LocalDate.now())
                .sourceReference(reason)
                .build();

        ledgerRepository.save(entry);
    }

    @Override
    @Transactional
    public void runAccrual(String period) {
        // period is e.g. "2026-07"
        if (accrualRunRepository.findByPeriod(period).isPresent()) {
            throw new ConflictException("Accrual run for period " + period + " already exists");
        }

        List<Employee> employeesList = employeeRepository.findAll();
        if (employeesList.isEmpty()) return;

        UUID tenantId = employeesList.get(0).getTenantId();

        LeaveAccrualRun run = LeaveAccrualRun.builder()
                .tenantId(tenantId)
                .runDate(LocalDate.now())
                .period(period)
                .build();
        LeaveAccrualRun savedRun = accrualRunRepository.save(run);

        // Find standard leave types
        List<LeaveType> leaveTypes = leaveTypeRepository.findAll().stream()
                .filter(t -> tenantId.equals(t.getTenantId()))
                .collect(Collectors.toList());

        for (Employee emp : employeesList) {
            initDefaults(tenantId, emp.getId());

            // Load policy assignment
            List<LeavePolicyAssignment> assignments = leavePolicyAssignmentRepository.findByEmployeeId(emp.getId());
            if (assignments.isEmpty()) continue;

            LeavePolicyAssignment assignment = assignments.get(0);
            LeavePolicyVersion version = leavePolicyVersionRepository.findById(assignment.getPolicyVersionId())
                    .orElseThrow(() -> NotFoundException.of("LeavePolicyVersion", assignment.getPolicyVersionId()));

            Map<String, Object> rules;
            try {
                rules = objectMapper.readValue(version.getRulesJson(), new TypeReference<Map<String, Object>>() {});
            } catch (Exception ex) {
                continue;
            }

            BigDecimal accrualRate = new BigDecimal(rules.getOrDefault("accrualRate", "1.5").toString());

            for (LeaveType type : leaveTypes) {
                // We accrue for ANNUAL leave types by default
                if ("ANNUAL".equalsIgnoreCase(type.getCode())) {
                    String idempotencyKey = period + ":" + emp.getId() + ":" + type.getId();

                    Optional<LeaveAccrualRunItem> itemOpt = accrualRunItemRepository.findByIdempotencyKey(idempotencyKey);
                    if (itemOpt.isPresent()) {
                        continue; // Idempotent check
                    }

                    LeaveAccrualRunItem item = LeaveAccrualRunItem.builder()
                            .tenantId(tenantId)
                            .runId(savedRun.getId())
                            .employeeId(emp.getId())
                            .leaveTypeId(type.getId())
                            .amount(accrualRate)
                            .idempotencyKey(idempotencyKey)
                            .build();

                    accrualRunItemRepository.save(item);

                    LeaveLedgerEntry entry = LeaveLedgerEntry.builder()
                            .tenantId(tenantId)
                            .employeeId(emp.getId())
                            .leaveTypeId(type.getId())
                            .transactionType("ACCRUAL")
                            .quantity(accrualRate)
                            .effectiveDate(LocalDate.now())
                            .sourceReference("Accrual run " + savedRun.getId())
                            .build();

                    ledgerRepository.save(entry);
                }
            }
        }
    }

    private void initDefaults(UUID tenantId, UUID employeeId) {
        List<LeaveType> leaveTypes = leaveTypeRepository.findAll().stream()
                .filter(t -> tenantId.equals(t.getTenantId()))
                .collect(Collectors.toList());

        if (leaveTypes.isEmpty()) {
            LeaveType annual = LeaveType.builder().code("ANNUAL").name("Annual Leave").build();
            annual.setTenantId(tenantId);
            leaveTypeRepository.save(annual);

            LeaveType sick = LeaveType.builder().code("SICK").name("Sick Leave").build();
            sick.setTenantId(tenantId);
            leaveTypeRepository.save(sick);

            leaveTypes.add(annual);
            leaveTypes.add(sick);
        }

        List<LeavePolicy> policies = leavePolicyRepository.findAll().stream()
                .filter(p -> tenantId.equals(p.getTenantId()))
                .collect(Collectors.toList());

        LeavePolicyVersion activeVersion = null;
        if (policies.isEmpty()) {
            LeavePolicy p = LeavePolicy.builder().name("Standard Leave Policy").description("Default corporate leave policy").build();
            p.setTenantId(tenantId);
            leavePolicyRepository.save(p);

            LeavePolicyVersion version = LeavePolicyVersion.builder()
                    .policyId(p.getId())
                    .versionNumber(1)
                    .effectiveFrom(LocalDate.now().minusYears(1))
                    .rulesJson("{\"accrualRate\": 1.5, \"noticeDays\": 2}")
                    .status("PUBLISHED")
                    .build();
            version.setTenantId(tenantId);
            activeVersion = leavePolicyVersionRepository.save(version);
        } else {
            List<LeavePolicyVersion> versions = leavePolicyVersionRepository.findByPolicyId(policies.get(0).getId());
            if (!versions.isEmpty()) {
                activeVersion = versions.get(0);
            }
        }

        List<LeavePolicyAssignment> assignments = leavePolicyAssignmentRepository.findByEmployeeId(employeeId);
        if (assignments.isEmpty() && activeVersion != null) {
            LeavePolicyAssignment ass = LeavePolicyAssignment.builder()
                    .employeeId(employeeId)
                    .policyVersionId(activeVersion.getId())
                    .effectiveFrom(LocalDate.now().minusYears(1))
                    .build();
            ass.setTenantId(tenantId);
            leavePolicyAssignmentRepository.save(ass);

            // Populate opening balance of 10.00
            for (LeaveType type : leaveTypes) {
                if (ledgerRepository.findByEmployeeIdAndLeaveTypeId(employeeId, type.getId()).isEmpty()) {
                    LeaveLedgerEntry entry = LeaveLedgerEntry.builder()
                            .tenantId(tenantId)
                            .employeeId(employeeId)
                            .leaveTypeId(type.getId())
                            .transactionType("OPENING_BALANCE")
                            .quantity(BigDecimal.valueOf(10.00))
                            .effectiveDate(LocalDate.now().minusMonths(1))
                            .build();
                    ledgerRepository.save(entry);
                }
            }
        }
    }
}
