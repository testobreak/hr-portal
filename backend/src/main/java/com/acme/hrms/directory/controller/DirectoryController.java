package com.acme.hrms.directory.controller;

import com.acme.hrms.directory.dto.DirectoryEmployeeResponse;
import com.acme.hrms.employee.entity.Employee;
import com.acme.hrms.employee.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/directory")
@Transactional(readOnly = true)
public class DirectoryController {

    private final EmployeeRepository employeeRepository;

    public DirectoryController(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @GetMapping("/employees")
    public ResponseEntity<Page<DirectoryEmployeeResponse>> getDirectory(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Employee> employees = employeeRepository.findAll(pageable);
        Page<DirectoryEmployeeResponse> projection = employees.map(this::toDirectoryResponse);
        return ResponseEntity.ok(projection);
    }

    @GetMapping("/employees/{employeeId}")
    public ResponseEntity<DirectoryEmployeeResponse> getDetails(@PathVariable UUID employeeId) {
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));
        return ResponseEntity.ok(toDirectoryResponse(emp));
    }

    private DirectoryEmployeeResponse toDirectoryResponse(Employee emp) {
        String mgrName = null;
        if (emp.getManager() != null) {
            String first = emp.getManager().getFirstName() == null ? "" : emp.getManager().getFirstName();
            String last = emp.getManager().getLastName() == null ? "" : emp.getManager().getLastName();
            mgrName = (first + " " + last).trim();
        }
        return DirectoryEmployeeResponse.builder()
                .id(emp.getId())
                .fullName((emp.getFirstName() + " " + emp.getLastName()).trim())
                .designationTitle(emp.getDesignation() != null ? emp.getDesignation().getTitle() : null)
                .departmentName(emp.getDepartment() != null ? emp.getDepartment().getName() : null)
                .locationName(emp.getLocation() != null ? emp.getLocation().getName() : null)
                .email(emp.getEmail())
                .phoneNumber(emp.getPhoneNumber())
                .managerName(mgrName)
                .build();
    }
}
