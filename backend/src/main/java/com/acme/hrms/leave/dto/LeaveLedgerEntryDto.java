package com.acme.hrms.leave.dto;

import java.math.BigDecimal;
import java.time.Instant;
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
public class LeaveLedgerEntryDto {
    private UUID id;
    private String transactionType;
    private BigDecimal quantity;
    private LocalDate effectiveDate;
    private String sourceReference;
    private Instant createdAt;
}
