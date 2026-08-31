package com.acme.hrms.attendance.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.acme.hrms.attendance.entity.Timesheet;

public interface TimesheetRepository extends JpaRepository<Timesheet, UUID> {

    List<Timesheet> findByEmployeeIdOrderByStartDateDesc(UUID employeeId);

    List<Timesheet> findByTenantIdOrderByStartDateDesc(UUID tenantId);

    Optional<Timesheet> findByEmployeeIdAndStartDate(UUID employeeId, LocalDate startDate);

    List<Timesheet> findByTenantIdAndStatus(UUID tenantId, String status);
}
