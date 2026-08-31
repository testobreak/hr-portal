package com.acme.hrms.profile;

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
public class ProfileManagementIntegrationTest extends AbstractIntegrationTest {

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

        // Update employee bank details directly via SQL to test masking
        jdbcTemplate.update("UPDATE employee SET bank_account_number = '1234567890', tax_id = 'TAX-999-99' WHERE id = ?", employeeId);
    }

    @Test
    public void testProfileDirectUpdateAndChangeRequestFlow() throws Exception {
        // 1. Get Profile and assert masking works
        mockMvc.perform(get("/api/v1/me/profile")
                        .with(asTenantUser(EMPLOYEE_SUBJECT, "john@acme.local", TENANT_ID, Roles.EMPLOYEE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.personal.firstName").value("John"))
                .andExpect(jsonPath("$.personal.bankAccountNumber").value("******7890"))
                .andExpect(jsonPath("$.personal.taxId").value("******9-99"));

        // 2. Direct update preferred name (non-sensitive field)
        mockMvc.perform(patch("/api/v1/me/profile")
                        .with(asTenantUser(EMPLOYEE_SUBJECT, "john@acme.local", TENANT_ID, Roles.EMPLOYEE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("personal.preferredName", "Johnny"))))
                .andExpect(status().isNoContent());

        // Verify direct update in database
        String preferredName = jdbcTemplate.queryForObject("SELECT preferred_name FROM employee WHERE id = ?", String.class, employeeId);
        assertEquals("Johnny", preferredName);

        // 3. Attempt direct update on bank account (sensitive field) - should fail/throw ConflictException
        mockMvc.perform(patch("/api/v1/me/profile")
                        .with(asTenantUser(EMPLOYEE_SUBJECT, "john@acme.local", TENANT_ID, Roles.EMPLOYEE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("personal.bankAccountNumber", "0987654321"))))
                .andExpect(status().isConflict());

        // 4. Submit change request for bank account number
        MvcResult reqRes = mockMvc.perform(post("/api/v1/me/profile-change-requests")
                        .with(asTenantUser(EMPLOYEE_SUBJECT, "john@acme.local", TENANT_ID, Roles.EMPLOYEE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("personal.bankAccountNumber", "0987654321"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();
        UUID changeRequestId = UUID.fromString(objectMapper.readTree(reqRes.getResponse().getContentAsString()).get("id").asText());

        // 5. HR approves change request
        mockMvc.perform(post("/api/v1/profile-change-requests/" + changeRequestId + "/approve")
                        .with(asTenantUser(HR_SUBJECT, "hr@acme.local", TENANT_ID, Roles.HR_ADMIN)))
                .andExpect(status().isOk());

        // Verify request approved and committed
        String status = jdbcTemplate.queryForObject("SELECT status FROM profile_change_request WHERE id = ?", String.class, changeRequestId);
        assertEquals("APPROVED", status);

        String updatedBankAc = jdbcTemplate.queryForObject("SELECT bank_account_number FROM employee WHERE id = ?", String.class, employeeId);
        assertEquals("0987654321", updatedBankAc);

        // Verify updated profile reflects new masked details
        mockMvc.perform(get("/api/v1/me/profile")
                        .with(asTenantUser(EMPLOYEE_SUBJECT, "john@acme.local", TENANT_ID, Roles.EMPLOYEE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.personal.bankAccountNumber").value("******4321"));
    }
}
