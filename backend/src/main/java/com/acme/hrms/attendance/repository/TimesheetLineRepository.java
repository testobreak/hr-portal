package com.acme.hrms.attendance.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.acme.hrms.attendance.entity.TimesheetLine;

public interface TimesheetLineRepository extends JpaRepository<TimesheetLine, UUID> {
}
