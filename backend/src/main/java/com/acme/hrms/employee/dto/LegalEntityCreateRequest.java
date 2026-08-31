package com.acme.hrms.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LegalEntityCreateRequest(
    @NotBlank @Size(min = 2, max = 16) String code,
    @NotBlank @Size(min = 2, max = 128) String name
) {}
