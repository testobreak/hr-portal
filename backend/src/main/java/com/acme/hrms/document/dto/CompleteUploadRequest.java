package com.acme.hrms.document.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CompleteUploadRequest(
        @NotNull @Positive Long sizeBytes
) {
}
