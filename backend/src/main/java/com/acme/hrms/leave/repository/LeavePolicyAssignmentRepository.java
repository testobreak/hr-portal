package com.acme.hrms.leave.repository;

import com.acme.hrms.leave.entity.LeavePolicyAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface LeavePolicyAssignmentRepository extends JpaRepository<LeavePolicyAssignment, UUID> {
    List<LeavePolicyAssignment> findByEmployeeId(UUID employeeId);
}
