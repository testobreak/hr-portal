package com.acme.hrms.project.dto;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.acme.hrms.project.entity.ProjectStatus;

public record ProjectUpdateRequest(
        @NotNull UUID clientId,
        @NotBlank @Size(max = 128) String name,
        @Size(max = 1024) String description,
        UUID projectManagerId,
        @NotNull ProjectStatus status,
        @NotNull LocalDate startDate,
        LocalDate endDate,
        @NotNull Long version
) {
}
