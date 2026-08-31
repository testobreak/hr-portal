package com.acme.hrms.profile.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileChangeRequestResponse {
    private UUID id;
    private UUID employeeId;
    private String status;
    private UUID requestedBy;
    private Instant requestedAt;
    private UUID approvedBy;
    private Instant approvedAt;
    private String changeJson;
}
