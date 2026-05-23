package com.acme.hrms.common.audit;

import java.util.UUID;

/**
 * Service-layer DTO describing a single audit event. Service authors call
 * {@code AuditService.record(...)} with one of these.
 *
 * <p>Most fields are optional; the bare minimum is {@code action} and
 * {@code entity}. The actor and request id are filled in by the service
 * from the security context and MDC if not supplied.
 */
public record AuditEvent(
        AuditAction action,
        String entity,
        UUID entityId,
        String beforeJson,
        String afterJson,
        String detail
) {

    public static AuditEvent of(AuditAction action, String entity) {
        return new AuditEvent(action, entity, null, null, null, null);
    }

    public AuditEvent withEntityId(UUID id) {
        return new AuditEvent(action, entity, id, beforeJson, afterJson, detail);
    }

    public AuditEvent withDetail(String d) {
        return new AuditEvent(action, entity, entityId, beforeJson, afterJson, d);
    }

    public AuditEvent withBefore(String json) {
        return new AuditEvent(action, entity, entityId, json, afterJson, detail);
    }

    public AuditEvent withAfter(String json) {
        return new AuditEvent(action, entity, entityId, beforeJson, json, detail);
    }
}
