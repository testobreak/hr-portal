package com.acme.hrms.leave.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequestResponseDto {
    private UUID id;
    private UUID employeeId;
    private UUID leaveTypeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String reason;
    private BigDecimal totalDays;
}
