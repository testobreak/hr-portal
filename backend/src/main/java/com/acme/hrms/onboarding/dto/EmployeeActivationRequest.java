package com.acme.hrms.onboarding.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;

public record EmployeeActivationRequest(
        @NotBlank String employeeCode,
        @Past LocalDate dateOfBirth,
        String phoneNumber
) {
}
