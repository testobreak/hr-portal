package com.acme.hrms.document.entity;

import com.acme.hrms.common.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "document_category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentCategory extends BaseEntity {

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "employee_visible", nullable = false)
    private boolean employeeVisible;

    @Column(name = "employee_editable", nullable = false)
    private boolean employeeEditable;

    @Column(name = "manager_visible", nullable = false)
    private boolean managerVisible;

    @Column(name = "retention_years")
    private Integer retentionYears;
}
