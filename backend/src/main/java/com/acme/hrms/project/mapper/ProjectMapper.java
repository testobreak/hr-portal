package com.acme.hrms.project.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.acme.hrms.employee.entity.Employee;
import com.acme.hrms.project.dto.AllocationCreateRequest;
import com.acme.hrms.project.dto.AllocationResponse;
import com.acme.hrms.project.dto.AllocationUpdateRequest;
import com.acme.hrms.project.dto.ClientCreateRequest;
import com.acme.hrms.project.dto.ClientResponse;
import com.acme.hrms.project.dto.ClientUpdateRequest;
import com.acme.hrms.project.dto.ProjectCreateRequest;
import com.acme.hrms.project.dto.ProjectResponse;
import com.acme.hrms.project.dto.ProjectUpdateRequest;
import com.acme.hrms.project.entity.Allocation;
import com.acme.hrms.project.entity.Client;
import com.acme.hrms.project.entity.Project;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProjectMapper {

    ClientResponse toResponse(Client entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    Client toEntity(ClientCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    void apply(ClientUpdateRequest request, @MappingTarget Client entity);

    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientName", source = "client.name")
    @Mapping(target = "projectManagerId", source = "projectManager.id")
    @Mapping(target = "projectManagerName", source = "projectManager", qualifiedByName = "fullName")
    ProjectResponse toResponse(Project entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "projectManager", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    Project toEntity(ProjectCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "projectCode", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "projectManager", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    void apply(ProjectUpdateRequest request, @MappingTarget Project entity);

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectCode", source = "project.projectCode")
    @Mapping(target = "projectName", source = "project.name")
    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", source = "employee", qualifiedByName = "fullName")
    AllocationResponse toResponse(Allocation entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    Allocation toEntity(AllocationCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    void apply(AllocationUpdateRequest request, @MappingTarget Allocation entity);

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
}
