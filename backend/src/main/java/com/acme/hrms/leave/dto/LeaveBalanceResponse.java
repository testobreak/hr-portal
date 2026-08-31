package com.acme.hrms.leave.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalanceResponse {
    private UUID leaveTypeId;
    private String leaveTypeCode;
    private String leaveTypeName;
    private BigDecimal balance;
}
