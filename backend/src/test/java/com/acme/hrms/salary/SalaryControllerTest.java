package com.acme.hrms.salary;

import static com.acme.hrms.support.JwtTestSupport.asUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.transaction.Transactional;

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
import com.acme.hrms.salary.entity.SalaryHistory;
import com.acme.hrms.salary.repository.SalaryHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SalaryControllerTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private EmployeeRepository employees;
    @Autowired private SalaryHistoryRepository salaries;
    @Autowired private AllocationRepository allocations;
    @Autowired private ProjectRepository projects;
    @Autowired private ClientRepository clients;
    @Autowired private EmployeeDocumentRepository employeeDocuments;

    private static final UUID HR_SUBJECT = UUID.fromString("cccc1111-1111-7111-8111-111111111111");
    private static final UUID FIN_SUBJECT = UUID.fromString("cccc2222-2222-7222-8222-222222222222");
    private static final UUID EMP_SUBJECT = UUID.fromString("cccc3333-3333-7333-8333-333333333333");
    private static final UUID OTHER_SUBJECT = UUID.fromString("cccc4444-4444-7444-8444-444444444444");
    private static final UUID LEAD_SUBJECT = UUID.fromString("cccc5555-5555-7555-8555-555555555555");

    private UUID employeeId;
    private UUID otherEmployeeId;

    @BeforeEach
    @Transactional
    void seed() {
        allocations.deleteAll();
        projects.deleteAll();
        clients.deleteAll();
        salaries.deleteAll();
        employeeDocuments.deleteAllRowsForTests();
        employees.deleteAll();

        Employee employee = baseEmp("ACME-SAL-1", "Erin", "Employee", "erin@acme.local", EMP_SUBJECT);
        employeeId = employees.save(employee).getId();

        Employee other = baseEmp("ACME-SAL-2", "Oli", "Other", "oli@acme.local", OTHER_SUBJECT);
        otherEmployeeId = employees.save(other).getId();

        salaries.save(SalaryHistory.builder()
                .employee(employee)
                .amount(new java.math.BigDecimal("100000.0000"))
                .currencyCode("INR")
                .effectiveFrom(LocalDate.now().minusMonths(3))
                .effectiveTo(LocalDate.now())
                .reason("Initial offer")
                .build());
    }

    @Test
    void employeeCanReadOwnSalaryHistory() throws Exception {
        mockMvc.perform(get("/api/salaries/me")
                        .with(asUser(EMP_SUBJECT, "erin@acme.local", Roles.EMPLOYEE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employeeId").value(employeeId.toString()))
                .andExpect(jsonPath("$[0].amount").value(100000.0000));
    }

    @Test
    void employeeCannotReadOtherEmployeesSalaryHistory() throws Exception {
        mockMvc.perform(get("/api/salaries/employee/" + otherEmployeeId)
                        .with(asUser(EMP_SUBJECT, "erin@acme.local", Roles.EMPLOYEE)))
                .andExpect(status().isForbidden());
    }

    @Test
    void hrAndFinanceCanCreateSalaryRecord() throws Exception {
        String body = objectMapper.createObjectNode()
                .put("employeeId", employeeId.toString())
                .put("amount", "120000.0000")
                .put("currencyCode", "INR")
                .put("effectiveFrom", LocalDate.now().plusDays(1).toString())
                .put("reason", "Annual revision")
                .toString();

        mockMvc.perform(post("/api/salaries")
                        .with(asUser(HR_SUBJECT, "hr@acme.local", Roles.HR_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeId").value(employeeId.toString()))
                .andExpect(jsonPath("$.amount").value(120000.0000));

        String finBody = objectMapper.createObjectNode()
                .put("employeeId", otherEmployeeId.toString())
                .put("amount", "90000.0000")
                .put("currencyCode", "INR")
                .put("effectiveFrom", LocalDate.now().plusDays(2).toString())
                .put("reason", "Market correction")
                .toString();

        mockMvc.perform(post("/api/salaries")
                        .with(asUser(FIN_SUBJECT, "fin@acme.local", Roles.FINANCE_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(finBody))
                .andExpect(status().isCreated());
    }

    @Test
    void leadershipCannotReadRawSalaryAmounts() throws Exception {
        mockMvc.perform(get("/api/salaries/employee/" + employeeId)
                        .with(asUser(LEAD_SUBJECT, "lead@acme.local", Roles.LEADERSHIP)))
                .andExpect(status().isForbidden());
    }

    private Employee baseEmp(String code, String first, String last, String email, UUID keycloakUserId) {
        Employee employee = new Employee();
        employee.setId(keycloakUserId);
        employee.setEmployeeCode(code);
        employee.setFirstName(first);
        employee.setLastName(last);
        employee.setEmail(email);
        employee.setDateOfJoining(LocalDate.now().minusYears(1));
        employee.setEmploymentStatus(EmploymentStatus.ACTIVE);
        return employee;
    }
}
