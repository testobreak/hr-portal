package com.acme.hrms.attendance.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.acme.hrms.attendance.dto.AttendanceResponse;
import com.acme.hrms.attendance.dto.ClockInRequest;
import com.acme.hrms.attendance.dto.ClockOutRequest;

public interface AttendanceService {
    AttendanceResponse clockIn(UUID employeeId, ClockInRequest request);
    AttendanceResponse clockOut(UUID employeeId, ClockOutRequest request);
    AttendanceResponse getLatestLog(UUID employeeId);
    List<AttendanceResponse> getLogsForPeriod(UUID employeeId, Instant start, Instant end);
    List<AttendanceResponse> getTenantLogsForPeriod(Instant start, Instant end);
}
