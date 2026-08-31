package com.acme.hrms.tenant;

import static com.acme.hrms.support.JwtTestSupport.asTenantUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TenantIsolationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final UUID TENANT_A = UUID.fromString("aaaa1111-1111-1111-1111-111111111111");
    private static final UUID TENANT_B = UUID.fromString("bbbb2222-2222-2222-2222-222222222222");

    private static final UUID HR_A = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID HR_B = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    @Test
    void verifiesTenantDataIsolation() throws Exception {
        // 1. Create Department in Tenant A
        mockMvc.perform(post("/api/departments")
                        .with(asTenantUser(HR_A, "hr_a@acme.local", TENANT_A, Roles.HR_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"code\": \"DEPT_A\", \"name\": \"Department A\", \"description\": \"Tenant A Dept\" }"))
                .andExpect(status().isCreated());

        // 2. Create Department in Tenant B
        mockMvc.perform(post("/api/departments")
                        .with(asTenantUser(HR_B, "hr_b@acme.local", TENANT_B, Roles.HR_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"code\": \"DEPT_B\", \"name\": \"Department B\", \"description\": \"Tenant B Dept\" }"))
                .andExpect(status().isCreated());

        // 3. Query as Tenant A Admin - should only see Department A
        mockMvc.perform(get("/api/departments")
                        .with(asTenantUser(HR_A, "hr_a@acme.local", TENANT_A, Roles.HR_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].code").value("DEPT_A"));

        // 4. Query as Tenant B Admin - should only see Department B
        mockMvc.perform(get("/api/departments")
                        .with(asTenantUser(HR_B, "hr_b@acme.local", TENANT_B, Roles.HR_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].code").value("DEPT_B"));
    }
}
