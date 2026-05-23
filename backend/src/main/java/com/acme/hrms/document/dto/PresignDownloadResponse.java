package com.acme.hrms.document.dto;

import java.net.URL;
import java.util.UUID;

public record PresignDownloadResponse(
        UUID documentId,
        String downloadUrl,
        String httpMethod
) {
    public static PresignDownloadResponse of(UUID documentId, URL downloadUrl) {
        return new PresignDownloadResponse(documentId, downloadUrl.toString(), "GET");
    }
}
