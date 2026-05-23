package com.acme.hrms.document;

import static com.acme.hrms.support.JwtTestSupport.asUser;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.acme.hrms.AbstractIntegrationTest;
import com.acme.hrms.common.security.Roles;
import com.acme.hrms.document.repository.EmployeeDocumentRepository;
import com.acme.hrms.employee.entity.Employee;
import com.acme.hrms.employee.entity.EmploymentStatus;
import com.acme.hrms.employee.repository.EmployeeRepository;
import com.acme.hrms.project.repository.AllocationRepository;
import com.acme.hrms.project.repository.ClientRepository;
import com.acme.hrms.project.repository.ProjectRepository;
import com.acme.hrms.salary.repository.SalaryHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DocumentControllerTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private EmployeeRepository employees;
    @Autowired private EmployeeDocumentRepository documents;
    @Autowired private SalaryHistoryRepository salaries;
    @Autowired private AllocationRepository allocations;
    @Autowired private ProjectRepository projects;
    @Autowired private ClientRepository clients;

    private static final UUID HR_SUBJECT = UUID.fromString("dddd1111-1111-7111-8111-111111111111");
    private static final UUID EMP_SUBJECT = UUID.fromString("dddd2222-2222-7222-8222-222222222222");
    private static final UUID OTHER_SUBJECT = UUID.fromString("dddd3333-3333-7333-8333-333333333333");
    private static final UUID FIN_SUBJECT = UUID.fromString("dddd4444-4444-7444-8444-444444444444");

    private UUID employeeId;
    private UUID otherEmployeeId;

    @BeforeEach
    void seed() {
        allocations.deleteAll();
        projects.deleteAll();
        clients.deleteAll();
        salaries.deleteAll();
        documents.deleteAllRowsForTests();
        employees.deleteAll();

        Employee emp = baseEmp("DOC-EMP-1", "Dana", "Employee", "dana@acme.local", EMP_SUBJECT);
        employeeId = employees.save(emp).getId();

        Employee other = baseEmp("DOC-EMP-2", "Owen", "Other", "owen@acme.local", OTHER_SUBJECT);
        otherEmployeeId = employees.save(other).getId();
    }

    @Test
    void anonymousCannotPresignUpload() throws Exception {
        mockMvc.perform(post("/api/documents/presign-upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.parseMediaType("application/problem+json")));
    }

    @Test
    void employeeCanPresignProfilePhotoForSelf() throws Exception {
        String body = objectMapper.createObjectNode()
                .put("employeeId", employeeId.toString())
                .put("documentType", "PROFILE_PHOTO")
                .put("contentType", "image/png")
                .put("originalFilename", "face.png")
                .put("sharable", false)
                .toString();

        mockMvc.perform(post("/api/documents/presign-upload")
                        .with(asUser(EMP_SUBJECT, "dana@acme.local", Roles.EMPLOYEE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId").isString())
                .andExpect(jsonPath("$.uploadUrl").value(containsString("storage.invalid/put/")))
                .andExpect(jsonPath("$.httpMethod").value("PUT"));
    }

    @Test
    void employeeCannotPresignRestrictedType() throws Exception {
        String body = objectMapper.createObjectNode()
                .put("employeeId", employeeId.toString())
                .put("documentType", "OFFER_LETTER")
                .put("contentType", "application/pdf")
                .put("originalFilename", "offer.pdf")
                .put("sharable", true)
                .toString();

        mockMvc.perform(post("/api/documents/presign-upload")
                        .with(asUser(EMP_SUBJECT, "dana@acme.local", Roles.EMPLOYEE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void hrCanPresignRestrictedDocument() throws Exception {
        String body = objectMapper.createObjectNode()
                .put("employeeId", employeeId.toString())
                .put("documentType", "ID_PROOF")
                .put("contentType", "application/pdf")
                .put("originalFilename", "passport.pdf")
                .put("sharable", true)
                .toString();

        mockMvc.perform(post("/api/documents/presign-upload")
                        .with(asUser(HR_SUBJECT, "hr@acme.local", Roles.HR_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storageKey").isString());
    }

    @Test
    void financeAdminCannotListDocuments() throws Exception {
        mockMvc.perform(get("/api/documents/employee/" + employeeId)
                        .with(asUser(FIN_SUBJECT, "fin@acme.local", Roles.FINANCE_ADMIN)))
                .andExpect(status().isForbidden());
    }

    @Test
    void employeeCanListOwnDocuments() throws Exception {
        mockMvc.perform(get("/api/documents/employee/" + employeeId)
                        .with(asUser(EMP_SUBJECT, "dana@acme.local", Roles.EMPLOYEE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void employeeCannotListSomeoneElsesDocuments() throws Exception {
        mockMvc.perform(get("/api/documents/employee/" + otherEmployeeId)
                        .with(asUser(EMP_SUBJECT, "dana@acme.local", Roles.EMPLOYEE)))
                .andExpect(status().isForbidden());
    }

    @Test
    void cannotDownloadBeforeUploadCompleted() throws Exception {
        String body = objectMapper.createObjectNode()
                .put("employeeId", employeeId.toString())
                .put("documentType", "PROFILE_PHOTO")
                .put("contentType", "image/png")
                .put("originalFilename", "a.png")
                .put("sharable", false)
                .toString();

        String created = mockMvc.perform(post("/api/documents/presign-upload")
                        .with(asUser(EMP_SUBJECT, "dana@acme.local", Roles.EMPLOYEE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        UUID docId = UUID.fromString(objectMapper.readTree(created).get("documentId").asText());

        mockMvc.perform(get("/api/documents/" + docId + "/presign-download")
                        .with(asUser(EMP_SUBJECT, "dana@acme.local", Roles.EMPLOYEE)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    void employeeCanDownloadOwnPublicDocumentAfterComplete() throws Exception {
        String presignBody = objectMapper.createObjectNode()
                .put("employeeId", employeeId.toString())
                .put("documentType", "PROFILE_PHOTO")
                .put("contentType", "image/png")
                .put("originalFilename", "pic.png")
                .put("sharable", false)
                .toString();

        String created = mockMvc.perform(post("/api/documents/presign-upload")
                        .with(asUser(EMP_SUBJECT, "dana@acme.local", Roles.EMPLOYEE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(presignBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        UUID docId = UUID.fromString(objectMapper.readTree(created).get("documentId").asText());

        String completeBody = objectMapper.createObjectNode().put("sizeBytes", 1024).toString();
        mockMvc.perform(post("/api/documents/" + docId + "/complete")
                        .with(asUser(EMP_SUBJECT, "dana@acme.local", Roles.EMPLOYEE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(completeBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadStatus").value("UPLOADED"));

        mockMvc.perform(get("/api/documents/" + docId + "/presign-download")
                        .with(asUser(EMP_SUBJECT, "dana@acme.local", Roles.EMPLOYEE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.downloadUrl").value(containsString("storage.invalid/get/")));
    }

    @Test
    void employeeCannotDownloadRestrictedUnlessSharable() throws Exception {
        String presignBody = objectMapper.createObjectNode()
                .put("employeeId", employeeId.toString())
                .put("documentType", "OFFER_LETTER")
                .put("contentType", "application/pdf")
                .put("originalFilename", "offer.pdf")
                .put("sharable", false)
                .toString();

        String created = mockMvc.perform(post("/api/documents/presign-upload")
                        .with(asUser(HR_SUBJECT, "hr@acme.local", Roles.HR_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(presignBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        UUID docId = UUID.fromString(objectMapper.readTree(created).get("documentId").asText());

        String completeBody = objectMapper.createObjectNode().put("sizeBytes", 2048).toString();
        mockMvc.perform(post("/api/documents/" + docId + "/complete")
                        .with(asUser(HR_SUBJECT, "hr@acme.local", Roles.HR_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(completeBody))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/documents/" + docId + "/presign-download")
                        .with(asUser(EMP_SUBJECT, "dana@acme.local", Roles.EMPLOYEE)))
                .andExpect(status().isForbidden());
    }

    @Test
    void employeeCanDownloadRestrictedWhenSharable() throws Exception {
        String presignBody = objectMapper.createObjectNode()
                .put("employeeId", employeeId.toString())
                .put("documentType", "OFFER_LETTER")
                .put("contentType", "application/pdf")
                .put("originalFilename", "offer.pdf")
                .put("sharable", true)
                .toString();

        String created = mockMvc.perform(post("/api/documents/presign-upload")
                        .with(asUser(HR_SUBJECT, "hr@acme.local", Roles.HR_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(presignBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        UUID docId = UUID.fromString(objectMapper.readTree(created).get("documentId").asText());

        mockMvc.perform(post("/api/documents/" + docId + "/complete")
                        .with(asUser(HR_SUBJECT, "hr@acme.local", Roles.HR_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode().put("sizeBytes", 99).toString()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/documents/" + docId + "/presign-download")
                        .with(asUser(EMP_SUBJECT, "dana@acme.local", Roles.EMPLOYEE)))
                .andExpect(status().isOk());
    }

    @Test
    void employeeCannotDeleteDocument() throws Exception {
        UUID docId = createCompletedProfileDoc();
        mockMvc.perform(delete("/api/documents/" + docId)
                        .with(asUser(EMP_SUBJECT, "dana@acme.local", Roles.EMPLOYEE)))
                .andExpect(status().isForbidden());
    }

    @Test
    void hrCanSoftDeleteDocument() throws Exception {
        UUID docId = createCompletedProfileDoc();
        mockMvc.perform(delete("/api/documents/" + docId)
                        .with(asUser(HR_SUBJECT, "hr@acme.local", Roles.HR_ADMIN)))
                .andExpect(status().isNoContent());
    }

    private UUID createCompletedProfileDoc() throws Exception {
        String presignBody = objectMapper.createObjectNode()
                .put("employeeId", employeeId.toString())
                .put("documentType", "PROFILE_PHOTO")
                .put("contentType", "image/png")
                .put("originalFilename", "x.png")
                .put("sharable", false)
                .toString();
        String created = mockMvc.perform(post("/api/documents/presign-upload")
                        .with(asUser(EMP_SUBJECT, "dana@acme.local", Roles.EMPLOYEE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(presignBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        UUID docId = UUID.fromString(objectMapper.readTree(created).get("documentId").asText());
        mockMvc.perform(post("/api/documents/" + docId + "/complete")
                        .with(asUser(EMP_SUBJECT, "dana@acme.local", Roles.EMPLOYEE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.createObjectNode().put("sizeBytes", 10).toString()))
                .andExpect(status().isOk());
        return docId;
    }

    private Employee baseEmp(String code, String first, String last, String email, UUID keycloakUserId) {
        Employee employee = new Employee();
        employee.setEmployeeCode(code);
        employee.setFirstName(first);
        employee.setLastName(last);
        employee.setEmail(email);
        employee.setDateOfJoining(LocalDate.now().minusYears(1));
        employee.setEmploymentStatus(EmploymentStatus.ACTIVE);
        employee.setKeycloakUserId(keycloakUserId);
        return employee;
    }
}
