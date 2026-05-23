package com.acme.hrms.dashboard.dto;

public record HrOverviewResponse(
        long headcountActive,
        long joinersLast30Days,
        long leaversLast30Days
) {
}
