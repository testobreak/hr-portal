package com.acme.hrms.document.dto;

import java.time.Instant;
import java.util.UUID;

import com.acme.hrms.document.DocumentType;
import com.acme.hrms.document.UploadStatus;

public record DocumentResponse(
        UUID id,
        UUID employeeId,
        DocumentType documentType,
        boolean restricted,
        boolean sharable,
        String originalFilename,
        String contentType,
        Long sizeBytes,
        UploadStatus uploadStatus,
        Instant createdAt
) {
}
