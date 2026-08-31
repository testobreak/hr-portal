package com.acme.hrms.manager.service;

import com.acme.hrms.employee.dto.EmployeeSummary;
import com.acme.hrms.employee.entity.Employee;
import com.acme.hrms.employee.mapper.EmployeeMapper;
import com.acme.hrms.employee.repository.EmployeeRepository;
import com.acme.hrms.manager.entity.ManagerDelegation;
import com.acme.hrms.manager.entity.ManagerHierarchyProjection;
import com.acme.hrms.manager.repository.ManagerDelegationRepository;
import com.acme.hrms.manager.repository.ManagerHierarchyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ManagerServiceImpl implements ManagerService {

    private final ManagerHierarchyRepository hierarchyRepository;
    private final ManagerDelegationRepository delegationRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    public ManagerServiceImpl(ManagerHierarchyRepository hierarchyRepository,
                              ManagerDelegationRepository delegationRepository,
                              EmployeeRepository employeeRepository,
                              EmployeeMapper employeeMapper) {
        this.hierarchyRepository = hierarchyRepository;
        this.delegationRepository = delegationRepository;
        this.employeeRepository = employeeRepository;
        this.employeeMapper = employeeMapper;
    }

    @Override
    @Transactional
    public void rebuildHierarchy(Employee employee) {
        UUID subordinateId = employee.getId();
        UUID tenantId = employee.getTenantId();

        // 1. Close current active records for Y as subordinate
        List<ManagerHierarchyProjection> activeHistory = hierarchyRepository.findAll().stream()
                .filter(p -> tenantId.equals(p.getTenantId()) && subordinateId.equals(p.getSubordinateId()) && p.getEffectiveTo() == null)
                .collect(Collectors.toList());

        for (ManagerHierarchyProjection p : activeHistory) {
            p.setEffectiveTo(LocalDate.now().minusDays(1));
            hierarchyRepository.save(p);
        }

        // 2. If Y has a new manager X, create new reporting paths
        if (employee.getManager() != null) {
            UUID managerId = employee.getManager().getId();

            // Insert Y reports to X (depth 1)
            hierarchyRepository.save(ManagerHierarchyProjection.builder()
                    .tenantId(tenantId)
                    .managerId(managerId)
                    .subordinateId(subordinateId)
                    .depth(1)
                    .effectiveFrom(LocalDate.now())
                    .build());

            // Load all current active managers of X
            List<ManagerHierarchyProjection> managersOfManager = hierarchyRepository.findAll().stream()
                    .filter(p -> tenantId.equals(p.getTenantId()) && managerId.equals(p.getSubordinateId()) && p.getEffectiveTo() == null)
                    .collect(Collectors.toList());

            for (ManagerHierarchyProjection p : managersOfManager) {
                hierarchyRepository.save(ManagerHierarchyProjection.builder()
                        .tenantId(tenantId)
                        .managerId(p.getManagerId())
                        .subordinateId(subordinateId)
                        .depth(p.getDepth() + 1)
                        .effectiveFrom(LocalDate.now())
                        .build());
            }
        }
    }

    @Override
    @Transactional
    public ManagerDelegation createDelegation(UUID managerId, UUID delegateId, LocalDate start, LocalDate end) {
        ManagerDelegation delegation = ManagerDelegation.builder()
                .managerId(managerId)
                .delegateId(delegateId)
                .startDate(start)
                .endDate(end)
                .status("ACTIVE")
                .build();
        return delegationRepository.save(delegation);
    }

    @Override
    @Transactional
    public void revokeDelegation(UUID delegationId) {
        ManagerDelegation delegation = delegationRepository.findById(delegationId)
                .orElseThrow(() -> new IllegalArgumentException("Delegation not found: " + delegationId));
        delegation.setStatus("REVOKED");
        delegationRepository.save(delegation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeSummary> getTeam(UUID managerId, String scope) {
        List<ManagerHierarchyProjection> matches;
        if ("direct".equalsIgnoreCase(scope)) {
            matches = hierarchyRepository.findActiveDirectReports(managerId);
        } else {
            matches = hierarchyRepository.findActiveReports(managerId);
        }

        List<EmployeeSummary> summaries = new ArrayList<>();
        for (ManagerHierarchyProjection m : matches) {
            employeeRepository.findById(m.getSubordinateId()).ifPresent(emp -> {
                summaries.add(employeeMapper.toSummary(emp));
            });
        }
        return summaries;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isManagerOf(UUID managerId, UUID employeeId) {
        if (managerId.equals(employeeId)) return true;

        List<ManagerDelegation> activeDelegations = delegationRepository.findActiveDelegationsForDelegate(managerId, LocalDate.now());
        Set<UUID> actingManagers = activeDelegations.stream().map(ManagerDelegation::getManagerId).collect(Collectors.toSet());
        actingManagers.add(managerId);

        for (UUID actingManagerId : actingManagers) {
            boolean isDirectOrIndirect = hierarchyRepository.findAll().stream()
                    .anyMatch(p -> actingManagerId.equals(p.getManagerId())
                            && employeeId.equals(p.getSubordinateId())
                            && p.getEffectiveTo() == null);
            if (isDirectOrIndirect) {
                return true;
            }
        }
        return false;
    }
}
