package com.acme.hrms.profile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "profile_field_definition")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(ProfileFieldDefinitionId.class)
public class ProfileFieldDefinition implements Serializable {

    @Id
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Id
    @Column(name = "field_key", nullable = false)
    private String fieldKey;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "employee_visible", nullable = false)
    private boolean employeeVisible;

    @Column(name = "employee_editable", nullable = false)
    private boolean employeeEditable;

    @Column(name = "manager_visible", nullable = false)
    private boolean managerVisible;

    @Column(name = "hr_visible", nullable = false)
    private boolean hrVisible;

    @Column(name = "approval_required", nullable = false)
    private boolean approvalRequired;

    @Column(name = "masking_policy", nullable = false)
    private String maskingPolicy;
}
