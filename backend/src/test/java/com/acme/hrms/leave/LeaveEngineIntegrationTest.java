package com.acme.hrms.leave;

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

import java.util.Map;
import java.util.UUID;

import static com.acme.hrms.support.JwtTestSupport.asTenantUser;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class LeaveEngineIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final UUID TENANT_ID = UUID.randomUUID();
    private static final UUID HR_SUBJECT = UUID.randomUUID();
    private static final UUID EMPLOYEE_SUBJECT = UUID.randomUUID();

    private UUID employeeId;
    private UUID hrId;

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

        // Create Employee
        MvcResult empRes = mockMvc.perform(post("/api/employees")
                        .with(asTenantUser(HR_SUBJECT, "hr@acme.local", TENANT_ID, Roles.HR_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeCode\":\"EMP001\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john@acme.local\",\"dateOfJoining\":\"2022-01-01\",\"keycloakUserId\":\"" + EMPLOYEE_SUBJECT + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        employeeId = UUID.fromString(objectMapper.readTree(empRes.getResponse().getContentAsString()).get("id").asText());
    }

    @Test
    public void testLeaveRequestReservationAndApprovalFlow() throws Exception {
        // 1. Fetch initial balances (seeded as 10.00)
        MvcResult balancesRes = mockMvc.perform(get("/api/v1/me/leave-balances")
                        .with(asTenantUser(EMPLOYEE_SUBJECT, "john@acme.local", TENANT_ID, Roles.EMPLOYEE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.leaveTypeCode=='ANNUAL')].balance").value(10.00))
                .andReturn();

        UUID annualLeaveTypeId = UUID.fromString(objectMapper.readTree(balancesRes.getResponse().getContentAsString())
                .get(0).get("leaveTypeId").asText());

        // 2. Create a leave request draft for Mon-Wed (3 days)
        // 2026-07-20 is a Monday, 2026-07-22 is a Wednesday
        MvcResult requestRes = mockMvc.perform(post("/api/v1/me/leave-requests")
                        .with(asTenantUser(EMPLOYEE_SUBJECT, "john@acme.local", TENANT_ID, Roles.EMPLOYEE))
                        .param("leaveTypeId", annualLeaveTypeId.toString())
                        .param("startDate", "2026-07-20")
                        .param("endDate", "2026-07-22")
                        .param("reason", "Vacation"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.totalDays").value(3))
                .andReturn();
        UUID leaveRequestId = UUID.fromString(objectMapper.readTree(requestRes.getResponse().getContentAsString()).get("id").asText());

        // 3. Submit the request (reserves balance)
        mockMvc.perform(post("/api/v1/me/leave-requests/" + leaveRequestId + "/submit")
                        .with(asTenantUser(EMPLOYEE_SUBJECT, "john@acme.local", TENANT_ID, Roles.EMPLOYEE)))
                .andExpect(status().isOk());

        // Assert available balance is now 7.00 (10.00 - 3.00 reserved)
        mockMvc.perform(get("/api/v1/me/leave-balances")
                        .with(asTenantUser(EMPLOYEE_SUBJECT, "john@acme.local", TENANT_ID, Roles.EMPLOYEE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.leaveTypeCode=='ANNUAL')].balance").value(7.00));

        // 4. HR finds the pending workflow and approves it
        // Query approval request from database
        Map<String, Object> workflowRequest = jdbcTemplate.queryForMap(
                "SELECT * FROM approval_request WHERE type = 'LEAVE_REQUEST' AND status = 'PENDING'");
        UUID workflowReqId = UUID.fromString(workflowRequest.get("id").toString());

        mockMvc.perform(post("/api/approvals/requests/" + workflowReqId + "/approve")
                        .with(asTenantUser(HR_SUBJECT, "hr@acme.local", TENANT_ID, Roles.HR_ADMIN)))
                .andExpect(status().isOk());

        // Assert leave request is now APPROVED and ledger shows CONSUMPTION
        String requestStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM leave_request WHERE id = ?", String.class, leaveRequestId);
        assertEquals("APPROVED", requestStatus);

        mockMvc.perform(get("/api/v1/me/leave-balances")
                        .with(asTenantUser(EMPLOYEE_SUBJECT, "john@acme.local", TENANT_ID, Roles.EMPLOYEE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.leaveTypeCode=='ANNUAL')].balance").value(7.00));

        // 5. Test weekend exclusion: 2026-07-24 (Friday) to 2026-07-27 (Monday) should be 2 days (Fri, Mon)
        MvcResult weekendReqRes = mockMvc.perform(post("/api/v1/me/leave-requests")
                        .with(asTenantUser(EMPLOYEE_SUBJECT, "john@acme.local", TENANT_ID, Roles.EMPLOYEE))
                        .param("leaveTypeId", annualLeaveTypeId.toString())
                        .param("startDate", "2026-07-24")
                        .param("endDate", "2026-07-27"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalDays").value(2))
                .andReturn();
    }
}
