package com.acme.hrms.common.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;

import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;
import lombok.Setter;

/**
 * Common columns for every business entity. Audit log entries do <em>not</em>
 * extend this — they are append-only and have their own shape.
 *
 * <p>Subclasses get:
 * <ul>
 *   <li>{@code id uuid PK} — generated client-side via Hibernate's
 *       time-ordered UUID generator. The DB column has {@code DEFAULT uuidv7()}
 *       as a backstop for ad-hoc inserts (e.g. via psql).</li>
 *   <li>{@code created_at}, {@code created_by} — populated by JPA auditing
 *       on first persist. {@code created_by} comes from
 *       {@link SecurityContextAuditorAware} (resolves to the calling user's
 *       Keycloak subject UUID).</li>
 *   <li>{@code updated_at}, {@code updated_by} — populated on every flush.</li>
 *   <li>{@code deleted_at} — soft delete marker. Subclasses are filtered by
 *       {@code @SQLRestriction("deleted_at IS NULL")} so default reads only
 *       see live rows.</li>
 *   <li>{@code version} — JPA optimistic lock counter. Stale updates surface
 *       as {@code ObjectOptimisticLockingFailureException} → HTTP 409.</li>
 * </ul>
 *
 * <p>This class deliberately does <em>not</em> declare {@code equals}/{@code hashCode}
 * — JPA entities should be compared by id only, and only after the id is
 * assigned. Subclasses that need set-membership semantics override these.
 */


@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by")
    private UUID updatedBy;

    @org.hibernate.annotations.TenantId
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public boolean isDeleted() {
        return deletedAt != null;
    }

    @jakarta.persistence.PrePersist
    protected void populateTenantId() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.tenantId == null) {
            UUID id = com.acme.hrms.common.tenant.TenantContext.getTenantId();
            if (id == null) {
                id = UUID.fromString("00000000-0000-0000-0000-000000000000");
            }
            this.tenantId = id;
        }
    }
}
