package com.acme.hrms.common.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.acme.hrms.common.audit.dto.AuditLogEntryResponse;

public interface AuditLogQueryService {

    Page<AuditLogEntryResponse> recent(Pageable pageable);
}
