package com.acme.hrms.employee.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.acme.hrms.employee.entity.Employee;

@Repository
public interface EmployeeRepository
        extends JpaRepository<Employee, UUID>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByEmailIgnoreCase(String email);

    Optional<Employee> findByEmployeeCodeIgnoreCase(String employeeCode);

    Optional<Employee> findByKeycloakUserId(UUID keycloakUserId);

    /**
     * Walk the manager tree under {@code rootId} (inclusive) and return the
     * IDs of the root and every descendant. Excludes soft-deleted rows.
     *
     * <p>Implemented as a recursive CTE in native SQL because JPQL does not
     * support {@code WITH RECURSIVE}. The result set is used to seed an
     * {@code IN (...)} predicate in {@code EmployeeScopePredicates}.
     */
    @Query(value = """
            WITH RECURSIVE descendants(id) AS (
                SELECT id FROM employee
                 WHERE id = :rootId
                   AND deleted_at IS NULL
                UNION ALL
                SELECT e.id FROM employee e
                 JOIN descendants d ON e.manager_id = d.id
                 WHERE e.deleted_at IS NULL
            )
            SELECT id FROM descendants
            """, nativeQuery = true)
    List<UUID> findDescendantIds(@Param("rootId") UUID rootId);

    /**
     * Employees allocated to projects managed by the caller's employee row.
     * Kept as a native query here so the employee feature does not need a Java
     * dependency on the project package.
     */
    @Query(value = """
            SELECT DISTINCT a.employee_id
              FROM allocation a
              JOIN project p ON p.id = a.project_id
              JOIN employee pm ON pm.id = p.project_manager_id
             WHERE pm.keycloak_user_id = :managerSubject
               AND pm.deleted_at IS NULL
               AND p.deleted_at IS NULL
               AND a.deleted_at IS NULL
               AND a.start_date <= CAST(:today AS date)
               AND (a.end_date IS NULL OR a.end_date >= CAST(:today AS date))
            """, nativeQuery = true)
    List<UUID> findProjectMemberIdsForManager(@Param("managerSubject") UUID managerSubject,
                                            @Param("today") LocalDate today);

    /**
     * Active roster members in {@code poolIds} whose total active allocation % is below 100
     * (portfolio under-utilization for PM dashboards).
     */
    @Query(value = """
            SELECT COUNT(*) FROM (
                SELECT e.id
                  FROM employee e
                 WHERE e.deleted_at IS NULL
                   AND e.employment_status IN ('ACTIVE', 'ON_LEAVE')
                   AND e.id IN (:poolIds)
                 GROUP BY e.id
                HAVING COALESCE((
                    SELECT SUM(a.allocation_percentage)
                      FROM allocation a
                     WHERE a.employee_id = e.id
                       AND a.deleted_at IS NULL
                       AND a.start_date <= CAST(:today AS date)
                       AND (a.end_date IS NULL OR a.end_date >= CAST(:today AS date))
                ), 0) < 100
            ) t
            """, nativeQuery = true)
    long countUnderAllocatedAmong(@Param("poolIds") List<UUID> poolIds, @Param("today") LocalDate today);
}
