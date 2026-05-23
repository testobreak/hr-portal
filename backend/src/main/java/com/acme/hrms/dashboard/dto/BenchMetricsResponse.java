package com.acme.hrms.dashboard.dto;

/**
 * Bench / utilization snapshot.
 *
 * <p><strong>Organization-wide</strong> (SUPER_ADMIN, FINANCE_ADMIN, LEADERSHIP):
 * {@code underutilizedOrBenchedCount} is employees with no active project allocation.
 *
 * <p><strong>Project manager portfolio</strong>: counts are limited to people with a
 * current allocation on at least one project the caller manages;
 * {@code underutilizedOrBenchedCount} is those whose total active allocation % is
 * below 100 across the company.
 */
public record BenchMetricsResponse(
        long activeRosterSize,
        long fullyUtilizedCount,
        long underutilizedOrBenchedCount,
        double benchPercentage
) {
}
