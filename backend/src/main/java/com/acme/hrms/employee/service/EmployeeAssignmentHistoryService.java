package com.acme.hrms.employee.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.acme.hrms.employee.entity.Employee;
import com.acme.hrms.employee.entity.EmployeeAssignmentHistory;

public interface EmployeeAssignmentHistoryService {
    void recordAssignment(Employee employee, LocalDate effectiveFrom);
    List<EmployeeAssignmentHistory> getHistory(UUID employeeId);
}
