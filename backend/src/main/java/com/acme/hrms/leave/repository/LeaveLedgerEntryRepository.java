package com.acme.hrms.leave.repository;

import com.acme.hrms.leave.entity.LeaveLedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface LeaveLedgerEntryRepository extends JpaRepository<LeaveLedgerEntry, UUID> {
    List<LeaveLedgerEntry> findByEmployeeIdAndLeaveTypeId(UUID employeeId, UUID leaveTypeId);
    List<LeaveLedgerEntry> findByEmployeeId(UUID employeeId);
}
