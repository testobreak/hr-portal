package com.acme.hrms.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClientCreateRequest(
        @NotBlank @Size(max = 32) String code,
        @NotBlank @Size(max = 128) String name,
        @Size(max = 1024) String description
) {
}
