package com.acme.hrms.workflow.dto;

import java.time.Instant;
import java.util.UUID;

public record ApprovalRequestResponse(
    UUID id,
    UUID requesterId,
    UUID employeeId,
    String type,
    String changeJson,
    String status,
    UUID approvedBy,
    Instant approvedAt,
    Instant createdAt,
    Long version
) {}
