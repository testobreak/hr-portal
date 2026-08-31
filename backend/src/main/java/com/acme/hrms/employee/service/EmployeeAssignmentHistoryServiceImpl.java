package com.acme.hrms.employee.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acme.hrms.employee.entity.Employee;
import com.acme.hrms.employee.entity.EmployeeAssignmentHistory;
import com.acme.hrms.employee.repository.EmployeeAssignmentHistoryRepository;

@Service
public class EmployeeAssignmentHistoryServiceImpl implements EmployeeAssignmentHistoryService {

    private final EmployeeAssignmentHistoryRepository repository;

    public EmployeeAssignmentHistoryServiceImpl(EmployeeAssignmentHistoryRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void recordAssignment(Employee employee, LocalDate effectiveFrom) {
        Optional<EmployeeAssignmentHistory> latestOpt = repository.findFirstByEmployeeIdOrderByEffectiveFromDesc(employee.getId());
        
        if (latestOpt.isPresent()) {
            EmployeeAssignmentHistory latest = latestOpt.get();
            if (latest.getEffectiveFrom().isBefore(effectiveFrom)) {
                latest.setEffectiveTo(effectiveFrom.minusDays(1));
                repository.save(latest);
            }
        }

        EmployeeAssignmentHistory newHist = EmployeeAssignmentHistory.builder()
                .employee(employee)
                .department(employee.getDepartment())
                .designation(employee.getDesignation())
                .location(employee.getLocation())
                .manager(employee.getManager())
                .effectiveFrom(effectiveFrom)
                .effectiveTo(null)
                .build();
        
        repository.save(newHist);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeAssignmentHistory> getHistory(UUID employeeId) {
        return repository.findHistoryForEmployee(employeeId);
    }
}
