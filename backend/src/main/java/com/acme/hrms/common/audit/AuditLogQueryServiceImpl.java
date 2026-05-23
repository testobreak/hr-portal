package com.acme.hrms.common.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acme.hrms.common.audit.dto.AuditLogEntryResponse;

@Service
public class AuditLogQueryServiceImpl implements AuditLogQueryService {

    private final AuditLogRepository auditLogs;

    public AuditLogQueryServiceImpl(AuditLogRepository auditLogs) {
        this.auditLogs = auditLogs;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogEntryResponse> recent(Pageable pageable) {
        return auditLogs.findRecent(pageable).map(this::toResponse);
    }

    private AuditLogEntryResponse toResponse(AuditLog row) {
        return new AuditLogEntryResponse(
                row.getId(),
                row.getAt(),
                row.getActorId(),
                row.getActorLabel(),
                row.getAction().name(),
                row.getEntity(),
                row.getEntityId(),
                row.getRequestId(),
                row.getDetail());
    }
}
