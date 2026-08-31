package com.acme.hrms.recruitment.entity;

import java.time.Instant;

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
@Table(name = "interview")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interview extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_application_id", nullable = false)
    private CandidateApplication candidateApplication;

    @Column(name = "interview_type", nullable = false)
    private String interviewType; // SCREENING, TECHNICAL, MANAGERIAL, HR

    @Column(name = "scheduled_time", nullable = false)
    private Instant scheduledTime;

    @Column(name = "status", nullable = false)
    private String status; // SCHEDULED, COMPLETED, CANCELLED, NO_SHOW
}
