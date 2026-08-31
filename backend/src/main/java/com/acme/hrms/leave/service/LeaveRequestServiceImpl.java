package com.acme.hrms.leave.service;

import com.acme.hrms.common.error.ConflictException;
import com.acme.hrms.common.error.NotFoundException;
import com.acme.hrms.employee.dto.EmployeeSummary;
import com.acme.hrms.employee.entity.Employee;
import com.acme.hrms.employee.repository.EmployeeRepository;
import com.acme.hrms.leave.dto.LeaveRequestResponseDto;
import com.acme.hrms.leave.entity.*;
import com.acme.hrms.leave.repository.*;
import com.acme.hrms.manager.service.ManagerService;
import com.acme.hrms.workflow.entity.ApprovalRequest;
import com.acme.hrms.workflow.entity.ApprovalStatus;
import com.acme.hrms.workflow.repository.ApprovalRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository requestRepository;
    private final LeaveRequestDayRepository requestDayRepository;
    private final LeaveLedgerEntryRepository ledgerRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final EmployeeRepository employeeRepository;
    private final HolidayRepository holidayRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final ManagerService managerService;

    public LeaveRequestServiceImpl(LeaveRequestRepository requestRepository,
                                   LeaveRequestDayRepository requestDayRepository,
                                   LeaveLedgerEntryRepository ledgerRepository,
                                   LeaveTypeRepository leaveTypeRepository,
                                   EmployeeRepository employeeRepository,
                                   HolidayRepository holidayRepository,
                                   ApprovalRequestRepository approvalRequestRepository,
                                   ManagerService managerService) {
        this.requestRepository = requestRepository;
        this.requestDayRepository = requestDayRepository;
        this.ledgerRepository = ledgerRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.employeeRepository = employeeRepository;
        this.holidayRepository = holidayRepository;
        this.approvalRequestRepository = approvalRequestRepository;
        this.managerService = managerService;
    }

    @Override
    @Transactional
    public LeaveRequestResponseDto createRequest(UUID employeeId, UUID leaveTypeId, LocalDate start, LocalDate end, String reason) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> NotFoundException.of("Employee", employeeId));

        if (start.isAfter(end)) {
            throw new ConflictException("Start date cannot be after end date");
        }

        LeaveRequest request = LeaveRequest.builder()
                .employeeId(employeeId)
                .leaveTypeId(leaveTypeId)
                .startDate(start)
                .endDate(end)
                .status("DRAFT")
                .reason(reason)
                .build();
        request.setTenantId(employee.getTenantId());

        LeaveRequest saved = requestRepository.save(request);

        // Calculate days
        List<LocalDate> leaveDays = getLeaveDays(employeeId, start, end, employee.getTenantId());
        for (LocalDate d : leaveDays) {
            LeaveRequestDay day = LeaveRequestDay.builder()
                    .leaveRequestId(saved.getId())
                    .dayDate(d)
                    .hours(BigDecimal.valueOf(8.00)) // Default full day
                    .build();
            day.setTenantId(employee.getTenantId());
            requestDayRepository.save(day);
        }

        BigDecimal totalDays = BigDecimal.valueOf(leaveDays.size());
        return toDto(saved, totalDays);
    }

    @Override
    @Transactional
    public void submitRequest(UUID requestId) {
        LeaveRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> NotFoundException.of("LeaveRequest", requestId));

        if (!"DRAFT".equals(request.getStatus())) {
            throw new ConflictException("Leave request is not in DRAFT status");
        }

        List<LeaveRequestDay> days = requestDayRepository.findByLeaveRequestId(requestId);
        BigDecimal totalDays = BigDecimal.valueOf(days.size());

        if (totalDays.compareTo(BigDecimal.ZERO) == 0) {
            throw new ConflictException("Request contains 0 billable leave days");
        }

        // Validate Balance
        List<LeaveLedgerEntry> entries = ledgerRepository.findByEmployeeId(request.getEmployeeId());
        BigDecimal currentBalance = entries.stream()
                .filter(e -> request.getLeaveTypeId().equals(e.getLeaveTypeId()))
                .map(LeaveLedgerEntry::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (currentBalance.compareTo(totalDays) < 0) {
            throw new ConflictException("Insufficient leave balance. Available: " + currentBalance + ", Requested: " + totalDays);
        }

        // Create reservation ledger entry
        LeaveLedgerEntry reservation = LeaveLedgerEntry.builder()
                .tenantId(request.getTenantId())
                .employeeId(request.getEmployeeId())
                .leaveTypeId(request.getLeaveTypeId())
                .transactionType("RESERVATION")
                .quantity(totalDays.negate())
                .effectiveDate(request.getStartDate())
                .sourceReference("RESERVATION_" + requestId)
                .build();
        ledgerRepository.save(reservation);

        // Create Approval Request
        ApprovalRequest approvalRequest = ApprovalRequest.builder()
                .employeeId(request.getEmployeeId())
                .requesterId(request.getEmployeeId())
                .type("LEAVE_REQUEST")
                .changeJson("{\"leaveRequestId\":\"" + requestId + "\"}")
                .status(ApprovalStatus.PENDING)
                .build();
        approvalRequest.setTenantId(request.getTenantId());
        approvalRequestRepository.save(approvalRequest);

        request.setStatus("PENDING_APPROVAL");
        requestRepository.save(request);
    }

    @Override
    @Transactional
    public void onWorkflowComplete(UUID requestId, String status) {
        LeaveRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> NotFoundException.of("LeaveRequest", requestId));

        if (!"PENDING_APPROVAL".equals(request.getStatus())) {
            return;
        }

        List<LeaveLedgerEntry> reservations = ledgerRepository.findByEmployeeId(request.getEmployeeId()).stream()
                .filter(e -> ("RESERVATION_" + requestId).equals(e.getSourceReference()))
                .collect(Collectors.toList());

        if ("APPROVED".equalsIgnoreCase(status)) {
            request.setStatus("APPROVED");

            // Convert reservation to consumption
            for (LeaveLedgerEntry res : reservations) {
                res.setTransactionType("CONSUMPTION");
                res.setSourceReference("CONSUMPTION_" + requestId);
                ledgerRepository.save(res);
            }
        } else {
            request.setStatus("REJECTED");

            // Release reservation
            for (LeaveLedgerEntry res : reservations) {
                ledgerRepository.delete(res);
            }
        }
        requestRepository.save(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestResponseDto> getMyRequests(UUID employeeId) {
        return requestRepository.findByEmployeeId(employeeId).stream()
                .map(r -> {
                    List<LeaveRequestDay> days = requestDayRepository.findByLeaveRequestId(r.getId());
                    return toDto(r, BigDecimal.valueOf(days.size()));
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestResponseDto> getTeamCalendar(UUID managerId, LocalDate start, LocalDate end) {
        List<EmployeeSummary> reports = managerService.getTeam(managerId, "all");
        List<UUID> subordinateIds = reports.stream().map(EmployeeSummary::id).collect(Collectors.toList());

        return requestRepository.findAll().stream()
                .filter(r -> subordinateIds.contains(r.getEmployeeId()) && "APPROVED".equals(r.getStatus()))
                .filter(r -> !r.getStartDate().isAfter(end) && !r.getEndDate().isBefore(start))
                .map(r -> {
                    List<LeaveRequestDay> days = requestDayRepository.findByLeaveRequestId(r.getId());
                    return toDto(r, BigDecimal.valueOf(days.size()));
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelRequest(UUID requestId) {
        LeaveRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> NotFoundException.of("LeaveRequest", requestId));

        if ("CANCELLED".equals(request.getStatus())) {
            return;
        }

        request.setStatus("CANCELLED");
        requestRepository.save(request);

        // Remove any reservation or consumption ledger entry
        List<LeaveLedgerEntry> entries = ledgerRepository.findByEmployeeId(request.getEmployeeId()).stream()
                .filter(e -> ("RESERVATION_" + requestId).equals(e.getSourceReference()) ||
                             ("CONSUMPTION_" + requestId).equals(e.getSourceReference()))
                .collect(Collectors.toList());

        for (LeaveLedgerEntry entry : entries) {
            ledgerRepository.delete(entry);
        }
    }

    private List<LocalDate> getLeaveDays(UUID employeeId, LocalDate start, LocalDate end, UUID tenantId) {
        List<LocalDate> days = new ArrayList<>();
        List<Holiday> holidays = holidayRepository.findAll().stream()
                .filter(h -> tenantId.equals(h.getTenantId()))
                .collect(Collectors.toList());

        Set<LocalDate> holidayDates = holidays.stream().map(Holiday::getHolidayDate).collect(Collectors.toSet());

        LocalDate curr = start;
        while (!curr.isAfter(end)) {
            if (!holidayDates.contains(curr)) {
                int dow = curr.getDayOfWeek().getValue();
                if (dow != 6 && dow != 7) {
                    days.add(curr);
                }
            }
            curr = curr.plusDays(1);
        }
        return days;
    }

    private LeaveRequestResponseDto toDto(LeaveRequest r, BigDecimal totalDays) {
        return LeaveRequestResponseDto.builder()
                .id(r.getId())
                .employeeId(r.getEmployeeId())
                .leaveTypeId(r.getLeaveTypeId())
                .startDate(r.getStartDate())
                .endDate(r.getEndDate())
                .status(r.getStatus())
                .reason(r.getReason())
                .totalDays(totalDays)
                .build();
    }
}
