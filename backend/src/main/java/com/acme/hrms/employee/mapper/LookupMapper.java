package com.acme.hrms.employee.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.acme.hrms.employee.dto.DepartmentCreateRequest;
import com.acme.hrms.employee.dto.DepartmentResponse;
import com.acme.hrms.employee.dto.DepartmentUpdateRequest;
import com.acme.hrms.employee.dto.DesignationCreateRequest;
import com.acme.hrms.employee.dto.DesignationResponse;
import com.acme.hrms.employee.dto.DesignationUpdateRequest;
import com.acme.hrms.employee.dto.LocationCreateRequest;
import com.acme.hrms.employee.dto.LocationResponse;
import com.acme.hrms.employee.dto.LocationUpdateRequest;
import com.acme.hrms.employee.dto.LegalEntityCreateRequest;
import com.acme.hrms.employee.dto.LegalEntityResponse;
import com.acme.hrms.employee.dto.LegalEntityUpdateRequest;
import com.acme.hrms.employee.entity.Department;
import com.acme.hrms.employee.entity.Designation;
import com.acme.hrms.employee.entity.Location;
import com.acme.hrms.employee.entity.LegalEntity;

@Mapper(
        componentModel = "spring",
        // Force the standard no-arg constructor + setter strategy. Without
        // this MapStruct picks up Lombok's @Builder on the entity, but the
        // generated builder only knows the entity's own fields, not those
        // inherited from BaseEntity.
        builder = @Builder(disableBuilder = true),
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface LookupMapper {

    // -- Department --------------------------------------------------------

    DepartmentResponse toResponse(Department entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    Department toEntity(DepartmentCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    void apply(DepartmentUpdateRequest request, @MappingTarget Department entity);

    // -- Designation -------------------------------------------------------

    DesignationResponse toResponse(Designation entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    Designation toEntity(DesignationCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    void apply(DesignationUpdateRequest request, @MappingTarget Designation entity);

    // -- Location ----------------------------------------------------------

    LocationResponse toResponse(Location entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    Location toEntity(LocationCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    void apply(LocationUpdateRequest request, @MappingTarget Location entity);

    // -- LegalEntity -------------------------------------------------------

    LegalEntityResponse toResponse(LegalEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    LegalEntity toEntity(LegalEntityCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    void apply(LegalEntityUpdateRequest request, @MappingTarget LegalEntity entity);
}
