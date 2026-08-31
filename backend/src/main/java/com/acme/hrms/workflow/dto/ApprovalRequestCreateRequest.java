package com.acme.hrms.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ApprovalRequestCreateRequest(
    @NotNull UUID employeeId,
    @NotBlank String type,
    @NotBlank String changeJson
) {}
