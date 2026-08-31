package com.acme.hrms.payroll.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PayslipItemResponse(
        UUID id,
        String itemName,
        String itemType,
        BigDecimal amount
) {
}
