package com.acme.hrms.me;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import com.acme.hrms.AbstractIntegrationTest;
import com.acme.hrms.common.security.Roles;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MeControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void anonymousRequestIs401WithProblemJson() throws Exception {
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.parseMediaType("application/problem+json")))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(header().exists("X-Request-Id"))
                .andExpect(header().exists("WWW-Authenticate"));
    }

    @Test
    void authenticatedRequestReturnsIdentityAndRoles() throws Exception {
        UUID subject = UUID.fromString("11111111-1111-7111-8111-111111111111");

        mockMvc.perform(get("/api/me")
                        .with(jwt()
                                .jwt(j -> j
                                        .subject(subject.toString())
                                        .claim("preferred_username", "hr@acme.local")
                                        .claim("email", "hr@acme.local")
                                        .claim("roles", List.of(Roles.HR_ADMIN, Roles.EMPLOYEE)))
                                .authorities(
                                        new SimpleGrantedAuthority(Roles.AUTHORITY_PREFIX + Roles.HR_ADMIN),
                                        new SimpleGrantedAuthority(Roles.AUTHORITY_PREFIX + Roles.EMPLOYEE))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subjectUuid").value(subject.toString()))
                .andExpect(jsonPath("$.username").value("hr@acme.local"))
                .andExpect(jsonPath("$.email").value("hr@acme.local"))
                .andExpect(jsonPath("$.roles[0]").value(Roles.EMPLOYEE))
                .andExpect(jsonPath("$.roles[1]").value(Roles.HR_ADMIN))
                .andExpect(jsonPath("$.requestId").isString())
                .andExpect(header().exists("X-Request-Id"));
    }
}
