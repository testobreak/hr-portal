package com.acme.hrms.document.dto;

import java.net.URL;
import java.util.UUID;

public record PresignUploadResponse(
        UUID documentId,
        String uploadUrl,
        String httpMethod,
        String storageKey
) {
    public static PresignUploadResponse of(UUID documentId, URL uploadUrl, String storageKey) {
        return new PresignUploadResponse(documentId, uploadUrl.toString(), "PUT", storageKey);
    }
}
