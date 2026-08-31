package com.acme.hrms.leave.service;

import com.acme.hrms.leave.dto.LeaveBalanceResponse;
import com.acme.hrms.leave.dto.LeaveLedgerEntryDto;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface LeaveService {
    List<LeaveBalanceResponse> getLeaveBalances(UUID employeeId);
    List<LeaveLedgerEntryDto> getLedger(UUID employeeId, UUID leaveTypeId);
    void adjustLeaveBalance(UUID employeeId, UUID leaveTypeId, BigDecimal quantity, String reason);
    void runAccrual(String period);
}
