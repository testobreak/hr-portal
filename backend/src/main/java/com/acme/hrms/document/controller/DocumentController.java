package com.acme.hrms.document.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.acme.hrms.common.security.CurrentUser;
import com.acme.hrms.document.dto.CompleteUploadRequest;
import com.acme.hrms.document.dto.DocumentResponse;
import com.acme.hrms.document.dto.PresignDownloadResponse;
import com.acme.hrms.document.dto.PresignUploadRequest;
import com.acme.hrms.document.dto.PresignUploadResponse;
import com.acme.hrms.document.service.DocumentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/documents")
@Tag(name = "Documents")
public class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    @PostMapping("/presign-upload")
    @Operation(summary = "Create document row and return presigned PUT URL")
    public PresignUploadResponse presignUpload(@AuthenticationPrincipal Jwt jwt,
                                               @RequestBody @Valid PresignUploadRequest request) {
        return service.presignUpload(CurrentUser.from(jwt), request);
    }

    @GetMapping("/{documentId}/presign-download")
    @Operation(summary = "Presigned GET URL for an uploaded document")
    public PresignDownloadResponse presignDownload(@AuthenticationPrincipal Jwt jwt,
                                                   @PathVariable UUID documentId) {
        return service.presignDownload(CurrentUser.from(jwt), documentId);
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "List documents for an employee")
    public List<DocumentResponse> listForEmployee(@AuthenticationPrincipal Jwt jwt,
                                                    @PathVariable UUID employeeId) {
        return service.listForEmployee(CurrentUser.from(jwt), employeeId);
    }

    @PostMapping("/{documentId}/complete")
    @Operation(summary = "Mark upload finished after client PUT to storage")
    public DocumentResponse complete(@AuthenticationPrincipal Jwt jwt,
                                     @PathVariable UUID documentId,
                                     @RequestBody @Valid CompleteUploadRequest request) {
        return service.completeUpload(CurrentUser.from(jwt), documentId, request);
    }

    @DeleteMapping("/{documentId}")
    @Operation(summary = "Soft-delete a document (HR / super-admin)")
    public ResponseEntity<Void> softDelete(@AuthenticationPrincipal Jwt jwt,
                                           @PathVariable UUID documentId) {
        service.softDelete(CurrentUser.from(jwt), documentId);
        return ResponseEntity.noContent().build();
    }
}
