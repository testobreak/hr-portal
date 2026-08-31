package com.acme.hrms.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LegalEntityUpdateRequest(
    @NotBlank @Size(min = 2, max = 128) String name,
    @NotNull Long version
) {}
