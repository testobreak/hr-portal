package com.acme.hrms.employee.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.acme.hrms.employee.entity.EmploymentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Full single-employee read.
 *
 * <p>Personal fields ({@code dateOfBirth}, {@code phoneNumber}) are present
 * only when the caller has read permission for them — for self, HR, FIN,
 * SUPER. For managers / project managers, they are stripped at the service
 * layer before the response is built. {@code keycloakUserId} is exposed
 * only to HR/SUPER (admin-y info).
 */
@Schema(description = "Full employee read. Some fields are role-conditional.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EmployeeResponse(
        UUID id,
        String employeeCode,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        LocalDate dateOfBirth,
        LocalDate dateOfJoining,
        EmploymentStatus employmentStatus,
        UUID keycloakUserId,
        UUID departmentId,
        String departmentName,
        UUID designationId,
        String designationTitle,
        UUID locationId,
        String locationName,
        UUID legalEntityId,
        String legalEntityName,
        UUID managerId,
        String managerName,
        Instant createdAt,
        Instant updatedAt,
        Long version,
        java.util.List<String> roles,
        String tempPassword
) {
}
