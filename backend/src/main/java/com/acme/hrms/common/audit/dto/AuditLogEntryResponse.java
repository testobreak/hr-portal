package com.acme.hrms.common.audit.dto;

import java.time.Instant;
import java.util.UUID;

public record AuditLogEntryResponse(
        UUID id,
        Instant at,
        UUID actorId,
        String actorLabel,
        String action,
        String entity,
        UUID entityId,
        String requestId,
        String detail
) {
}
