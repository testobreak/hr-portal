package com.acme.hrms.employee;

import static com.acme.hrms.support.JwtTestSupport.asUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.acme.hrms.AbstractIntegrationTest;
import com.acme.hrms.common.security.Roles;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DepartmentControllerTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private static final UUID HR_SUBJECT  = UUID.fromString("11111111-1111-7111-8111-111111111111");
    private static final UUID EMP_SUBJECT = UUID.fromString("22222222-2222-7222-8222-222222222222");

    @Test
    void anonymousCannotList() throws Exception {
        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.parseMediaType("application/problem+json")));
    }

    @Test
    void anyAuthenticatedUserCanList() throws Exception {
        mockMvc.perform(get("/api/departments")
                        .with(asUser(EMP_SUBJECT, "emp@acme.local", Roles.EMPLOYEE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void qFiltersDepartmentsByCodeOrName() throws Exception {
        mockMvc.perform(post("/api/departments")
                        .with(asUser(HR_SUBJECT, "hr@acme.local", Roles.HR_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"code\": \"QENG\", \"name\": \"Query Engineering\" }"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/departments")
                        .with(asUser(HR_SUBJECT, "hr@acme.local", Roles.HR_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"code\": \"FIN\", \"name\": \"Finance\" }"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/departments")
                        .param("q", "query engineering")
                        .with(asUser(EMP_SUBJECT, "emp@acme.local", Roles.EMPLOYEE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].code").value("QENG"));
    }

    @Test
    void hrAdminCanCreateDepartment() throws Exception {
        String body = """
                { "code": "ENG", "name": "Engineering", "description": "Builders" }
                """;
        mockMvc.perform(post("/api/departments")
                        .with(asUser(HR_SUBJECT, "hr@acme.local", Roles.HR_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("ENG"))
                .andExpect(jsonPath("$.name").value("Engineering"))
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.version").value(0));
    }

    @Test
    void duplicateCodeYieldsConflict() throws Exception {
        String body = """
                { "code": "OPS", "name": "Operations" }
                """;
        mockMvc.perform(post("/api/departments")
                        .with(asUser(HR_SUBJECT, "hr@acme.local", Roles.HR_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/departments")
                        .with(asUser(HR_SUBJECT, "hr@acme.local", Roles.HR_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.parseMediaType("application/problem+json")))
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    void employeeCannotCreateDepartment() throws Exception {
        String body = """
                { "code": "FIN", "name": "Finance" }
                """;
        mockMvc.perform(post("/api/departments")
                        .with(asUser(EMP_SUBJECT, "emp@acme.local", Roles.EMPLOYEE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void hrCanUpdateAndOptimisticLockRejectsStaleVersion() throws Exception {
        String created = mockMvc.perform(post("/api/departments")
                        .with(asUser(HR_SUBJECT, "hr@acme.local", Roles.HR_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"code\": \"MKT\", \"name\": \"Marketing\" }"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode node = objectMapper.readTree(created);
        UUID id = UUID.fromString(node.get("id").asText());
        long version = node.get("version").asLong();

        mockMvc.perform(put("/api/departments/" + id)
                        .with(asUser(HR_SUBJECT, "hr@acme.local", Roles.HR_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"Brand & Marketing\", \"version\": " + version + " }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Brand & Marketing"))
                .andExpect(jsonPath("$.version").value(version + 1));

        // Reusing the old version should now lose the optimistic lock race.
        mockMvc.perform(put("/api/departments/" + id)
                        .with(asUser(HR_SUBJECT, "hr@acme.local", Roles.HR_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"Marketing\", \"version\": " + version + " }"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    void softDeleteByHrFreesUpTheCodeForReuse() throws Exception {
        // Create, soft-delete, re-create with same code: should succeed because
        // the partial unique index excludes deleted rows.
        String created = mockMvc.perform(post("/api/departments")
                        .with(asUser(HR_SUBJECT, "hr@acme.local", Roles.HR_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"code\": \"LEGAL\", \"name\": \"Legal\" }"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID id = UUID.fromString(objectMapper.readTree(created).get("id").asText());

        mockMvc.perform(delete("/api/departments/" + id)
                        .with(asUser(HR_SUBJECT, "hr@acme.local", Roles.HR_ADMIN)))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/departments")
                        .with(asUser(HR_SUBJECT, "hr@acme.local", Roles.HR_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"code\": \"LEGAL\", \"name\": \"Legal v2\" }"))
                .andExpect(status().isCreated());
    }
}
