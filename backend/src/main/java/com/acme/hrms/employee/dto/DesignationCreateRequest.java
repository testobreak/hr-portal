package com.acme.hrms.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DesignationCreateRequest(
        @NotBlank @Size(max = 128) String title,
        @Size(max = 16) String level,
        @Size(max = 1024) String description
) {
}
