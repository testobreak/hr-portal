package com.acme.hrms.payroll.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.acme.hrms.payroll.entity.Payslip;

public interface PayslipRepository extends JpaRepository<Payslip, UUID> {

    List<Payslip> findByPayrollRunId(UUID payrollRunId);

    List<Payslip> findByEmployeeIdOrderByCreatedAtDesc(UUID employeeId);

    Optional<Payslip> findByPayrollRunIdAndEmployeeId(UUID payrollRunId, UUID employeeId);
}
