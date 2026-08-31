package com.acme.hrms.recruitment.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record OfferResponse(
        UUID id,
        UUID candidateApplicationId,
        String candidateName,
        String candidateEmail,
        String jobTitle,
        BigDecimal salaryAmount,
        String currencyCode,
        LocalDate startDate,
        String status,
        UUID secureToken,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
