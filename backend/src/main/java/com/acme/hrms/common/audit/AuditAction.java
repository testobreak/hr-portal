package com.acme.hrms.common.audit;

/**
 * Enum of audit actions. The DB-side {@code CHECK} on
 * {@code audit_log.action} accepts exactly these names — keep them in sync.
 *
 * <p>Semantics:
 * <ul>
 *   <li>{@link #CREATE} — a row was created.</li>
 *   <li>{@link #UPDATE} — a row was updated.</li>
 *   <li>{@link #SOFT_DELETE} — a row's {@code deleted_at} was set.</li>
 *   <li>{@link #HARD_DELETE} — a row was physically removed (SUPER_ADMIN only).</li>
 *   <li>{@link #READ_SENSITIVE} — a sensitive field (salary amount, document
 *       blob) was read.</li>
 *   <li>{@link #LOGIN} — a successful authenticated request was observed
 *       (recorded once per request).</li>
 *   <li>{@link #EXPORT} — a bulk download of business data (CSV/Excel/PDF).</li>
 * </ul>
 */
public enum AuditAction {
    CREATE,
    UPDATE,
    SOFT_DELETE,
    HARD_DELETE,
    READ_SENSITIVE,
    LOGIN,
    EXPORT
}
