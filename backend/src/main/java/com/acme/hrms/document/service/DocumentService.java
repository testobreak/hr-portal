package com.acme.hrms.document.service;

import java.util.List;
import java.util.UUID;

import com.acme.hrms.common.security.CurrentUser;
import com.acme.hrms.document.dto.CompleteUploadRequest;
import com.acme.hrms.document.dto.DocumentResponse;
import com.acme.hrms.document.dto.PresignDownloadResponse;
import com.acme.hrms.document.dto.PresignUploadRequest;
import com.acme.hrms.document.dto.PresignUploadResponse;

public interface DocumentService {

    PresignUploadResponse presignUpload(CurrentUser user, PresignUploadRequest request);

    PresignDownloadResponse presignDownload(CurrentUser user, UUID documentId);

    List<DocumentResponse> listForEmployee(CurrentUser user, UUID employeeId);

    DocumentResponse completeUpload(CurrentUser user, UUID documentId, CompleteUploadRequest request);

    void softDelete(CurrentUser user, UUID documentId);
}
