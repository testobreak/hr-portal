package com.acme.hrms.common.audit;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Composite key for {@link AuditLog}. JPA requires the IdClass to be a
 * plain serializable Java object whose fields match the @Id columns in
 * type and name.
 */
public class AuditLogId implements Serializable {

    private UUID id;
    private Instant at;

    public AuditLogId() {
    }

    public AuditLogId(UUID id, Instant at) {
        this.id = id;
        this.at = at;
    }

    public UUID getId() {
        return id;
    }

    public Instant getAt() {
        return at;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuditLogId other)) return false;
        return Objects.equals(id, other.id) && Objects.equals(at, other.at);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, at);
    }
}
