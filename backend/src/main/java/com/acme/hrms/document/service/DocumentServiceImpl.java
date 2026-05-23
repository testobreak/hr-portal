package com.acme.hrms.document.service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acme.hrms.common.audit.AuditAction;
import com.acme.hrms.common.audit.AuditEvent;
import com.acme.hrms.common.audit.AuditService;
import com.acme.hrms.common.error.ConflictException;
import com.acme.hrms.common.error.ForbiddenAccessException;
import com.acme.hrms.common.error.NotFoundException;
import com.acme.hrms.common.security.CurrentUser;
import com.acme.hrms.common.security.Roles;
import com.acme.hrms.common.storage.StorageProperties;
import com.acme.hrms.common.storage.StorageService;
import com.acme.hrms.document.DocumentType;
import com.acme.hrms.document.UploadStatus;
import com.acme.hrms.document.dto.CompleteUploadRequest;
import com.acme.hrms.document.dto.DocumentResponse;
import com.acme.hrms.document.dto.PresignDownloadResponse;
import com.acme.hrms.document.dto.PresignUploadRequest;
import com.acme.hrms.document.dto.PresignUploadResponse;
import com.acme.hrms.document.entity.EmployeeDocument;
import com.acme.hrms.document.repository.EmployeeDocumentRepository;
import com.acme.hrms.employee.entity.Employee;
import com.acme.hrms.employee.repository.EmployeeRepository;

@Service
public class DocumentServiceImpl implements DocumentService {

    private static final String ENTITY = "employee_document";

    private final EmployeeDocumentRepository documents;
    private final EmployeeRepository employees;
    private final StorageService storage;
    private final StorageProperties storageProperties;
    private final AuditService auditService;

    public DocumentServiceImpl(EmployeeDocumentRepository documents,
                               EmployeeRepository employees,
                               StorageService storage,
                               StorageProperties storageProperties,
                               AuditService auditService) {
        this.documents = documents;
        this.employees = employees;
        this.storage = storage;
        this.storageProperties = storageProperties;
        this.auditService = auditService;
    }

    @Override
    @Transactional
    public PresignUploadResponse presignUpload(CurrentUser user, PresignUploadRequest request) {
        Employee employee = loadEmployee(request.employeeId());
        assertUploadAllowed(user, employee, request.documentType());

        boolean restricted = request.documentType().isRestricted();
        boolean sharable = restricted && request.sharable();

        String safeName = sanitizeFilename(request.originalFilename());
        String storageKey = "hrms/v1/documents/%s/%s/%s"
                .formatted(employee.getId(), UUID.randomUUID(), safeName);

        EmployeeDocument row = EmployeeDocument.builder()
                .employee(employee)
                .documentType(request.documentType())
                .restricted(restricted)
                .sharable(sharable)
                .storageKey(storageKey)
                .originalFilename(request.originalFilename().trim())
                .contentType(request.contentType().trim())
                .uploadStatus(UploadStatus.PENDING)
                .build();

        EmployeeDocument saved = documents.save(row);
        Duration ttl = Duration.ofSeconds(storageProperties.getPresignTtlSeconds());
        var url = storage.presignPut(
                storageProperties.getBucket(),
                storageKey,
                request.contentType().trim(),
                ttl);

        auditService.record(AuditEvent.of(AuditAction.CREATE, ENTITY)
                .withEntityId(saved.getId())
                .withDetail("presign upload employeeId=" + employee.getId()));

        return PresignUploadResponse.of(saved.getId(), url, storageKey);
    }

    @Override
    @Transactional(readOnly = true)
    public PresignDownloadResponse presignDownload(CurrentUser user, UUID documentId) {
        EmployeeDocument doc = loadDocument(documentId);
        if (doc.getUploadStatus() != UploadStatus.UPLOADED) {
            throw new ConflictException("Document is not available until upload is completed");
        }
        assertDownloadAllowed(user, doc);

        auditService.record(AuditEvent.of(AuditAction.READ_SENSITIVE, ENTITY)
                .withEntityId(doc.getId())
                .withDetail("presign download employeeId=" + doc.getEmployee().getId()));

        Duration ttl = Duration.ofSeconds(storageProperties.getPresignTtlSeconds());
        var url = storage.presignGet(storageProperties.getBucket(), doc.getStorageKey(), ttl);
        return PresignDownloadResponse.of(doc.getId(), url);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> listForEmployee(CurrentUser user, UUID employeeId) {
        assertListAllowed(user, employeeId);
        loadEmployee(employeeId);
        return documents.findByEmployee_IdOrderByCreatedAtDesc(employeeId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public DocumentResponse completeUpload(CurrentUser user, UUID documentId, CompleteUploadRequest request) {
        EmployeeDocument doc = loadDocument(documentId);
        assertCompleteAllowed(user, doc);
        if (doc.getUploadStatus() != UploadStatus.PENDING) {
            throw new ConflictException("Document upload is already finalized");
        }
        doc.setSizeBytes(request.sizeBytes());
        doc.setUploadStatus(UploadStatus.UPLOADED);
        EmployeeDocument saved = documents.save(doc);
        auditService.record(AuditEvent.of(AuditAction.UPDATE, ENTITY)
                .withEntityId(saved.getId())
                .withDetail("upload completed"));
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void softDelete(CurrentUser user, UUID documentId) {
        if (!user.hasAnyRole(Roles.SUPER_ADMIN, Roles.HR_ADMIN)) {
            throw new ForbiddenAccessException("Only HR or super-admin can delete documents");
        }
        EmployeeDocument doc = loadDocument(documentId);
        doc.setDeletedAt(java.time.Instant.now());
        documents.save(doc);
        auditService.record(AuditEvent.of(AuditAction.SOFT_DELETE, ENTITY)
                .withEntityId(doc.getId()));
    }

    private void assertUploadAllowed(CurrentUser user, Employee employee, DocumentType type) {
        if (type.isRestricted()) {
            if (!user.hasAnyRole(Roles.SUPER_ADMIN, Roles.HR_ADMIN)) {
                throw new ForbiddenAccessException("Restricted document types require HR");
            }
            return;
        }
        if (user.hasAnyRole(Roles.SUPER_ADMIN, Roles.HR_ADMIN)) {
            return;
        }
        if (user.hasRole(Roles.EMPLOYEE) && type == DocumentType.PROFILE_PHOTO && isSelf(user, employee)) {
            return;
        }
        throw new ForbiddenAccessException("Not allowed to upload this document type for this employee");
    }

    private void assertDownloadAllowed(CurrentUser user, EmployeeDocument doc) {
        Employee employee = doc.getEmployee();
        if (user.hasAnyRole(Roles.SUPER_ADMIN, Roles.HR_ADMIN)) {
            return;
        }
        if (!isSelf(user, employee)) {
            throw new ForbiddenAccessException("Not allowed to download this document");
        }
        if (doc.isRestricted() && !doc.isSharable()) {
            throw new ForbiddenAccessException("This document is not shared with you");
        }
    }

    private void assertListAllowed(CurrentUser user, UUID employeeId) {
        if (user.hasAnyRole(Roles.SUPER_ADMIN, Roles.HR_ADMIN)) {
            return;
        }
        if (!user.hasRole(Roles.EMPLOYEE)) {
            throw new ForbiddenAccessException("Not allowed to list documents");
        }
        Employee self = resolveSelf(user);
        if (!self.getId().equals(employeeId)) {
            throw new ForbiddenAccessException("You can only list your own documents");
        }
    }

    private void assertCompleteAllowed(CurrentUser user, EmployeeDocument doc) {
        if (user.hasAnyRole(Roles.SUPER_ADMIN, Roles.HR_ADMIN)) {
            return;
        }
        if (user.hasRole(Roles.EMPLOYEE) && isSelf(user, doc.getEmployee())) {
            return;
        }
        throw new ForbiddenAccessException("Not allowed to finalize this upload");
    }

    private boolean isSelf(CurrentUser user, Employee employee) {
        return user.subjectUuid() != null
                && employee.getKeycloakUserId() != null
                && user.subjectUuid().equals(employee.getKeycloakUserId());
    }

    private Employee resolveSelf(CurrentUser user) {
        if (user.subjectUuid() == null) {
            throw new ForbiddenAccessException("Authenticated subject is not linked");
        }
        return employees.findByKeycloakUserId(user.subjectUuid())
                .orElseThrow(() -> new ForbiddenAccessException("No employee is linked to the authenticated subject"));
    }

    private Employee loadEmployee(UUID id) {
        return employees.findById(id)
                .orElseThrow(() -> NotFoundException.of("Employee", id));
    }

    private EmployeeDocument loadDocument(UUID id) {
        return documents.findById(id)
                .orElseThrow(() -> NotFoundException.of("Document", id));
    }

    private DocumentResponse toResponse(EmployeeDocument d) {
        return new DocumentResponse(
                d.getId(),
                d.getEmployee().getId(),
                d.getDocumentType(),
                d.isRestricted(),
                d.isSharable(),
                d.getOriginalFilename(),
                d.getContentType(),
                d.getSizeBytes(),
                d.getUploadStatus(),
                d.getCreatedAt());
    }

    private static String sanitizeFilename(String name) {
        String base = name.replace('\\', '/');
        int slash = base.lastIndexOf('/');
        String leaf = slash >= 0 ? base.substring(slash + 1) : base;
        String cleaned = leaf.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (cleaned.isBlank()) {
            cleaned = "file";
        }
        return cleaned.length() > 200 ? cleaned.substring(0, 200) : cleaned;
    }
}
