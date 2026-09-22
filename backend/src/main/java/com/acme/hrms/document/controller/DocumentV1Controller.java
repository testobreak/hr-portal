package com.acme.hrms.document.controller;

import com.acme.hrms.common.security.CurrentUser;
import com.acme.hrms.document.entity.DocumentCategory;
import com.acme.hrms.document.entity.EmployeeDocument;
import com.acme.hrms.document.UploadStatus;
import com.acme.hrms.document.repository.DocumentCategoryRepository;
import com.acme.hrms.document.repository.EmployeeDocumentRepository;
import com.acme.hrms.employee.entity.Employee;
import com.acme.hrms.employee.repository.EmployeeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class DocumentV1Controller {

    private final EmployeeDocumentRepository documentRepository;
    private final DocumentCategoryRepository categoryRepository;
    private final EmployeeRepository employeeRepository;

    public DocumentV1Controller(EmployeeDocumentRepository documentRepository,
                                DocumentCategoryRepository categoryRepository,
                                EmployeeRepository employeeRepository) {
        this.documentRepository = documentRepository;
        this.categoryRepository = categoryRepository;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping("/me/documents")
    public ResponseEntity<List<EmployeeDocument>> getMyDocuments() {
        UUID subjectUuid = CurrentUser.fromSecurityContext()
                .map(CurrentUser::subjectUuid)
                .orElse(null);

        if (subjectUuid == null) {
            return ResponseEntity.ok(List.of());
        }

        Employee employee = employeeRepository.findById(subjectUuid).orElse(null);
        if (employee == null) {
            return ResponseEntity.ok(List.of());
        }
        UUID myId = employee.getId();

        initDefaults(employee.getTenantId());

        List<EmployeeDocument> docs = documentRepository.findAll().stream()
                .filter(d -> myId.equals(d.getEmployee().getId()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(docs);
    }

    @PostMapping("/me/documents")
    public ResponseEntity<EmployeeDocument> uploadDocument(
            @RequestParam(name = "classification") String classification,
            @RequestParam(name = "originalFilename") String originalFilename,
            @RequestParam(name = "contentType") String contentType,
            @RequestParam(name = "storageKey") String storageKey,
            @RequestParam(name = "sizeBytes") Long sizeBytes) {

        UUID subjectUuid = CurrentUser.fromSecurityContext()
                .map(CurrentUser::subjectUuid)
                .orElseThrow(() -> new IllegalStateException("User not found in security context"));

        Employee employee = employeeRepository.findById(subjectUuid)
                .orElseThrow(() -> com.acme.hrms.common.error.NotFoundException.of("Employee", subjectUuid));

        EmployeeDocument doc = EmployeeDocument.builder()
                .employee(employee)
                .restricted(false)
                .sharable(false)
                .storageKey(storageKey)
                .originalFilename(originalFilename)
                .contentType(contentType)
                .sizeBytes(sizeBytes)
                .uploadStatus(UploadStatus.UPLOADED)
                .classification(classification)
                .verificationStatus("PENDING")
                .build();
        doc.setTenantId(employee.getTenantId());

        EmployeeDocument saved = documentRepository.save(doc);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/documents/{documentId}/verify")
    public ResponseEntity<Void> verifyDocument(@PathVariable UUID documentId) {
        EmployeeDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> com.acme.hrms.common.error.NotFoundException.of("EmployeeDocument", documentId));

        UUID verifierId = CurrentUser.fromSecurityContext()
                .map(CurrentUser::subjectUuid)
                .orElse(UUID.fromString("88888888-8888-8888-8888-888888888888"));

        doc.setVerificationStatus("VERIFIED");
        doc.setVerifiedBy(verifierId);
        doc.setVerifiedAt(Instant.now());
        documentRepository.save(doc);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/documents/{documentId}/reject")
    public ResponseEntity<Void> rejectDocument(
            @PathVariable UUID documentId,
            @RequestParam(name = "reason") String reason) {
        EmployeeDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> com.acme.hrms.common.error.NotFoundException.of("EmployeeDocument", documentId));

        UUID verifierId = CurrentUser.fromSecurityContext()
                .map(CurrentUser::subjectUuid)
                .orElse(UUID.fromString("88888888-8888-8888-8888-888888888888"));

        doc.setVerificationStatus("REJECTED");
        doc.setVerifiedBy(verifierId);
        doc.setVerifiedAt(Instant.now());
        doc.setRejectionReason(reason);
        documentRepository.save(doc);

        return ResponseEntity.ok().build();
    }

    private void initDefaults(UUID tenantId) {
        List<DocumentCategory> cats = categoryRepository.findAll().stream()
                .filter(c -> tenantId.equals(c.getTenantId()))
                .collect(Collectors.toList());

        if (cats.isEmpty()) {
            String[][] defaults = {
                    {"ID_PROOF", "Identity Proof", "true", "true", "true", "5"},
                    {"OFFER_LETTER", "Offer Letter", "true", "false", "true", "10"},
                    {"ADDRESS_PROOF", "Address Proof", "true", "true", "true", "5"},
                    {"OTHER", "Other Documents", "true", "true", "true", "3"}
            };

            for (String[] def : defaults) {
                DocumentCategory cat = DocumentCategory.builder()
                        .code(def[0])
                        .name(def[1])
                        .employeeVisible(Boolean.parseBoolean(def[2]))
                        .employeeEditable(Boolean.parseBoolean(def[3]))
                        .managerVisible(Boolean.parseBoolean(def[4]))
                        .retentionYears(Integer.parseInt(def[5]))
                        .build();
                cat.setTenantId(tenantId);
                categoryRepository.save(cat);
            }
        }
    }
}
