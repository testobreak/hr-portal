package com.acme.hrms.leave.repository;

import com.acme.hrms.leave.entity.LeaveRequestDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface LeaveRequestDayRepository extends JpaRepository<LeaveRequestDay, UUID> {
    List<LeaveRequestDay> findByLeaveRequestId(UUID leaveRequestId);
}
