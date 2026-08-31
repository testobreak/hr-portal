package com.acme.hrms.employee.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.acme.hrms.employee.entity.EmployeeAssignmentHistory;

@Repository
public interface EmployeeAssignmentHistoryRepository extends JpaRepository<EmployeeAssignmentHistory, UUID> {

    Optional<EmployeeAssignmentHistory> findFirstByEmployeeIdOrderByEffectiveFromDesc(UUID employeeId);

    List<EmployeeAssignmentHistory> findAllByEmployeeIdOrderByEffectiveFromDesc(UUID employeeId);

    @Query("SELECT h FROM EmployeeAssignmentHistory h WHERE h.employee.id = :employeeId AND h.deletedAt IS NULL ORDER BY h.effectiveFrom DESC")
    List<EmployeeAssignmentHistory> findHistoryForEmployee(@Param("employeeId") UUID employeeId);
}
