package com.acme.hrms.recruitment.entity;

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
@Table(name = "candidate_application")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateApplication extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_opening_id", nullable = false)
    private JobOpening jobOpening;

    @Column(name = "current_stage", nullable = false)
    private String currentStage; // APPLIED, SCREENING, TECHNICAL_INTERVIEW, HR_ROUND, OFFER, HIRED, REJECTED

    @Column(name = "status", nullable = false)
    private String status; // APPLIED, ACTIVE, REJECTED, WITHDRAWN, HIRED, ARCHIVED

    @Column(name = "source")
    private String source;

    @Column(name = "cover_letter")
    private String coverLetter;
}
