package com.acme.hrms.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LocationCreateRequest(
        @NotBlank @Size(max = 32) String code,
        @NotBlank @Size(max = 128) String name,
        @Size(max = 64) String city,
        @Size(max = 64) String country
) {
}
