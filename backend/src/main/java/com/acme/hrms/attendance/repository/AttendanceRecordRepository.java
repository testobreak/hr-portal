package com.acme.hrms.attendance.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.acme.hrms.attendance.entity.AttendanceRecord;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, UUID> {

    List<AttendanceRecord> findByEmployeeIdAndClockInBetween(UUID employeeId, Instant start, Instant end);

    List<AttendanceRecord> findByTenantIdAndClockInBetween(UUID tenantId, Instant start, Instant end);

    Optional<AttendanceRecord> findTopByEmployeeIdAndClockOutIsNullOrderByClockInDesc(UUID employeeId);

    @Query("SELECT a FROM AttendanceRecord a WHERE a.employee.id = :employeeId AND a.clockIn >= :start AND a.clockIn <= :end")
    List<AttendanceRecord> findLogsForPeriod(@Param("employeeId") UUID employeeId, @Param("start") Instant start, @Param("end") Instant end);
}
