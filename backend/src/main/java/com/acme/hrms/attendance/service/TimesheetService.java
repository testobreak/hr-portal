package com.acme.hrms.attendance.service;

import java.util.List;
import java.util.UUID;

import com.acme.hrms.attendance.dto.TimesheetApproveRequest;
import com.acme.hrms.attendance.dto.TimesheetCreateRequest;
import com.acme.hrms.attendance.dto.TimesheetResponse;

public interface TimesheetService {
    TimesheetResponse createOrUpdateTimesheet(UUID employeeId, TimesheetCreateRequest request);
    TimesheetResponse submitTimesheet(UUID timesheetId);
    TimesheetResponse approveTimesheet(UUID timesheetId, UUID approverId, TimesheetApproveRequest request);
    TimesheetResponse rejectTimesheet(UUID timesheetId, UUID approverId, TimesheetApproveRequest request);
    List<TimesheetResponse> listEmployeeTimesheets(UUID employeeId);
    List<TimesheetResponse> listPendingApprovals();
    TimesheetResponse getTimesheet(UUID timesheetId);
}
