package com.acme.hrms.security;

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

/**
 * Spot-checks for {@code docs/rbac-matrix.md}. Expand when new endpoints ship.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RbacMatrixTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;

    private static final UUID SUPER_SUB = UUID.fromString("feed1111-1111-7111-8111-111111111111");
    private static final UUID HR_SUB = UUID.fromString("feed2222-2222-7222-8222-222222222222");
    private static final UUID FIN_SUB = UUID.fromString("feed3333-3333-7333-8333-333333333333");
    private static final UUID LEAD_SUB = UUID.fromString("feed4444-4444-7444-8444-444444444444");
    private static final UUID MGR_SUB = UUID.fromString("feed5555-5555-7555-8555-555555555555");
    private static final UUID PM_SUB = UUID.fromString("feed6666-6666-7666-8666-666666666666");
    private static final UUID EMP_SUB = UUID.fromString("feed7777-7777-7777-8777-777777777777");

    @Test
    void superAdminCanListAuditLogs() throws Exception {
        mockMvc.perform(get("/api/audit-logs")
                        .with(asUser(SUPER_SUB, "super@acme.local", Roles.SUPER_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void hrCannotListAuditLogs() throws Exception {
        mockMvc.perform(get("/api/audit-logs")
                        .with(asUser(HR_SUB, "hr@acme.local", Roles.HR_ADMIN)))
                .andExpect(status().isForbidden());
    }

    @Test
    void hrCanLoadHrDashboardOverview() throws Exception {
        mockMvc.perform(get("/api/dashboard/hr-overview")
                        .with(asUser(HR_SUB, "hr@acme.local", Roles.HR_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headcountActive").exists());
    }

    @Test
    void financeCannotLoadHrDashboardOverview() throws Exception {
        mockMvc.perform(get("/api/dashboard/hr-overview")
                        .with(asUser(FIN_SUB, "fin@acme.local", Roles.FINANCE_ADMIN)))
                .andExpect(status().isForbidden());
    }

    @Test
    void employeeCannotLoadHrDashboardOverview() throws Exception {
        mockMvc.perform(get("/api/dashboard/hr-overview")
                        .with(asUser(EMP_SUB, "e@acme.local", Roles.EMPLOYEE)))
                .andExpect(status().isForbidden());
    }

    @Test
    void managerCanLoadHrDashboardOverview() throws Exception {
        mockMvc.perform(get("/api/dashboard/hr-overview")
                        .with(asUser(MGR_SUB, "mgr@acme.local", Roles.MANAGER)))
                .andExpect(status().isOk());
    }

    @Test
    void financeCanLoadBenchDashboard() throws Exception {
        mockMvc.perform(get("/api/dashboard/bench")
                        .with(asUser(FIN_SUB, "fin@acme.local", Roles.FINANCE_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeRosterSize").exists());
    }

    @Test
    void projectManagerCanLoadBenchDashboard() throws Exception {
        mockMvc.perform(get("/api/dashboard/bench")
                        .with(asUser(PM_SUB, "pm@acme.local", Roles.PROJECT_MANAGER)))
                .andExpect(status().isOk());
    }

    @Test
    void hrCannotLoadBenchDashboard() throws Exception {
        mockMvc.perform(get("/api/dashboard/bench")
                        .with(asUser(HR_SUB, "hr@acme.local", Roles.HR_ADMIN)))
                .andExpect(status().isForbidden());
    }

    @Test
    void leadershipCanLoadBenchDashboard() throws Exception {
        mockMvc.perform(get("/api/dashboard/bench")
                        .with(asUser(LEAD_SUB, "lead@acme.local", Roles.LEADERSHIP)))
                .andExpect(status().isOk());
    }

    @Test
    void financeCannotCreateEmployee() throws Exception {
        String body = """
                {
                  "employeeCode": "RBAC-X",
                  "firstName": "X",
                  "lastName": "Y",
                  "email": "rbac-x@acme.local",
                  "dateOfJoining": "2024-01-15",
                  "employmentStatus": "ACTIVE"
                }
                """;
        mockMvc.perform(post("/api/employees")
                        .with(asUser(FIN_SUB, "fin@acme.local", Roles.FINANCE_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void leadershipCannotCreateSalary() throws Exception {
        String body = """
                {
                  "employeeId": "%s",
                  "amount": "1.0000",
                  "currencyCode": "INR",
                  "effectiveFrom": "2030-01-01",
                  "reason": "rbac"
                }
                """.formatted(UUID.randomUUID());
        mockMvc.perform(post("/api/salaries")
                        .with(asUser(LEAD_SUB, "lead@acme.local", Roles.LEADERSHIP))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void anyAuthenticatedUserCanListDepartments() throws Exception {
        mockMvc.perform(get("/api/departments")
                        .with(asUser(EMP_SUB, "e@acme.local", Roles.EMPLOYEE)))
                .andExpect(status().isOk());
    }
}
