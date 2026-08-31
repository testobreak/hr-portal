package com.acme.hrms.employee.dto;

import java.util.UUID;

public record LegalEntityResponse(
    UUID id,
    String code,
    String name,
    Long version
) {}
