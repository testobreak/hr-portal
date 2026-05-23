package com.acme.hrms.common.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.acme.hrms.common.audit.dto.AuditLogEntryResponse;
import com.acme.hrms.common.security.Roles;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/audit-logs")
@Tag(name = "Audit log")
public class AuditLogController {

    private final AuditLogQueryService auditLogQueryService;

    public AuditLogController(AuditLogQueryService auditLogQueryService) {
        this.auditLogQueryService = auditLogQueryService;
    }

    @GetMapping
    @PreAuthorize("hasRole('" + Roles.SUPER_ADMIN + "')")
    @Operation(summary = "Paged audit trail (SUPER_ADMIN only; matrix §9)")
    public Page<AuditLogEntryResponse> list(Pageable pageable) {
        return auditLogQueryService.recent(pageable);
    }
}
