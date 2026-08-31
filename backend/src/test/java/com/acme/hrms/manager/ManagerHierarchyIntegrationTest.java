package com.acme.hrms.manager;

import com.acme.hrms.AbstractIntegrationTest;
import com.acme.hrms.common.security.Roles;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.acme.hrms.support.JwtTestSupport.asTenantUser;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class ManagerHierarchyIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final UUID HR_SUBJECT = UUID.randomUUID();
    private static final UUID CEO_SUBJECT = UUID.randomUUID();
    private static final UUID MGR_SUBJECT = UUID.randomUUID();
    private static final UUID EMP_SUBJECT = UUID.randomUUID();

    private UUID hrId;
    private UUID ceoId;
    private UUID managerId;
    private UUID employeeId;

    @BeforeEach
    public void setup() throws Exception {
        // Create HR
        MvcResult hrRes = mockMvc.perform(post("/api/employees")
                        .with(asTenantUser(HR_SUBJECT, "hr@acme.local", TENANT_ID, Roles.HR_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeCode\":\"HR001\",\"firstName\":\"HR\",\"lastName\":\"Admin\",\"email\":\"hr@acme.local\",\"dateOfJoining\":\"2020-01-01\",\"keycloakUserId\":\"" + HR_SUBJECT + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        hrId = UUID.fromString(objectMapper.readTree(hrRes.getResponse().getContentAsString()).get("id").asText());

        // Create CEO (Top of tree)
        MvcResult ceoRes = mockMvc.perform(post("/api/employees")
                        .with(asTenantUser(HR_SUBJECT, "hr@acme.local", TENANT_ID, Roles.HR_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeCode\":\"CEO001\",\"firstName\":\"CEO\",\"lastName\":\"Top\",\"email\":\"ceo@acme.local\",\"dateOfJoining\":\"2020-01-01\",\"keycloakUserId\":\"" + CEO_SUBJECT + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        ceoId = UUID.fromString(objectMapper.readTree(ceoRes.getResponse().getContentAsString()).get("id").asText());

        // Create Manager (reports to CEO)
        MvcResult mgrRes = mockMvc.perform(post("/api/employees")
                        .with(asTenantUser(HR_SUBJECT, "hr@acme.local", TENANT_ID, Roles.HR_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeCode\":\"MGR001\",\"firstName\":\"Mgr\",\"lastName\":\"Middle\",\"email\":\"mgr@acme.local\",\"dateOfJoining\":\"2021-01-01\",\"managerId\":\"" + ceoId + "\",\"keycloakUserId\":\"" + MGR_SUBJECT + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        managerId = UUID.fromString(objectMapper.readTree(mgrRes.getResponse().getContentAsString()).get("id").asText());

        // Create Employee (reports to Manager)
        MvcResult empRes = mockMvc.perform(post("/api/employees")
                        .with(asTenantUser(HR_SUBJECT, "hr@acme.local", TENANT_ID, Roles.HR_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeCode\":\"EMP001\",\"firstName\":\"Emp\",\"lastName\":\"Sub\",\"email\":\"emp@acme.local\",\"dateOfJoining\":\"2022-01-01\",\"managerId\":\"" + managerId + "\",\"keycloakUserId\":\"" + EMP_SUBJECT + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        employeeId = UUID.fromString(objectMapper.readTree(empRes.getResponse().getContentAsString()).get("id").asText());
    }

    @Test
    public void testHierarchyPopulationsAndDelegations() throws Exception {
        // 1. Verify active hierarchy paths in database projection
        List<Map<String, Object>> ceoReports = jdbcTemplate.queryForList(
                "SELECT subordinate_id, depth FROM manager_hierarchy_projection WHERE manager_id = ? AND effective_to IS NULL ORDER BY depth ASC", ceoId);
        // CEO has 2 reports (managerId at depth 1, employeeId at depth 2)
        assertEquals(2, ceoReports.size());
        assertEquals(managerId.toString(), ceoReports.get(0).get("subordinate_id").toString());
        assertEquals(1, ceoReports.get(0).get("depth"));
        assertEquals(employeeId.toString(), ceoReports.get(1).get("subordinate_id").toString());
        assertEquals(2, ceoReports.get(1).get("depth"));

        // 2. Query team via Manager team APIs as CEO
        mockMvc.perform(get("/api/v1/me/team?scope=direct")
                        .with(asTenantUser(CEO_SUBJECT, "ceo@acme.local", TENANT_ID, Roles.MANAGER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].firstName").value("Mgr"));

        mockMvc.perform(get("/api/v1/me/team?scope=all")
                        .with(asTenantUser(CEO_SUBJECT, "ceo@acme.local", TENANT_ID, Roles.MANAGER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));

        // 3. Delegate Manager approval to CEO (or delegate)
        MvcResult delRes = mockMvc.perform(post("/api/v1/me/delegations")
                        .with(asTenantUser(MGR_SUBJECT, "mgr@acme.local", TENANT_ID, Roles.MANAGER))
                        .param("delegateId", ceoId.toString())
                        .param("startDate", LocalDate.now().toString())
                        .param("endDate", LocalDate.now().plusDays(5).toString()))
                .andExpect(status().isCreated())
                .andReturn();
        UUID delegationId = UUID.fromString(objectMapper.readTree(delRes.getResponse().getContentAsString()).get("id").asText());

        // Verify delegation saved in database
        String status = jdbcTemplate.queryForObject("SELECT status FROM manager_delegation WHERE id = ?", String.class, delegationId);
        assertEquals("ACTIVE", status);

        // Revoke delegation
        mockMvc.perform(delete("/api/v1/me/delegations/" + delegationId)
                        .with(asTenantUser(MGR_SUBJECT, "mgr@acme.local", TENANT_ID, Roles.MANAGER)))
                .andExpect(status().isNoContent());

        status = jdbcTemplate.queryForObject("SELECT status FROM manager_delegation WHERE id = ?", String.class, delegationId);
        assertEquals("REVOKED", status);
    }
}
