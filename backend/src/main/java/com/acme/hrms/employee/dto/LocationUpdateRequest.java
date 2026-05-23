package com.acme.hrms.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LocationUpdateRequest(
        @NotBlank @Size(max = 128) String name,
        @Size(max = 64) String city,
        @Size(max = 64) String country,
        @NotNull Long version
) {
}
