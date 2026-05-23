package com.acme.hrms.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ClientUpdateRequest(
        @NotBlank @Size(max = 128) String name,
        @Size(max = 1024) String description,
        @NotNull Long version
) {
}
