package com.acme.hrms.common;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.acme.hrms.announcement.controller.AnnouncementController;
import com.acme.hrms.announcement.service.AnnouncementService;
import com.acme.hrms.attendance.controller.AttendanceController;
import com.acme.hrms.attendance.service.AttendanceService;
import com.acme.hrms.attendance.service.TimesheetService;
import com.acme.hrms.common.error.GlobalExceptionHandler;
import com.acme.hrms.common.error.NotFoundException;
import com.acme.hrms.directory.controller.DirectoryController;
import com.acme.hrms.document.controller.DocumentV1Controller;
import com.acme.hrms.document.repository.DocumentCategoryRepository;
import com.acme.hrms.document.repository.EmployeeDocumentRepository;
import com.acme.hrms.employee.repository.EmployeeRepository;
import com.acme.hrms.leave.controller.LeaveController;
import com.acme.hrms.leave.controller.LeaveRequestController;
import com.acme.hrms.leave.service.LeaveRequestService;
import com.acme.hrms.leave.service.LeaveService;
import com.acme.hrms.manager.controller.ManagerController;
import com.acme.hrms.manager.service.ManagerService;
import com.acme.hrms.payroll.controller.PayrollController;
import com.acme.hrms.payroll.service.PayrollService;
import com.acme.hrms.salary.controller.SalaryController;
import com.acme.hrms.salary.service.SalaryService;

@ExtendWith(MockitoExtension.class)
class GetApiNoDataTest {

    private MockMvc mockMvc;

    @Mock private EmployeeRepository employeeRepository;
    @Mock private EmployeeDocumentRepository documentRepository;
    @Mock private DocumentCategoryRepository categoryRepository;
    @Mock private AnnouncementService announcementService;
    @Mock private LeaveService leaveService;
    @Mock private LeaveRequestService leaveRequestService;
    @Mock private ManagerService managerService;
    @Mock private PayrollService payrollService;
    @Mock private AttendanceService attendanceService;
    @Mock private TimesheetService timesheetService;
    @Mock private SalaryService salaryService;

    @RestController
    static class SampleController {
        @GetMapping("/test/not-found/{id}")
        public String notFoundEndpoint(@PathVariable UUID id) {
            throw NotFoundException.of("Employee", id);
        }

        @GetMapping("/test/no-such-element")
        public String noSuchElementEndpoint() {
            throw new java.util.NoSuchElementException("No value present in database");
        }

        @GetMapping("/test/entity-not-found")
        public String entityNotFoundEndpoint() {
            throw new jakarta.persistence.EntityNotFoundException("Entity does not exist");
        }

        @GetMapping("/test/illegal-argument-not-found")
        public String illegalArgumentNotFoundEndpoint() {
            throw new IllegalArgumentException("Resource not found: sample");
        }

        @GetMapping("/test/illegal-argument-other")
        public String illegalArgumentOtherEndpoint() {
            throw new IllegalArgumentException("Invalid parameter value");
        }

        @GetMapping("/test/user-not-found-auth")
        public String userNotFoundAuthEndpoint() {
            throw new IllegalStateException("User not found in security context");
        }
    }

    @BeforeEach
    void setup() {
        DirectoryController directoryController = new DirectoryController(employeeRepository);
        DocumentV1Controller documentV1Controller = new DocumentV1Controller(documentRepository, categoryRepository, employeeRepository);
        AnnouncementController announcementController = new AnnouncementController(announcementService, employeeRepository);
        LeaveController leaveController = new LeaveController(leaveService, employeeRepository);
        LeaveRequestController leaveRequestController = new LeaveRequestController(leaveRequestService, employeeRepository);
        ManagerController managerController = new ManagerController(managerService, employeeRepository);
        PayrollController payrollController = new PayrollController(payrollService);
        org.springframework.test.util.ReflectionTestUtils.setField(payrollController, "employeeRepository", employeeRepository);
        AttendanceController attendanceController = new AttendanceController(attendanceService, timesheetService);
        org.springframework.test.util.ReflectionTestUtils.setField(attendanceController, "employeeRepository", employeeRepository);
        SalaryController salaryController = new SalaryController(salaryService);
        SampleController sampleController = new SampleController();

        mockMvc = MockMvcBuilders.standaloneSetup(
                        directoryController,
                        documentV1Controller,
                        announcementController,
                        leaveController,
                        leaveRequestController,
                        managerController,
                        payrollController,
                        attendanceController,
                        salaryController,
                        sampleController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void directoryEmployeeNotFoundReturns200WithDataNotFoundResponse() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(employeeRepository.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/directory/employees/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Data does not exist"));
    }

    @Test
    void getNotFoundExceptionReturns200WithDataNotFoundResponse() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(get("/test/not-found/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Data does not exist"))
                .andExpect(jsonPath("$.detail").value("Employee " + id + " not found"));
    }

    @Test
    void getNoSuchElementExceptionReturns200WithDataNotFoundResponse() throws Exception {
        mockMvc.perform(get("/test/no-such-element"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Data does not exist"));
    }

    @Test
    void getEntityNotFoundExceptionReturns200WithDataNotFoundResponse() throws Exception {
        mockMvc.perform(get("/test/entity-not-found"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Data does not exist"));
    }

    @Test
    void getIllegalArgumentExceptionWithNotFoundMessageReturns200() throws Exception {
        mockMvc.perform(get("/test/illegal-argument-not-found"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Data does not exist"));
    }

    @Test
    void getIllegalArgumentExceptionOtherReturns400Not500() throws Exception {
        mockMvc.perform(get("/test/illegal-argument-other"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getIllegalStateExceptionAuthReturns401Not500() throws Exception {
        mockMvc.perform(get("/test/user-not-found-auth"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void myDocumentsWhenNoEmployeeProfileReturns200EmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/me/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void myAnnouncementsWhenNoEmployeeProfileReturns200EmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/me/announcements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void myLeaveBalancesWhenNoEmployeeProfileReturns200EmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/me/leave-balances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void myLeaveRequestsWhenNoEmployeeProfileReturns200EmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/me/leave-requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void myTeamWhenNoEmployeeProfileReturns200EmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/me/team"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void myPayslipsWhenNoEmployeeProfileReturns200EmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/payroll/me/payslips"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void myAttendanceLatestWhenNoEmployeeProfileReturns200() throws Exception {
        mockMvc.perform(get("/api/v1/attendance/me/latest"))
                .andExpect(status().isOk());
    }

    @Test
    void myAttendanceLogsWhenNoEmployeeProfileReturns200EmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/attendance/me/logs")
                        .param("startIso", "2026-01-01T00:00:00Z")
                        .param("endIso", "2026-01-31T23:59:59Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void myTimesheetsWhenNoEmployeeProfileReturns200EmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/attendance/me/timesheets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
