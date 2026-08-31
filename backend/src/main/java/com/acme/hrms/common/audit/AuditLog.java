package com.acme.hrms.common.audit;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.type.SqlTypes;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Append-only audit row. Maps to the partitioned {@code audit_log} table.
 *
 * <p>The PK is composite ({@code id}, {@code at}) because Postgres requires
 * the partition key in the primary key of a partitioned table. We expose
 * both as @Id columns and reference {@link AuditLogId} as the IdClass.
 *
 * <p>This entity is intentionally <em>not</em> an extension of any
 * {@code BaseEntity}: there is no {@code deleted_at}, no
 * {@code updated_at}, no {@code created_by} self-reference. Audit rows are
 * written, never edited.
 */
@Entity
@Table(name = "audit_log")
@IdClass(AuditLogId.class)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = {"id", "at"})
public class AuditLog {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Id
    @Column(name = "at", nullable = false, updatable = false)
    private Instant at;

    @Column(name = "actor_id")
    private UUID actorId;

    @Column(name = "actor_label")
    private String actorLabel;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, updatable = false, length = 32)
    private AuditAction action;

    @Column(name = "entity", nullable = false, updatable = false, length = 64)
    private String entity;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "request_id", length = 128)
    private String requestId;

    @Column(name = "ip", columnDefinition = "inet")
    @ColumnTransformer(write = "?::inet")
    private String ip;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "before_json", columnDefinition = "jsonb")
    private String beforeJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "after_json", columnDefinition = "jsonb")
    private String afterJson;

    @Column(name = "detail", length = 1024)
    private String detail;

    @org.hibernate.annotations.TenantId
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;
}
