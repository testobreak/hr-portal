package com.acme.hrms.leave.repository;

import com.acme.hrms.leave.entity.EmployeeWorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface EmployeeWorkScheduleRepository extends JpaRepository<EmployeeWorkSchedule, UUID> {
    List<EmployeeWorkSchedule> findByEmployeeId(UUID employeeId);
}
