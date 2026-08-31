package com.acme.hrms.recruitment.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OfferCreateRequest(
        BigDecimal salaryAmount,
        String currencyCode,
        LocalDate startDate
) {
}
