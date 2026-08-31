package com.acme.hrms.onboarding.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Entity
@Table(name = "onboarding_task")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnboardingTask extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "onboarding_plan_id", nullable = false)
    private OnboardingPlan onboardingPlan;

    @Column(name = "task_name", nullable = false)
    private String taskName;

    @Column(name = "description")
    private String description;

    @Column(name = "assigned_role", nullable = false)
    private String assignedRole; // CANDIDATE, HR, IT, MANAGER

    @Column(name = "status", nullable = false)
    private String status; // NOT_STARTED, IN_PROGRESS, COMPLETED, WAIVED

    @Column(name = "due_date")
    private LocalDate dueDate;
}
