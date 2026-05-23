package com.acme.hrms.employee.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.acme.hrms.employee.dto.EmployeeContactUpdateRequest;
import com.acme.hrms.employee.dto.EmployeeCreateRequest;
import com.acme.hrms.employee.dto.EmployeeResponse;
import com.acme.hrms.employee.dto.EmployeeSummary;
import com.acme.hrms.employee.dto.EmployeeUpdateRequest;
import com.acme.hrms.employee.entity.Employee;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface EmployeeMapper {

    @Mapping(target = "departmentId",     source = "department.id")
    @Mapping(target = "departmentName",   source = "department.name")
    @Mapping(target = "designationId",    source = "designation.id")
    @Mapping(target = "designationTitle", source = "designation.title")
    @Mapping(target = "locationId",       source = "location.id")
    @Mapping(target = "locationName",     source = "location.name")
    EmployeeSummary toSummary(Employee entity);

    @Mapping(target = "departmentId",     source = "department.id")
    @Mapping(target = "departmentName",   source = "department.name")
    @Mapping(target = "designationId",    source = "designation.id")
    @Mapping(target = "designationTitle", source = "designation.title")
    @Mapping(target = "locationId",       source = "location.id")
    @Mapping(target = "locationName",     source = "location.name")
    @Mapping(target = "managerId",        source = "manager.id")
    @Mapping(target = "managerName",      source = "manager", qualifiedByName = "fullName")
    EmployeeResponse toResponse(Employee entity);

    @Named("fullName")
    static String fullName(Employee employee) {
        if (employee == null) {
            return null;
        }
        String first = employee.getFirstName() == null ? "" : employee.getFirstName();
        String last = employee.getLastName() == null ? "" : employee.getLastName();
        String result = (first + " " + last).trim();
        return result.isEmpty() ? null : result;
    }

    /**
     * Strip personal fields. Used at the service layer for callers (manager,
     * project manager) who can read the row but not the personal fields.
     */
    default EmployeeResponse redactPersonal(EmployeeResponse full) {
        if (full == null) {
            return null;
        }
        return new EmployeeResponse(
                full.id(),
                full.employeeCode(),
                full.firstName(),
                full.lastName(),
                full.email(),
                null,                    // phoneNumber redacted
                null,                    // dateOfBirth redacted
                full.dateOfJoining(),
                full.employmentStatus(),
                null,                    // keycloakUserId redacted
                full.departmentId(),
                full.departmentName(),
                full.designationId(),
                full.designationTitle(),
                full.locationId(),
                full.locationName(),
                full.managerId(),
                full.managerName(),
                full.createdAt(),
                full.updatedAt(),
                full.version());
    }

    @Mapping(target = "id",                ignore = true)
    @Mapping(target = "createdAt",         ignore = true)
    @Mapping(target = "createdBy",         ignore = true)
    @Mapping(target = "updatedAt",         ignore = true)
    @Mapping(target = "updatedBy",         ignore = true)
    @Mapping(target = "deletedAt",         ignore = true)
    @Mapping(target = "version",           ignore = true)
    @Mapping(target = "department",        ignore = true)
    @Mapping(target = "designation",       ignore = true)
    @Mapping(target = "location",          ignore = true)
    @Mapping(target = "manager",           ignore = true)
    Employee toEntity(EmployeeCreateRequest request);

    @Mapping(target = "id",                ignore = true)
    @Mapping(target = "employeeCode",      ignore = true)
    @Mapping(target = "createdAt",         ignore = true)
    @Mapping(target = "createdBy",         ignore = true)
    @Mapping(target = "updatedAt",         ignore = true)
    @Mapping(target = "updatedBy",         ignore = true)
    @Mapping(target = "deletedAt",         ignore = true)
    @Mapping(target = "department",        ignore = true)
    @Mapping(target = "designation",       ignore = true)
    @Mapping(target = "location",          ignore = true)
    @Mapping(target = "manager",           ignore = true)
    void apply(EmployeeUpdateRequest request, @MappingTarget Employee entity);

    @Mapping(target = "id",                ignore = true)
    @Mapping(target = "employeeCode",      ignore = true)
    @Mapping(target = "firstName",         ignore = true)
    @Mapping(target = "lastName",          ignore = true)
    @Mapping(target = "email",             ignore = true)
    @Mapping(target = "dateOfBirth",       ignore = true)
    @Mapping(target = "dateOfJoining",     ignore = true)
    @Mapping(target = "employmentStatus",  ignore = true)
    @Mapping(target = "keycloakUserId",    ignore = true)
    @Mapping(target = "department",        ignore = true)
    @Mapping(target = "designation",       ignore = true)
    @Mapping(target = "location",          ignore = true)
    @Mapping(target = "manager",           ignore = true)
    @Mapping(target = "createdAt",         ignore = true)
    @Mapping(target = "createdBy",         ignore = true)
    @Mapping(target = "updatedAt",         ignore = true)
    @Mapping(target = "updatedBy",         ignore = true)
    @Mapping(target = "deletedAt",         ignore = true)
    void applyContact(EmployeeContactUpdateRequest request, @MappingTarget Employee entity);
}
