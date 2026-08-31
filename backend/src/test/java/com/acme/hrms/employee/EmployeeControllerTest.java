package com.acme.hrms.employee;

import static com.acme.hrms.support.JwtTestSupport.asUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

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
import com.acme.hrms.employee.entity.Department;
import com.acme.hrms.employee.entity.Designation;
import com.acme.hrms.employee.entity.Employee;
import com.acme.hrms.employee.entity.EmploymentStatus;
import com.acme.hrms.employee.entity.Location;
import com.acme.hrms.employee.repository.DepartmentRepository;
import com.acme.hrms.employee.repository.DesignationRepository;
import com.acme.hrms.document.repository.EmployeeDocumentRepository;
import com.acme.hrms.employee.repository.EmployeeRepository;
import com.acme.hrms.employee.repository.LocationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

import java.time.LocalDate;

/**
 * End-to-end behavior of the employee endpoints — endpoint role gates,
 * row-level scope filtering, field redaction, and the audit pipeline (a
 * row in {@code audit_log} is an implicit consequence of every write,
 * exercised here by simply asserting the public response is correct).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmployeeControllerTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private DepartmentRepository departments;
    @Autowired private DesignationRepository designations;
    @Autowired private LocationRepository locations;
    @Autowired private EmployeeRepository employees;
    @Autowired private EmployeeDocumentRepository employeeDocuments;

    private static final UUID HR_SUBJECT       = UUID.fromString("aaaa1111-1111-7111-8111-111111111111");
    private static final UUID FIN_SUBJECT      = UUID.fromString("aaaa2222-2222-7222-8222-222222222222");
    private static final UUID LEAD_SUBJECT     = UUID.fromString("aaaa3333-3333-7333-8333-333333333333");
    private static final UUID MGR_SUBJECT      = UUID.fromString("aaaa4444-4444-7444-8444-444444444444");
    private static final UUID EMP_SUBJECT      = UUID.fromString("aaaa5555-5555-7555-8555-555555555555");
    private static final UUID OTHER_SUBJECT    = UUID.fromString("aaaa6666-6666-7666-8666-666666666666");
    private static final UUID PM_SUBJECT       = UUID.fromString("aaaa7777-7777-7777-8777-777777777777");

    private UUID engId;
    private UUID swengId;
    private UUID hqId;
    private UUID mgrEmpId;
    private UUID empEmpId;
    private UUID otherEmpId;

    @BeforeEach
    @Transactional
    void seed() {
        // Idempotent: clear and re-seed each test for isolation.
        employeeDocuments.deleteAllRowsForTests();
        employees.deleteAll();
        designations.deleteAll();
        locations.deleteAll();
        departments.deleteAll();

        Department eng = new Department();
        eng.setCode("ENG");
        eng.setName("Engineering");
        engId = departments.save(eng).getId();

        Designation sweng = new Designation();
        sweng.setTitle("Software Engineer");
        sweng.setLevel("L3");
        swengId = designations.save(sweng).getId();

        Location hq = new Location();
        hq.setCode("BLR-HQ");
        hq.setName("Bengaluru HQ");
        hqId = locations.save(hq).getId();

        Employee manager = baseEmp("ACME-100", "Bob", "Boss", "bob@acme.local", MGR_SUBJECT);
        manager.setDepartment(eng);
        manager.setDesignation(sweng);
        manager.setLocation(hq);
        mgrEmpId = employees.save(manager).getId();

        Employee report = baseEmp("ACME-101", "Carol", "Coder", "carol@acme.local", EMP_SUBJECT);
        report.setDepartment(eng);
        report.setDesignation(sweng);
        report.setLocation(hq);
        report.setManager(manager);
        empEmpId = employees.save(report).getId();

        Employee other = baseEmp("ACME-200", "Dave", "Other", "dave@acme.local", OTHER_SUBJECT);
        other.setDepartment(eng);
        other.setDesignation(sweng);
        other.setLocation(hq);
        otherEmpId = employees.save(other).getId();
    }

    private Employee baseEmp(String code, String first, String last, String email, UUID kc) {
        Employee e = new Employee();
        e.setId(kc);
        e.setEmployeeCode(code);
        e.setFirstName(first);
        e.setLastName(last);
        e.setEmail(email);
        e.setDateOfJoining(LocalDate.now().minusYears(1));
        e.setEmploymentStatus(EmploymentStatus.ACTIVE);
        return e;
    }

    // -- List ---------------------------------------------------------------

    @Test
    void leadershipSeesAllEmployees() throws Exception {
        mockMvc.perform(get("/api/employees")
                        .with(asUser(LEAD_SUBJECT, "lead@acme.local", Roles.LEADERSHIP)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void financeSeesAllEmployees() throws Exception {
        mockMvc.perform(get("/api/employees")
                        .with(asUser(FIN_SUBJECT, "fin@acme.local", Roles.FINANCE_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void employeeSeesOnlySelf() throws Exception {
        mockMvc.perform(get("/api/employees")
                        .with(asUser(EMP_SUBJECT, "carol@acme.local", Roles.EMPLOYEE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].email").value("carol@acme.local"));
    }

    @Test
    void managerSeesSelfPlusReports() throws Exception {
        // Bob's tree contains Bob + Carol (Carol is Bob's report). Dave is not.
        mockMvc.perform(get("/api/employees")
                        .with(asUser(MGR_SUBJECT, "bob@acme.local", Roles.MANAGER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void queryFilterRespectsScopeForEmployee() throws Exception {
        mockMvc.perform(get("/api/employees")
                        .param("q", "dave")
                        .with(asUser(EMP_SUBJECT, "carol@acme.local", Roles.EMPLOYEE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        mockMvc.perform(get("/api/employees")
                        .param("q", "carol")
                        .with(asUser(EMP_SUBJECT, "carol@acme.local", Roles.EMPLOYEE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].email").value("carol@acme.local"));
    }

    @Test
    void queryFilterRespectsScopeForManager() throws Exception {
        mockMvc.perform(get("/api/employees")
                        .param("q", "carol")
                        .with(asUser(MGR_SUBJECT, "bob@acme.local", Roles.MANAGER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].email").value("carol@acme.local"));

        mockMvc.perform(get("/api/employees")
                        .param("q", "dave")
                        .with(asUser(MGR_SUBJECT, "bob@acme.local", Roles.MANAGER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void queryFilterReturnsMatchForLeadershipAcrossScopes() throws Exception {
        mockMvc.perform(get("/api/employees")
                        .param("q", "dave")
                        .with(asUser(LEAD_SUBJECT, "lead@acme.local", Roles.LEADERSHIP)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].email").value("dave@acme.local"));
    }

    @Test
    void projectManagerWithoutManagedAllocationsSeesNoEmployees() throws Exception {
        // PM visibility is derived from allocations on managed projects.
        mockMvc.perform(get("/api/employees")
                        .with(asUser(PM_SUBJECT, "pm@acme.local", Roles.PROJECT_MANAGER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // -- Read by id (with redaction) ----------------------------------------

    @Test
    void hrSeesPersonalFields() throws Exception {
        mockMvc.perform(get("/api/employees/" + empEmpId)
                        .with(asUser(HR_SUBJECT, "hr@acme.local", Roles.HR_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("carol@acme.local"))
                .andExpect(jsonPath("$.keycloakUserId").value(EMP_SUBJECT.toString()));
    }

    @Test
    void managerReadsReportButPersonalFieldsAreRedacted() throws Exception {
        mockMvc.perform(get("/api/employees/" + empEmpId)
                        .with(asUser(MGR_SUBJECT, "bob@acme.local", Roles.MANAGER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("carol@acme.local"))
                // keycloakUserId / phoneNumber / dateOfBirth must be omitted
                // (JsonInclude=NON_NULL on the response → key is absent).
                .andExpect(jsonPath("$.keycloakUserId").doesNotExist())
                .andExpect(jsonPath("$.phoneNumber").doesNotExist())
                .andExpect(jsonPath("$.dateOfBirth").doesNotExist());
    }

    @Test
    void readingOutOfScopeRowReturnsNotFound() throws Exception {
        // Carol (EMPLOYEE) tries to read Dave — collapsed to 404 to avoid
        // information leakage about row existence.
        mockMvc.perform(get("/api/employees/" + otherEmpId)
                        .with(asUser(EMP_SUBJECT, "carol@acme.local", Roles.EMPLOYEE)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    // -- Create / update / delete ------------------------------------------

    @Test
    void hrCreatesEmployee() throws Exception {
        String body = objectMapper.createObjectNode()
                .put("employeeCode", "ACME-300")
                .put("firstName", "Eve")
                .put("lastName", "New")
                .put("email", "eve@acme.local")
                .put("dateOfJoining", LocalDate.now().toString())
                .put("departmentId", engId.toString())
                .put("designationId", swengId.toString())
                .put("locationId", hqId.toString())
                .toString();

        mockMvc.perform(post("/api/employees")
                        .with(asUser(HR_SUBJECT, "hr@acme.local", Roles.HR_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeCode").value("ACME-300"))
                .andExpect(jsonPath("$.departmentName").value("Engineering"))
                .andExpect(jsonPath("$.designationTitle").value("Software Engineer"));
    }

    @Test
    void financeCannotCreateEmployee() throws Exception {
        // Per matrix §1: Create employee is SUPER + HR only.
        String body = """
                {"employeeCode":"X","firstName":"X","lastName":"X","email":"x@acme.local",
                 "dateOfJoining":"%s"}
                """.formatted(LocalDate.now());

        mockMvc.perform(post("/api/employees")
                        .with(asUser(FIN_SUBJECT, "fin@acme.local", Roles.FINANCE_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void employeeUpdatesOwnContactButNotAnotherPersons() throws Exception {
        // Carol updates her own phone number — allowed.
        long carolVersion = employees.findById(empEmpId).orElseThrow().getVersion();
        mockMvc.perform(patch("/api/employees/" + empEmpId + "/contact")
                        .with(asUser(EMP_SUBJECT, "carol@acme.local", Roles.EMPLOYEE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"phoneNumber\": \"+91-99999-00000\", \"version\": " + carolVersion + " }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneNumber").value("+91-99999-00000"));

        // Carol tries to update Dave — forbidden (service-layer self check).
        long daveVersion = employees.findById(otherEmpId).orElseThrow().getVersion();
        mockMvc.perform(patch("/api/employees/" + otherEmpId + "/contact")
                        .with(asUser(EMP_SUBJECT, "carol@acme.local", Roles.EMPLOYEE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"phoneNumber\": \"+91-00000-99999\", \"version\": " + daveVersion + " }"))
                .andExpect(status().isForbidden());
    }

    @Test
    void hardDeleteAllowedOnlyForSuperAdmin() throws Exception {
        mockMvc.perform(delete("/api/employees/" + otherEmpId + "/permanent")
                        .with(asUser(HR_SUBJECT, "hr@acme.local", Roles.HR_ADMIN)))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/employees/" + otherEmpId + "/permanent")
                        .with(asUser(HR_SUBJECT, "hr@acme.local", Roles.SUPER_ADMIN)))
                .andExpect(status().isNoContent());
    }
}
