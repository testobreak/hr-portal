package com.acme.hrms.leave.service;

import com.acme.hrms.leave.dto.LeaveRequestResponseDto;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface LeaveRequestService {
    LeaveRequestResponseDto createRequest(UUID employeeId, UUID leaveTypeId, LocalDate start, LocalDate end, String reason);
    void submitRequest(UUID requestId);
    void onWorkflowComplete(UUID requestId, String status);
    List<LeaveRequestResponseDto> getMyRequests(UUID employeeId);
    List<LeaveRequestResponseDto> getTeamCalendar(UUID managerId, LocalDate start, LocalDate end);
    void cancelRequest(UUID requestId);
}
