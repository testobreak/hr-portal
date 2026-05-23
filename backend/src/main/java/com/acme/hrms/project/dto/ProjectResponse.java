package com.acme.hrms.project.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.acme.hrms.project.entity.ProjectStatus;

public record ProjectResponse(
        UUID id,
        UUID clientId,
        String clientName,
        String projectCode,
        String name,
        String description,
        UUID projectManagerId,
        String projectManagerName,
        ProjectStatus status,
        LocalDate startDate,
        LocalDate endDate,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
}
