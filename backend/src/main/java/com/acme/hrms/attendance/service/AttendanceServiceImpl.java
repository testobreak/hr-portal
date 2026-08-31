package com.acme.hrms.attendance.service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acme.hrms.attendance.dto.AttendanceResponse;
import com.acme.hrms.attendance.dto.ClockInRequest;
import com.acme.hrms.attendance.dto.ClockOutRequest;
import com.acme.hrms.attendance.entity.AttendanceRecord;
import com.acme.hrms.attendance.repository.AttendanceRecordRepository;
import com.acme.hrms.common.error.ConflictException;
import com.acme.hrms.common.error.NotFoundException;
import com.acme.hrms.common.tenant.TenantContext;
import com.acme.hrms.employee.entity.Employee;
import com.acme.hrms.employee.repository.EmployeeRepository;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EmployeeRepository employeeRepository;

    public AttendanceServiceImpl(AttendanceRecordRepository attendanceRecordRepository,
                                 EmployeeRepository employeeRepository) {
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public AttendanceResponse clockIn(UUID employeeId, ClockInRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> NotFoundException.of("Employee", employeeId));

        // Check if already clocked in
        attendanceRecordRepository.findTopByEmployeeIdAndClockOutIsNullOrderByClockInDesc(employeeId)
                .ifPresent(r -> {
                    throw new ConflictException("Employee is already clocked in since " + r.getClockIn());
                });

        Instant now = Instant.now();
        ZonedDateTime localTime = now.atZone(ZoneId.systemDefault());
        
        String status = "PRESENT";
        if (localTime.getHour() > 9 || (localTime.getHour() == 9 && localTime.getMinute() > 30)) {
            status = "LATE";
        }

        AttendanceRecord record = AttendanceRecord.builder()
                .employee(employee)
                .clockIn(now)
                .status(status)
                .ipAddress(request.ipAddress())
                .notes(request.notes())
                .build();
        
        record.setTenantId(TenantContext.getTenantId() != null ? TenantContext.getTenantId() : employee.getTenantId());
        AttendanceRecord saved = attendanceRecordRepository.save(record);

        return toResponse(saved);
    }

    @Override
    @Transactional
    public AttendanceResponse clockOut(UUID employeeId, ClockOutRequest request) {
        AttendanceRecord record = attendanceRecordRepository.findTopByEmployeeIdAndClockOutIsNullOrderByClockInDesc(employeeId)
                .orElseThrow(() -> new ConflictException("No active clock-in found for employee. Cannot clock out."));

        Instant now = Instant.now();
        record.setClockOut(now);
        
        Duration duration = Duration.between(record.getClockIn(), now);
        double hours = duration.toMinutes() / 60.0;
        record.setWorkingHours(BigDecimal.valueOf(hours).setScale(2, RoundingMode.HALF_UP));

        if (request.notes() != null && !request.notes().isEmpty()) {
            record.setNotes(record.getNotes() != null ? record.getNotes() + " | " + request.notes() : request.notes());
        }

        AttendanceRecord saved = attendanceRecordRepository.save(record);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceResponse getLatestLog(UUID employeeId) {
        return attendanceRecordRepository.findTopByEmployeeIdAndClockOutIsNullOrderByClockInDesc(employeeId)
                .map(this::toResponse)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getLogsForPeriod(UUID employeeId, Instant start, Instant end) {
        return attendanceRecordRepository.findLogsForPeriod(employeeId, start, end).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getTenantLogsForPeriod(Instant start, Instant end) {
        UUID tenantId = TenantContext.getTenantId();
        return attendanceRecordRepository.findByTenantIdAndClockInBetween(tenantId, start, end).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private AttendanceResponse toResponse(AttendanceRecord record) {
        String empName = record.getEmployee().getFirstName() + " " + record.getEmployee().getLastName();
        return new AttendanceResponse(
                record.getId(),
                record.getEmployee().getId(),
                empName,
                record.getClockIn(),
                record.getClockOut(),
                record.getStatus(),
                record.getWorkingHours(),
                record.getIpAddress(),
                record.getNotes()
        );
    }
}
