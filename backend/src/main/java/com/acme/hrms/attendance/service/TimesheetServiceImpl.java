package com.acme.hrms.attendance.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acme.hrms.attendance.dto.TimesheetApproveRequest;
import com.acme.hrms.attendance.dto.TimesheetCreateRequest;
import com.acme.hrms.attendance.dto.TimesheetLineResponse;
import com.acme.hrms.attendance.dto.TimesheetResponse;
import com.acme.hrms.attendance.entity.Timesheet;
import com.acme.hrms.attendance.entity.TimesheetLine;
import com.acme.hrms.attendance.repository.TimesheetLineRepository;
import com.acme.hrms.attendance.repository.TimesheetRepository;
import com.acme.hrms.common.error.ConflictException;
import com.acme.hrms.common.error.NotFoundException;
import com.acme.hrms.common.tenant.TenantContext;
import com.acme.hrms.employee.entity.Employee;
import com.acme.hrms.employee.repository.EmployeeRepository;

@Service
public class TimesheetServiceImpl implements TimesheetService {

    private final TimesheetRepository timesheetRepository;
    private final TimesheetLineRepository timesheetLineRepository;
    private final EmployeeRepository employeeRepository;

    public TimesheetServiceImpl(TimesheetRepository timesheetRepository,
                                TimesheetLineRepository timesheetLineRepository,
                                EmployeeRepository employeeRepository) {
        this.timesheetRepository = timesheetRepository;
        this.timesheetLineRepository = timesheetLineRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public TimesheetResponse createOrUpdateTimesheet(UUID employeeId, TimesheetCreateRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> NotFoundException.of("Employee", employeeId));

        Timesheet timesheet = timesheetRepository.findByEmployeeIdAndStartDate(employeeId, request.startDate())
                .orElse(null);

        if (timesheet != null) {
            if (!"DRAFT".equals(timesheet.getStatus()) && !"REJECTED".equals(timesheet.getStatus())) {
                throw new ConflictException("Timesheet is already submitted or approved and cannot be modified.");
            }
            timesheet.setSubmissionComments(request.submissionComments());
            timesheet.setEndDate(request.endDate());
        } else {
            timesheet = Timesheet.builder()
                    .employee(employee)
                    .startDate(request.startDate())
                    .endDate(request.endDate())
                    .status("DRAFT")
                    .submissionComments(request.submissionComments())
                    .build();
            timesheet.setTenantId(TenantContext.getTenantId() != null ? TenantContext.getTenantId() : employee.getTenantId());
        }

        // Rebuild lines
        timesheet.getLines().clear();
        BigDecimal totalHours = BigDecimal.ZERO;
        
        if (request.lines() != null) {
            for (var lineReq : request.lines()) {
                TimesheetLine line = TimesheetLine.builder()
                        .timesheet(timesheet)
                        .dayDate(lineReq.dayDate())
                        .hoursWorked(lineReq.hoursWorked())
                        .notes(lineReq.notes())
                        .build();
                line.setTenantId(timesheet.getTenantId());
                timesheet.getLines().add(line);
                totalHours = totalHours.add(lineReq.hoursWorked());
            }
        }
        
        timesheet.setTotalHours(totalHours);
        Timesheet saved = timesheetRepository.save(timesheet);

        return toResponse(saved);
    }

    @Override
    @Transactional
    public TimesheetResponse submitTimesheet(UUID timesheetId) {
        Timesheet timesheet = timesheetRepository.findById(timesheetId)
                .orElseThrow(() -> NotFoundException.of("Timesheet", timesheetId));

        if (!"DRAFT".equals(timesheet.getStatus()) && !"REJECTED".equals(timesheet.getStatus())) {
            throw new ConflictException("Only DRAFT or REJECTED timesheets can be submitted.");
        }

        timesheet.setStatus("SUBMITTED");
        Timesheet saved = timesheetRepository.save(timesheet);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public TimesheetResponse approveTimesheet(UUID timesheetId, UUID approverId, TimesheetApproveRequest request) {
        Timesheet timesheet = timesheetRepository.findById(timesheetId)
                .orElseThrow(() -> NotFoundException.of("Timesheet", timesheetId));

        if (!"SUBMITTED".equals(timesheet.getStatus())) {
            throw new ConflictException("Only SUBMITTED timesheets can be approved.");
        }

        Employee approver = employeeRepository.findById(approverId)
                .orElseThrow(() -> NotFoundException.of("Employee", approverId));

        timesheet.setStatus("APPROVED");
        timesheet.setApprovedBy(approver);
        timesheet.setApprovedAt(Instant.now());
        timesheet.setApprovalComments(request.approvalComments());

        Timesheet saved = timesheetRepository.save(timesheet);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public TimesheetResponse rejectTimesheet(UUID timesheetId, UUID approverId, TimesheetApproveRequest request) {
        Timesheet timesheet = timesheetRepository.findById(timesheetId)
                .orElseThrow(() -> NotFoundException.of("Timesheet", timesheetId));

        if (!"SUBMITTED".equals(timesheet.getStatus())) {
            throw new ConflictException("Only SUBMITTED timesheets can be rejected.");
        }

        Employee approver = employeeRepository.findById(approverId)
                .orElseThrow(() -> NotFoundException.of("Employee", approverId));

        timesheet.setStatus("REJECTED");
        timesheet.setApprovedBy(approver);
        timesheet.setApprovedAt(Instant.now());
        timesheet.setApprovalComments(request.approvalComments());

        Timesheet saved = timesheetRepository.save(timesheet);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimesheetResponse> listEmployeeTimesheets(UUID employeeId) {
        return timesheetRepository.findByEmployeeIdOrderByStartDateDesc(employeeId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimesheetResponse> listPendingApprovals() {
        UUID tenantId = TenantContext.getTenantId();
        return timesheetRepository.findByTenantIdAndStatus(tenantId, "SUBMITTED").stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TimesheetResponse getTimesheet(UUID timesheetId) {
        Timesheet timesheet = timesheetRepository.findById(timesheetId)
                .orElseThrow(() -> NotFoundException.of("Timesheet", timesheetId));
        return toResponse(timesheet);
    }

    private TimesheetResponse toResponse(Timesheet t) {
        String empName = t.getEmployee().getFirstName() + " " + t.getEmployee().getLastName();
        
        UUID appById = t.getApprovedBy() != null ? t.getApprovedBy().getId() : null;
        String appByName = t.getApprovedBy() != null ? t.getApprovedBy().getFirstName() + " " + t.getApprovedBy().getLastName() : null;

        List<TimesheetLineResponse> linesResponse = t.getLines().stream()
                .map(l -> new TimesheetLineResponse(
                        l.getId(),
                        l.getDayDate(),
                        l.getHoursWorked(),
                        l.getNotes()
                ))
                .collect(Collectors.toList());

        return new TimesheetResponse(
                t.getId(),
                t.getEmployee().getId(),
                empName,
                t.getStartDate(),
                t.getEndDate(),
                t.getTotalHours(),
                t.getStatus(),
                appById,
                appByName,
                t.getApprovedAt(),
                t.getSubmissionComments(),
                t.getApprovalComments(),
                linesResponse
        );
    }
}
