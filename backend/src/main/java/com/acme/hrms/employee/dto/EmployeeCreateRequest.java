package com.acme.hrms.employee.dto;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.acme.hrms.employee.entity.EmploymentStatus;

public record EmployeeCreateRequest(
        @NotBlank @Size(max = 32) String employeeCode,
        @NotBlank @Size(max = 64) String firstName,
        @NotBlank @Size(max = 64) String lastName,
        @NotBlank @Email @Size(max = 254) String email,
        @Size(max = 32)
        @Pattern(regexp = "^[+0-9 ()-]*$", message = "phone number contains invalid characters")
        String phoneNumber,
        @Past LocalDate dateOfBirth,
        @NotNull @PastOrPresent LocalDate dateOfJoining,
        EmploymentStatus employmentStatus,
        UUID keycloakUserId,
        UUID departmentId,
        UUID designationId,
        UUID locationId,
        UUID legalEntityId,
        UUID managerId,
        java.util.List<String> roles,
        String keycloakPassword
) {
}
