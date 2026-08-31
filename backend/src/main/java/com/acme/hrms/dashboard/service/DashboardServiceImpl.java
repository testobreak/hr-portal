package com.acme.hrms.dashboard.service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acme.hrms.common.error.ForbiddenAccessException;
import com.acme.hrms.common.security.CurrentUser;
import com.acme.hrms.common.security.Roles;
import com.acme.hrms.dashboard.dto.BenchMetricsResponse;
import com.acme.hrms.dashboard.dto.HrOverviewResponse;
import com.acme.hrms.employee.entity.Employee;
import com.acme.hrms.employee.repository.EmployeeRepository;
import com.acme.hrms.employee.service.EmployeeSpecifications;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final EmployeeRepository employees;
    private final Clock clock;

    public DashboardServiceImpl(EmployeeRepository employees, Clock clock) {
        this.employees = employees;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public HrOverviewResponse hrOverview(CurrentUser user) {
        if (!user.hasAnyRole(Roles.SUPER_ADMIN, Roles.HR_ADMIN, Roles.LEADERSHIP, Roles.MANAGER)) {
            throw new ForbiddenAccessException("Not allowed to view HR dashboard cards");
        }
        Specification<Employee> scope = hrEmployeeScope(user);
        LocalDate today = todayUtc();
        var active = EmployeeSpecifications.activeRosterStatuses();
        long headcount = employees.count(scope.and(active));
        long joiners = employees.count(scope.and(active)
                .and(EmployeeSpecifications.joinedOnOrAfter(today.minusDays(30))));
        long leavers = employees.count(scope
                .and(EmployeeSpecifications.terminalEmploymentStatuses())
                .and(EmployeeSpecifications.updatedOnOrAfter(clock.instant().minus(Duration.ofDays(30)))));
        return new HrOverviewResponse(headcount, joiners, leavers);
    }

    @Override
    @Transactional(readOnly = true)
    public BenchMetricsResponse bench(CurrentUser user) {
        if (!user.hasAnyRole(Roles.SUPER_ADMIN, Roles.FINANCE_ADMIN, Roles.LEADERSHIP, Roles.PROJECT_MANAGER)) {
            throw new ForbiddenAccessException("Not allowed to view bench metrics");
        }
        LocalDate today = todayUtc();
        if (user.hasAnyRole(Roles.SUPER_ADMIN, Roles.FINANCE_ADMIN, Roles.LEADERSHIP)) {
            return benchOrganizationWide(today);
        }
        return benchProjectManagerPortfolio(user, today);
    }

    private BenchMetricsResponse benchOrganizationWide(LocalDate today) {
        Specification<Employee> active = EmployeeSpecifications.activeRosterStatuses();
        long activeCount = employees.count(active);
        long allocated = employees.count(active.and(EmployeeSpecifications.hasActiveAllocationOn(today)));
        long benched = activeCount - allocated;
        double pct = activeCount == 0 ? 0.0 : (benched * 100.0 / activeCount);
        return new BenchMetricsResponse(activeCount, allocated, benched, round1(pct));
    }

    private BenchMetricsResponse benchProjectManagerPortfolio(CurrentUser user, LocalDate today) {
        if (user.subjectUuid() == null) {
            return new BenchMetricsResponse(0, 0, 0, 0.0);
        }
        var pool = employees.findProjectMemberIdsForManager(user.subjectUuid(), today);
        if (pool.isEmpty()) {
            return new BenchMetricsResponse(0, 0, 0, 0.0);
        }
        Specification<Employee> inPool = EmployeeSpecifications.idIn(pool);
        Specification<Employee> active = EmployeeSpecifications.activeRosterStatuses();
        long portfolio = employees.count(inPool.and(active));
        long under = employees.countUnderAllocatedAmong(pool, today);
        long fully = portfolio - under;
        double pct = portfolio == 0 ? 0.0 : (under * 100.0 / portfolio);
        return new BenchMetricsResponse(portfolio, fully, under, round1(pct));
    }

    private Specification<Employee> hrEmployeeScope(CurrentUser user) {
        if (user.hasAnyRole(Roles.SUPER_ADMIN, Roles.HR_ADMIN, Roles.LEADERSHIP)) {
            return EmployeeSpecifications.alwaysTrue();
        }
        if (user.hasRole(Roles.MANAGER)) {
            return employees.findById(user.subjectUuid())
                    .map(self -> EmployeeSpecifications.idIn(employees.findDescendantIds(self.getId())))
                    .orElseGet(EmployeeSpecifications::alwaysFalse);
        }
        return EmployeeSpecifications.alwaysFalse();
    }

    private LocalDate todayUtc() {
        return LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC);
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
