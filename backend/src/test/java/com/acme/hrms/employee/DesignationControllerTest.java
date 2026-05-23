package com.acme.hrms.employee;

import static com.acme.hrms.support.JwtTestSupport.asUser;
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
class DesignationControllerTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    private static final UUID HR_SUBJECT  = UUID.fromString("31111111-1111-7111-8111-111111111111");
    private static final UUID EMP_SUBJECT = UUID.fromString("32222222-2222-7222-8222-222222222222");

    @Test
    void qFiltersDesignationsByTitle() throws Exception {
        mockMvc.perform(post("/api/designations")
                        .with(asUser(HR_SUBJECT, "hr@acme.local", Roles.HR_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"title\": \"Software Engineer\", \"level\": \"L3\" }"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/designations")
                        .with(asUser(HR_SUBJECT, "hr@acme.local", Roles.HR_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"title\": \"Finance Analyst\", \"level\": \"L2\" }"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/designations")
                        .param("q", "software")
                        .with(asUser(EMP_SUBJECT, "emp@acme.local", Roles.EMPLOYEE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Software Engineer"));
    }
}
