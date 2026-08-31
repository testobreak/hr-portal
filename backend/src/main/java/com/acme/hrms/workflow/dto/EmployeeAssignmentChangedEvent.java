package com.acme.hrms.workflow.dto;

import java.util.UUID;

public record EmployeeAssignmentChangedEvent(
    UUID employeeId,
    UUID departmentId,
    UUID designationId,
    UUID locationId,
    UUID managerId,
    String effectiveFrom
) {}
