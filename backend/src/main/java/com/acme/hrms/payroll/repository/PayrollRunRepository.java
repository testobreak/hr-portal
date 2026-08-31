package com.acme.hrms.payroll.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.acme.hrms.payroll.entity.PayrollRun;

public interface PayrollRunRepository extends JpaRepository<PayrollRun, UUID> {
    List<PayrollRun> findByTenantIdOrderByPeriodStartDesc(UUID tenantId);
}
