package com.acme.hrms.employee.entity;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLRestriction;

import com.acme.hrms.common.persistence.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Employee master record.
 *
 * <p>{@code keycloakUserId} is the join key to the JWT subject when the
 * employee logs in. It is nullable because HR may onboard an employee
 * (record on file, contract sent) before Keycloak provisioning happens.
 *
 * <p>Manager relationship is a self-reference. Trees can be walked via the
 * {@code findDescendantIds} recursive CTE in {@code EmployeeRepository}.
 *
 * <p>Note: relationships are mapped lazy and we expose both the FK column
 * (for filtering / scope predicates) <em>and</em> the lazy entity (for
 * fetch joins where convenient). To keep this simple, we use only the FK
 * id columns for now and resolve referenced names via mapper lookups.
 */
@Entity
@Table(name = "employee")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee extends BaseEntity {

    @Column(name = "employee_code", nullable = false, columnDefinition = "citext")
    private String employeeCode;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false, columnDefinition = "citext")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "date_of_joining", nullable = false)
    private LocalDate dateOfJoining;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", nullable = false, length = 32)
    private EmploymentStatus employmentStatus;

    @Column(name = "password_hash")
    private String passwordHash;

    @jakarta.persistence.ElementCollection(fetch = FetchType.EAGER)
    @jakarta.persistence.CollectionTable(name = "employee_role", joinColumns = @JoinColumn(name = "employee_id"))
    @Column(name = "role")
    @Builder.Default
    private java.util.Set<String> roles = new java.util.HashSet<>();

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Employee manager;

    @Column(name = "preferred_name")
    private String preferredName;

    @Column(name = "bank_account_number")
    private String bankAccountNumber;

    @Column(name = "tax_id")
    private String taxId;
}
