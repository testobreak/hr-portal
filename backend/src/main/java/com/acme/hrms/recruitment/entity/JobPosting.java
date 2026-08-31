package com.acme.hrms.recruitment.entity;

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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "job_posting")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPosting extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_opening_id", nullable = false)
    private JobOpening jobOpening;

    @Column(name = "public_id", nullable = false, updatable = false)
    private UUID publicId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "location_name")
    private String locationName;

    @Column(name = "work_arrangement", nullable = false)
    private String workArrangement; // REMOTE, ONSITE, HYBRID

    @Column(name = "employment_type", nullable = false)
    private String employmentType;

    @Column(name = "application_deadline")
    private LocalDate applicationDeadline;

    @Column(name = "status", nullable = false)
    private String status; // DRAFT, PUBLISHED, UNPUBLISHED

    @jakarta.persistence.PrePersist
    protected void populatePublicId() {
        if (this.publicId == null) {
            this.publicId = java.util.UUID.randomUUID();
        }
        super.populateTenantId();
    }
}
