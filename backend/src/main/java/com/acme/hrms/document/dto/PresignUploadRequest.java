package com.acme.hrms.document.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.acme.hrms.document.DocumentType;

public record PresignUploadRequest(
        @NotNull UUID employeeId,
        @NotNull DocumentType documentType,
        @NotBlank @Size(max = 255) String contentType,
        @NotBlank @Size(max = 512) String originalFilename,
        /** When true, employee may download their own restricted document (matrix §4). */
        boolean sharable
) {
}
