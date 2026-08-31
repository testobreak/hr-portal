package com.acme.hrms.manager.service;

import com.acme.hrms.employee.dto.EmployeeSummary;
import com.acme.hrms.employee.entity.Employee;
import com.acme.hrms.manager.entity.ManagerDelegation;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ManagerService {
    void rebuildHierarchy(Employee employee);
    ManagerDelegation createDelegation(UUID managerId, UUID delegateId, LocalDate start, LocalDate end);
    void revokeDelegation(UUID delegationId);
    List<EmployeeSummary> getTeam(UUID managerId, String scope);
    boolean isManagerOf(UUID managerId, UUID employeeId);
}
