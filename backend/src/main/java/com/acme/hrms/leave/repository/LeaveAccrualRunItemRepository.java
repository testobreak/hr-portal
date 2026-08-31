package com.acme.hrms.leave.repository;

import com.acme.hrms.leave.entity.LeaveAccrualRunItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaveAccrualRunItemRepository extends JpaRepository<LeaveAccrualRunItem, UUID> {
    Optional<LeaveAccrualRunItem> findByIdempotencyKey(String idempotencyKey);
}
