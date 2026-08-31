package com.acme.hrms.workflow;

import static com.acme.hrms.support.JwtTestSupport.asTenantUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.acme.hrms.AbstractIntegrationTest;
import com.acme.hrms.common.security.Roles;
import com.acme.hrms.employee.dto.EmployeeResponse;
import com.acme.hrms.workflow.dto.ApprovalRequestResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WorkflowSliceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final UUID TENANT_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final UUID HR_SUBJECT = UUID.fromString("88888888-8888-8888-8888-888888888888");
    private static final UUID MANAGER_SUBJECT = UUID.fromString("99999999-9999-9999-9999-999999999999");

    @Test
    void verifiesCompleteWorkflowSlice() throws Exception {
        // 1. Create a Legal Entity
        MvcResult leResult = mockMvc.perform(post("/api/legal-entities")
                        .with(asTenantUser(HR_SUBJECT, "hr@acme.local", TENANT_ID, Roles.HR_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"code\": \"ACME_CORP\", \"name\": \"Acme Corporation\" }"))
                .andExpect(status().isCreated())
                .andReturn();
        UUID legalEntityId = UUID.fromString(objectMapper.readTree(leResult.getResponse().getContentAsString()).get("id").asText());

        // 2. Create Departments
        MvcResult dept1Result = mockMvc.perform(post("/api/departments")
                        .with(asTenantUser(HR_SUBJECT, "hr@acme.local", TENANT_ID, Roles.HR_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"code\": \"ENG\", \"name\": \"Engineering\" }"))
                .andExpect(status().isCreated())
                .andReturn();
        UUID engDeptId = UUID.fromString(objectMapper.readTree(dept1Result.getResponse().getContentAsString()).get("id").asText());

        MvcResult dept2Result = mockMvc.perform(post("/api/departments")
                        .with(asTenantUser(HR_SUBJECT, "hr@acme.local", TENANT_ID, Roles.HR_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"code\": \"HR\", \"name\": \"Human Resources\" }"))
                .andExpect(status().isCreated())
                .andReturn();
        UUID hrDeptId = UUID.fromString(objectMapper.readTree(dept2Result.getResponse().getContentAsString()).get("id").asText());

        // 3. Create Designation
        MvcResult desResult = mockMvc.perform(post("/api/designations")
                        .with(asTenantUser(HR_SUBJECT, "hr@acme.local", TENANT_ID, Roles.HR_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"title\": \"Software Engineer\", \"level\": \"L3\" }"))
                .andExpect(status().isCreated())
                .andReturn();
        UUID sdeDesignationId = UUID.fromString(objectMapper.readTree(desResult.getResponse().getContentAsString()).get("id").asText());

        // 4. Create Location
        MvcResult locResult = mockMvc.perform(post("/api/locations")
                        .with(asTenantUser(HR_SUBJECT, "hr@acme.local", TENANT_ID, Roles.HR_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"code\": \"HQ\", \"name\": \"Headquarters\", \"city\": \"San Francisco\", \"country\": \"USA\" }"))
                .andExpect(status().isCreated())
                .andReturn();
        UUID hqLocationId = UUID.fromString(objectMapper.readTree(locResult.getResponse().getContentAsString()).get("id").asText());

        // 5. Create Manager Employee
        MvcResult managerResult = mockMvc.perform(post("/api/employees")
                        .with(asTenantUser(HR_SUBJECT, "hr@acme.local", TENANT_ID, Roles.HR_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.ofEntries(
                                Map.entry("employeeCode", "MGR001"),
                                Map.entry("firstName", "Manager"),
                                Map.entry("lastName", "M"),
                                Map.entry("email", "manager@acme.local"),
                                Map.entry("phoneNumber", "+15550100"),
                                Map.entry("dateOfBirth", "1980-01-01"),
                                Map.entry("dateOfJoining", "2020-01-01"),
                                Map.entry("keycloakUserId", MANAGER_SUBJECT.toString()),
                                Map.entry("departmentId", engDeptId.toString()),
                                Map.entry("designationId", sdeDesignationId.toString()),
                                Map.entry("locationId", hqLocationId.toString()),
                                Map.entry("legalEntityId", legalEntityId.toString())
                        ))))
                .andExpect(status().isCreated())
                .andReturn();
        UUID managerId = UUID.fromString(objectMapper.readTree(managerResult.getResponse().getContentAsString()).get("id").asText());

        // 6. Create Employee
        MvcResult empResult = mockMvc.perform(post("/api/employees")
                        .with(asTenantUser(HR_SUBJECT, "hr@acme.local", TENANT_ID, Roles.HR_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.ofEntries(
                                Map.entry("employeeCode", "EMP001"),
                                Map.entry("firstName", "Employee"),
                                Map.entry("lastName", "E"),
                                Map.entry("email", "emp@acme.local"),
                                Map.entry("phoneNumber", "+15550101"),
                                Map.entry("dateOfBirth", "1990-01-01"),
                                Map.entry("dateOfJoining", "2022-01-01"),
                                Map.entry("departmentId", engDeptId.toString()),
                                Map.entry("designationId", sdeDesignationId.toString()),
                                Map.entry("locationId", hqLocationId.toString()),
                                Map.entry("legalEntityId", legalEntityId.toString()),
                                Map.entry("managerId", managerId.toString())
                        ))))
                .andExpect(status().isCreated())
                .andReturn();
        UUID employeeId = UUID.fromString(objectMapper.readTree(empResult.getResponse().getContentAsString()).get("id").asText());

        // 7. Verify Initial Employee Assignment History exists (effective from dateOfJoining)
        List<Map<String, Object>> initialHistory = jdbcTemplate.queryForList(
                "SELECT * FROM employee_assignment_history WHERE employee_id = ? ORDER BY effective_from DESC", employeeId);
        assertEquals(1, initialHistory.size());
        assertEquals("2022-01-01", initialHistory.get(0).get("effective_from").toString());
        assertEquals(engDeptId.toString(), initialHistory.get(0).get("department_id").toString());

        // 8. Manager M requests a change (transfer to HR Department starting 2026-07-20)
        String changeJson = objectMapper.writeValueAsString(Map.of(
                "departmentId", hrDeptId.toString(),
                "effectiveFrom", "2026-07-20"
        ));
        MvcResult approvalReqResult = mockMvc.perform(post("/api/approvals/requests")
                        .with(asTenantUser(MANAGER_SUBJECT, "manager@acme.local", TENANT_ID, Roles.MANAGER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "employeeId", employeeId.toString(),
                                "type", "EMPLOYEE_CHANGE",
                                "changeJson", changeJson
                        ))))
                .andExpect(status().isCreated())
                .andReturn();
        UUID approvalRequestId = UUID.fromString(objectMapper.readTree(approvalReqResult.getResponse().getContentAsString()).get("id").asText());

        // 9. HR Admin approves the request
        mockMvc.perform(post("/api/approvals/requests/" + approvalRequestId + "/approve")
                        .with(asTenantUser(HR_SUBJECT, "hr@acme.local", TENANT_ID, Roles.HR_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        // 10. Verify Employee's active department has been updated
        mockMvc.perform(get("/api/employees/" + employeeId)
                        .with(asTenantUser(HR_SUBJECT, "hr@acme.local", TENANT_ID, Roles.HR_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departmentId").value(hrDeptId.toString()))
                .andExpect(jsonPath("$.departmentName").value("Human Resources"));

        // 11. Verify Assignment History timeline is correctly updated and effective-dated
        List<Map<String, Object>> historyList = jdbcTemplate.queryForList(
                "SELECT * FROM employee_assignment_history WHERE employee_id = ? ORDER BY effective_from ASC", employeeId);
        assertEquals(2, historyList.size());
        
        // Initial assignment should be closed
        assertEquals("2022-01-01", historyList.get(0).get("effective_from").toString());
        assertEquals("2026-07-19", historyList.get(0).get("effective_to").toString());
        assertEquals(engDeptId.toString(), historyList.get(0).get("department_id").toString());

        // New assignment should be active starting 2026-07-20
        assertEquals("2026-07-20", historyList.get(1).get("effective_from").toString());
        assertEquals(null, historyList.get(1).get("effective_to"));
        assertEquals(hrDeptId.toString(), historyList.get(1).get("department_id").toString());

        // 12. Verify Audit Log was generated
        List<Map<String, Object>> auditLogs = jdbcTemplate.queryForList(
                "SELECT * FROM audit_log WHERE entity = 'employee' AND entity_id = ? AND action = 'UPDATE'", employeeId);
        assertFalse(auditLogs.isEmpty());
        assertEquals(TENANT_ID.toString(), auditLogs.get(0).get("tenant_id").toString());

        // 13. Verify Transactional Outbox Event was staged
        List<Map<String, Object>> outboxEvents = jdbcTemplate.queryForList(
                "SELECT * FROM outbox_event WHERE tenant_id = ? AND event_type = 'EmployeeAssignmentChanged'", TENANT_ID);
        assertEquals(1, outboxEvents.size());
        assertNotNull(outboxEvents.get(0).get("payload"));
    }
}
