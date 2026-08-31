package com.acme.hrms.leave.repository;

import com.acme.hrms.leave.entity.WorkScheduleDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface WorkScheduleDayRepository extends JpaRepository<WorkScheduleDay, UUID> {
    List<WorkScheduleDay> findByScheduleId(UUID scheduleId);
}
