package com.acme.hrms.dashboard.service;

import com.acme.hrms.common.security.CurrentUser;
import com.acme.hrms.dashboard.dto.BenchMetricsResponse;
import com.acme.hrms.dashboard.dto.HrOverviewResponse;

public interface DashboardService {

    HrOverviewResponse hrOverview(CurrentUser user);

    BenchMetricsResponse bench(CurrentUser user);
}
