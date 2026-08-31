package com.acme.hrms.payroll.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.acme.hrms.payroll.entity.PayslipItem;

public interface PayslipItemRepository extends JpaRepository<PayslipItem, UUID> {
}
