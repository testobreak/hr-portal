package com.acme.hrms.leave.repository;

import com.acme.hrms.leave.entity.LeaveAccrualRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaveAccrualRunRepository extends JpaRepository<LeaveAccrualRun, UUID> {
    Optional<LeaveAccrualRun> findByPeriod(String period);
}
