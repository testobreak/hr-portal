package com.acme.hrms.common.audit;

/**
 * Writes audit events. The implementation enriches each event with actor +
 * request id from the current security context / MDC and persists a row
 * in {@code audit_log}.
 *
 * <p>This interface is intentionally small. Reads of audit data go through
 * {@code AuditLogRepository} directly and live behind a SUPER_ADMIN-only
 * controller (Phase 6).
 */
public interface AuditService {

    /**
     * Record an audit event. Actor, request id and IP are resolved from
     * the current request context — callers do not need to pass them.
     *
     * <p>This method must never throw on transient persistence errors that
     * would otherwise mask the originating business outcome. A failed audit
     * write is logged at ERROR but does not propagate.
     */
    void record(AuditEvent event);
}
