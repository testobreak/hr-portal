package com.acme.hrms.salary.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.acme.hrms.salary.entity.SalaryHistory;

@Repository
public interface SalaryHistoryRepository extends JpaRepository<SalaryHistory, UUID> {

    List<SalaryHistory> findByEmployeeIdOrderByEffectiveFromDescCreatedAtDesc(UUID employeeId);

    @Query("""
            select count(s) > 0
            from SalaryHistory s
            where s.employee.id = :employeeId
              and s.effectiveFrom <= coalesce(:effectiveTo, :infinityDate)
              and coalesce(s.effectiveTo, :infinityDate) >= :effectiveFrom
            """)
    boolean existsOverlappingRange(@Param("employeeId") UUID employeeId,
                                   @Param("effectiveFrom") LocalDate effectiveFrom,
                                   @Param("effectiveTo") LocalDate effectiveTo,
                                   @Param("infinityDate") LocalDate infinityDate);
}
