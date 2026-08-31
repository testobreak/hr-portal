package com.acme.hrms.manager.repository;

import com.acme.hrms.manager.entity.ManagerDelegation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ManagerDelegationRepository extends JpaRepository<ManagerDelegation, UUID> {

    @Query("SELECT d FROM ManagerDelegation d WHERE d.managerId = :managerId AND d.status = 'ACTIVE'")
    List<ManagerDelegation> findActiveByManager(@Param("managerId") UUID managerId);

    @Query("SELECT d FROM ManagerDelegation d WHERE d.delegateId = :delegateId AND d.status = 'ACTIVE' AND d.startDate <= :date AND d.endDate >= :date")
    List<ManagerDelegation> findActiveDelegationsForDelegate(@Param("delegateId") UUID delegateId, @Param("date") LocalDate date);
}
