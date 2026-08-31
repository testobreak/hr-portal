package com.acme.hrms.manager.repository;

import com.acme.hrms.manager.entity.ManagerHierarchyId;
import com.acme.hrms.manager.entity.ManagerHierarchyProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ManagerHierarchyRepository extends JpaRepository<ManagerHierarchyProjection, ManagerHierarchyId> {

    @Query("SELECT p FROM ManagerHierarchyProjection p WHERE p.managerId = :managerId AND (p.effectiveTo IS NULL OR p.effectiveTo >= CURRENT_DATE)")
    List<ManagerHierarchyProjection> findActiveReports(@Param("managerId") UUID managerId);

    @Query("SELECT p FROM ManagerHierarchyProjection p WHERE p.managerId = :managerId AND p.depth = 1 AND (p.effectiveTo IS NULL OR p.effectiveTo >= CURRENT_DATE)")
    List<ManagerHierarchyProjection> findActiveDirectReports(@Param("managerId") UUID managerId);
}
