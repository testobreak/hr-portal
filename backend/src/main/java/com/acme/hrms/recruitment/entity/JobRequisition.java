package com.acme.hrms.recruitment.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLRestriction;

import com.acme.hrms.common.persistence.BaseEntity;
import com.acme.hrms.employee.entity.Department;
import com.acme.hrms.employee.entity.Designation;
import com.acme.hrms.employee.entity.Employee;
import com.acme.hrms.employee.entity.LegalEntity;
import com.acme.hrms.employee.entity.Location;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "job_requisition")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobRequisition extends BaseEntity {

    @Column(name = "req_number", nullable = false)
    private String reqNumber;

    @Column(name = "job_title", nullable = false)
    private String jobTitle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designation_id")
    private Designation designation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "legal_entity_id")
    private LegalEntity legalEntity;

    @Column(name = "employment_type", nullable = false)
    private String employmentType;

    @Column(name = "openings_count", nullable = false)
    private Integer openingsCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hiring_manager_id")
    private Employee hiringManager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruiter_id")
    private Employee recruiter;

    @Column(name = "target_start_date")
    private LocalDate targetStartDate;

    @Column(name = "min_salary")
    private BigDecimal minSalary;

    @Column(name = "max_salary")
    private BigDecimal maxSalary;

    @Column(name = "currency_code", length = 3)
    private String currencyCode;

    @Column(name = "required_skills")
    private String requiredSkills;

    @Column(name = "min_experience_years")
    private Integer minExperienceYears;

    @Column(name = "description")
    private String description;

    @Column(name = "justification")
    private String justification;

    @Column(name = "status", nullable = false)
    private String status; // DRAFT, PENDING_APPROVAL, APPROVED, REJECTED, OPEN, ON_HOLD, CLOSED, CANCELLED
}
