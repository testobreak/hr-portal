package com.acme.hrms.employee.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Self-service update. EMPLOYEE callers can change their own contact info
 * but not their HR fields (department, designation, manager, etc.).
 */
public record EmployeeContactUpdateRequest(
        @Size(max = 32)
        @Pattern(regexp = "^[+0-9 ()-]*$", message = "phone number contains invalid characters")
        String phoneNumber,
        @NotNull Long version
) {
}
