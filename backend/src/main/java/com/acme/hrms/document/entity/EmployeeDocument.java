package com.acme.hrms.document.entity;

import com.acme.hrms.common.persistence.BaseEntity;
import com.acme.hrms.document.DocumentType;
import com.acme.hrms.document.UploadStatus;
import com.acme.hrms.employee.entity.Employee;
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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "employee_document")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 32)
    private DocumentType documentType;

    @Column(name = "restricted", nullable = false)
    private boolean restricted;

    @Column(name = "sharable", nullable = false)
    private boolean sharable;

    @Column(name = "storage_key", nullable = false)
    private String storageKey;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(name = "upload_status", nullable = false, length = 16)
    private UploadStatus uploadStatus;

    @Column(name = "classification")
    private String classification;

    @Builder.Default
    @Column(name = "verification_status", nullable = false)
    private String verificationStatus = "PENDING"; // PENDING, VERIFIED, REJECTED

    @Column(name = "expiry_date")
    private java.time.LocalDate expiryDate;

    @Column(name = "issued_date")
    private java.time.LocalDate issuedDate;

    @Column(name = "verified_by")
    private UUID verifiedBy;

    @Column(name = "verified_at")
    private java.time.Instant verifiedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;
}
