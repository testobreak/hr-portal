package com.acme.hrms.employee.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.acme.hrms.employee.entity.EmploymentStatus;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Public-ish view of an employee used in list endpoints.
 *
 * <p>Per architecture §3.4 (defense-in-depth field redaction), list views
 * intentionally omit fields that aren't safe for every authorised reader:
 * date of birth, phone number, manager id (org chart), salary (lives in
 * its own module). The full {@link EmployeeResponse} returns those
 * conditionally based on the caller's role and self-flag.
 */
@Schema(description = "List-view employee record. Omits sensitive personal "
        + "details — see EmployeeResponse for the full read.")
public record EmployeeSummary(
        UUID id,
        String employeeCode,
        String firstName,
        String lastName,
        String email,
        UUID departmentId,
        String departmentName,
        UUID designationId,
        String designationTitle,
        UUID locationId,
        String locationName,
        LocalDate dateOfJoining,
        EmploymentStatus employmentStatus
) {
}
