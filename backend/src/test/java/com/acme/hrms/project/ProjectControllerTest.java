package com.acme.hrms.project;

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
import com.acme.hrms.project.entity.Allocation;
import com.acme.hrms.project.entity.Client;
import com.acme.hrms.project.entity.Project;
import com.acme.hrms.project.entity.ProjectStatus;
import com.acme.hrms.project.repository.AllocationRepository;
import com.acme.hrms.project.repository.ClientRepository;
import com.acme.hrms.project.repository.ProjectRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProjectControllerTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private ClientRepository clients;
    @Autowired private ProjectRepository projects;
    @Autowired private AllocationRepository allocations;
    @Autowired private EmployeeRepository employees;
    @Autowired private EmployeeDocumentRepository employeeDocuments;

    private static final UUID HR_SUBJECT    = UUID.fromString("bbbb1111-1111-7111-8111-111111111111");
    private static final UUID FIN_SUBJECT   = UUID.fromString("bbbb2222-2222-7222-8222-222222222222");
    private static final UUID LEAD_SUBJECT  = UUID.fromString("bbbb3333-3333-7333-8333-333333333333");
    private static final UUID MGR_SUBJECT   = UUID.fromString("bbbb4444-4444-7444-8444-444444444444");
    private static final UUID EMP_SUBJECT   = UUID.fromString("bbbb5555-5555-7555-8555-555555555555");
    private static final UUID OTHER_SUBJECT = UUID.fromString("bbbb6666-6666-7666-8666-666666666666");
    private static final UUID PM_SUBJECT    = UUID.fromString("bbbb7777-7777-7777-8777-777777777777");

    private UUID clientId;
    private UUID pmEmployeeId;
    private UUID managerEmployeeId;
    private UUID memberEmployeeId;
    private UUID otherEmployeeId;
    private UUID ownedProjectId;
    private UUID memberProjectId;
    private UUID otherProjectId;
    private UUID memberAllocationId;

    @BeforeEach
    @Transactional
    void seed() {
        allocations.deleteAll();
        projects.deleteAll();
        clients.deleteAll();
        employeeDocuments.deleteAllRowsForTests();
        employees.deleteAll();

        Employee pm = employee("ACME-PM", "Pat", "Manager", "pm@acme.local", PM_SUBJECT);
        pmEmployeeId = employees.save(pm).getId();

        Employee manager = employee("ACME-MGR", "Mona", "Lead", "manager@acme.local", MGR_SUBJECT);
        managerEmployeeId = employees.save(manager).getId();

        Employee member = employee("ACME-EMP", "Eli", "Member", "eli@acme.local", EMP_SUBJECT);
        member.setManager(manager);
        memberEmployeeId = employees.save(member).getId();

        Employee other = employee("ACME-OTH", "Omar", "Other", "omar@acme.local", OTHER_SUBJECT);
        otherEmployeeId = employees.save(other).getId();

        Client acme = new Client();
        acme.setCode("ACME");
        acme.setName("Acme Corp");
        clientId = clients.save(acme).getId();

        Project owned = project("PRJ-OWN", "Owned Project", acme, pm);
        ownedProjectId = projects.save(owned).getId();

        Project memberProject = project("PRJ-MEM", "Member Project", acme, other);
        memberProjectId = projects.save(memberProject).getId();

        Project unrelated = project("PRJ-OTH", "Other Project", acme, other);
        otherProjectId = projects.save(unrelated).getId();

        memberAllocationId = allocations.save(allocation(memberProject, member)).getId();
        allocations.save(allocation(unrelated, other));
    }

    @Test
    void clientReadAndManageRolesFollowMatrix() throws Exception {
        mockMvc.perform(get("/api/clients")
                        .with(asUser(HR_SUBJECT, "hr@acme.local", Roles.HR_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/api/clients")
                        .with(asUser(EMP_SUBJECT, "eli@acme.local", Roles.EMPLOYEE)))
                .andExpect(status().isForbidden());

        String body = objectMapper.createObjectNode()
                .put("code", "NEW")
                .put("name", "New Client")
                .toString();

        mockMvc.perform(post("/api/clients")
                        .with(asUser(HR_SUBJECT, "hr@acme.local", Roles.HR_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/clients")
                        .with(asUser(FIN_SUBJECT, "fin@acme.local", Roles.FINANCE_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("NEW"));
    }

    @Test
    void clientQueryFiltersWithinReadableScope() throws Exception {
        mockMvc.perform(get("/api/clients")
                        .param("q", "acme")
                        .with(asUser(FIN_SUBJECT, "fin@acme.local", Roles.FINANCE_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].code").value("ACME"));
    }

    @Test
    void projectVisibilityIsScopedByRole() throws Exception {
        mockMvc.perform(get("/api/projects")
                        .with(asUser(LEAD_SUBJECT, "lead@acme.local", Roles.LEADERSHIP)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3));

        mockMvc.perform(get("/api/projects")
                        .with(asUser(PM_SUBJECT, "pm@acme.local", Roles.PROJECT_MANAGER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/api/projects")
                        .with(asUser(EMP_SUBJECT, "eli@acme.local", Roles.EMPLOYEE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(memberProjectId.toString()));

        mockMvc.perform(get("/api/projects/" + otherProjectId)
                        .with(asUser(EMP_SUBJECT, "eli@acme.local", Roles.EMPLOYEE)))
                .andExpect(status().isNotFound());
    }

    @Test
    void projectQueryFiltersWithinScope() throws Exception {
        mockMvc.perform(get("/api/projects")
                        .param("q", "owned")
                        .with(asUser(LEAD_SUBJECT, "lead@acme.local", Roles.LEADERSHIP)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(ownedProjectId.toString()));

        mockMvc.perform(get("/api/projects")
                        .param("q", "other")
                        .with(asUser(PM_SUBJECT, "pm@acme.local", Roles.PROJECT_MANAGER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void projectManagerCreatesOnlyOwnProject() throws Exception {
        String ownBody = objectMapper.createObjectNode()
                .put("clientId", clientId.toString())
                .put("projectCode", "PRJ-NEW")
                .put("name", "New Own Project")
                .put("projectManagerId", pmEmployeeId.toString())
                .put("startDate", LocalDate.now().toString())
                .toString();

        mockMvc.perform(post("/api/projects")
                        .with(asUser(PM_SUBJECT, "pm@acme.local", Roles.PROJECT_MANAGER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ownBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projectCode").value("PRJ-NEW"));

        String otherManagerBody = objectMapper.createObjectNode()
                .put("clientId", clientId.toString())
                .put("projectCode", "PRJ-BAD")
                .put("name", "Bad Project")
                .put("projectManagerId", otherEmployeeId.toString())
                .put("startDate", LocalDate.now().toString())
                .toString();

        mockMvc.perform(post("/api/projects")
                        .with(asUser(PM_SUBJECT, "pm@acme.local", Roles.PROJECT_MANAGER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(otherManagerBody))
                .andExpect(status().isForbidden());
    }

    @Test
    void allocationVisibilityAndProjectManagerWritesAreScoped() throws Exception {
        mockMvc.perform(get("/api/allocations")
                        .with(asUser(MGR_SUBJECT, "manager@acme.local", Roles.MANAGER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(memberAllocationId.toString()));

        mockMvc.perform(get("/api/allocations")
                        .with(asUser(EMP_SUBJECT, "eli@acme.local", Roles.EMPLOYEE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        String ownAllocationBody = allocationBody(ownedProjectId, memberEmployeeId, 50);
        mockMvc.perform(post("/api/allocations")
                        .with(asUser(PM_SUBJECT, "pm@acme.local", Roles.PROJECT_MANAGER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ownAllocationBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projectId").value(ownedProjectId.toString()));

        String otherProjectBody = allocationBody(otherProjectId, memberEmployeeId, 50);
        mockMvc.perform(post("/api/allocations")
                        .with(asUser(PM_SUBJECT, "pm@acme.local", Roles.PROJECT_MANAGER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(otherProjectBody))
                .andExpect(status().isForbidden());
    }

    @Test
    void allocationQueryFiltersWithinScope() throws Exception {
        mockMvc.perform(get("/api/allocations")
                        .param("q", "member")
                        .with(asUser(EMP_SUBJECT, "eli@acme.local", Roles.EMPLOYEE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(memberAllocationId.toString()));

        mockMvc.perform(get("/api/allocations")
                        .param("q", "omar")
                        .with(asUser(EMP_SUBJECT, "eli@acme.local", Roles.EMPLOYEE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void validationAndConflictPathsReturnExpectedStatusCodes() throws Exception {
        String duplicateProject = objectMapper.createObjectNode()
                .put("clientId", clientId.toString())
                .put("projectCode", "PRJ-OWN")
                .put("name", "Duplicate")
                .put("projectManagerId", pmEmployeeId.toString())
                .put("startDate", LocalDate.now().toString())
                .toString();

        mockMvc.perform(post("/api/projects")
                        .with(asUser(FIN_SUBJECT, "fin@acme.local", Roles.FINANCE_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateProject))
                .andExpect(status().isConflict());

        String badDates = objectMapper.createObjectNode()
                .put("clientId", clientId.toString())
                .put("projectCode", "PRJ-DATE")
                .put("name", "Bad Dates")
                .put("projectManagerId", pmEmployeeId.toString())
                .put("startDate", LocalDate.now().toString())
                .put("endDate", LocalDate.now().minusDays(1).toString())
                .toString();

        mockMvc.perform(post("/api/projects")
                        .with(asUser(FIN_SUBJECT, "fin@acme.local", Roles.FINANCE_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badDates))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/allocations")
                        .with(asUser(HR_SUBJECT, "hr@acme.local", Roles.HR_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(allocationBody(ownedProjectId, memberEmployeeId, 101)))
                .andExpect(status().isBadRequest());
    }

    private Employee employee(String code, String first, String last, String email, UUID keycloakUserId) {
        Employee employee = new Employee();
        employee.setEmployeeCode(code);
        employee.setFirstName(first);
        employee.setLastName(last);
        employee.setEmail(email);
        employee.setDateOfJoining(LocalDate.now().minusYears(1));
        employee.setEmploymentStatus(EmploymentStatus.ACTIVE);
        employee.setKeycloakUserId(keycloakUserId);
        return employee;
    }

    private Project project(String code, String name, Client client, Employee manager) {
        Project project = new Project();
        project.setClient(client);
        project.setProjectCode(code);
        project.setName(name);
        project.setProjectManager(manager);
        project.setStatus(ProjectStatus.ACTIVE);
        project.setStartDate(LocalDate.now().minusMonths(1));
        return project;
    }

    private Allocation allocation(Project project, Employee employee) {
        Allocation allocation = new Allocation();
        allocation.setProject(project);
        allocation.setEmployee(employee);
        allocation.setAllocationPercentage(100);
        allocation.setRoleTitle("Engineer");
        allocation.setStartDate(LocalDate.now().minusWeeks(1));
        return allocation;
    }

    private String allocationBody(UUID projectId, UUID employeeId, int percentage) {
        return objectMapper.createObjectNode()
                .put("projectId", projectId.toString())
                .put("employeeId", employeeId.toString())
                .put("allocationPercentage", percentage)
                .put("roleTitle", "Engineer")
                .put("startDate", LocalDate.now().toString())
                .toString();
    }
}
