package com.acme.hrms.common.audit;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, AuditLogId> {

    @Query("""
            SELECT a FROM AuditLog a
            WHERE a.entity = :entity
              AND a.entityId = :entityId
              AND a.at >= :from
              AND a.at <  :to
            ORDER BY a.at DESC
            """)
    List<AuditLog> findByEntity(@Param("entity") String entity,
                                @Param("entityId") UUID entityId,
                                @Param("from") Instant from,
                                @Param("to") Instant to);

    @Query("""
            SELECT a FROM AuditLog a
            ORDER BY a.at DESC, a.id DESC
            """)
    Page<AuditLog> findRecent(Pageable pageable);
}
