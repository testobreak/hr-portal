package com.acme.hrms.employee.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import org.springframework.data.jpa.domain.Specification;

import com.acme.hrms.employee.entity.Employee;
import com.acme.hrms.employee.entity.EmploymentStatus;
import com.acme.hrms.project.entity.Allocation;

/**
 * Reusable {@link Specification}s for {@code Employee} reads. Soft delete
 * is already enforced by {@code @SQLRestriction} on the entity, so these
 * specifications only carry scoping / search predicates.
 */
public final class EmployeeSpecifications {

    private EmployeeSpecifications() {
    }

    public static Specification<Employee> idIn(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return (root, query, cb) -> cb.disjunction(); // matches nothing
        }
        return (root, query, cb) -> root.get("id").in(ids);
    }

    public static Specification<Employee> idEquals(UUID id) {
        if (id == null) {
            return (root, query, cb) -> cb.disjunction();
        }
        return (root, query, cb) -> cb.equal(root.get("id"), id);
    }

    public static Specification<Employee> alwaysFalse() {
        return (root, query, cb) -> cb.disjunction();
    }

    public static Specification<Employee> alwaysTrue() {
        return (root, query, cb) -> cb.conjunction();
    }

    public static Specification<Employee> textSearch(String queryText) {
        if (queryText == null || queryText.isBlank()) {
            return alwaysTrue();
        }
        String needle = "%" + queryText.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("employeeCode")), needle),
                cb.like(cb.lower(root.get("firstName")), needle),
                cb.like(cb.lower(root.get("lastName")), needle),
                cb.like(cb.lower(root.get("email")), needle));
    }

    /** Active or on long-term leave — counts toward operational headcount. */
    public static Specification<Employee> activeRosterStatuses() {
        return (root, query, cb) -> root.get("employmentStatus")
                .in(EmploymentStatus.ACTIVE, EmploymentStatus.ON_LEAVE);
    }

    public static Specification<Employee> joinedOnOrAfter(LocalDate minInclusive) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dateOfJoining"), minInclusive);
    }

    public static Specification<Employee> terminalEmploymentStatuses() {
        return (root, query, cb) -> root.get("employmentStatus")
                .in(EmploymentStatus.TERMINATED, EmploymentStatus.RESIGNED, EmploymentStatus.ABSCONDED);
    }

    public static Specification<Employee> updatedOnOrAfter(Instant minInclusive) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("updatedAt"), minInclusive);
    }

    /** At least one non-deleted allocation covering {@code asOf}. */
    public static Specification<Employee> hasActiveAllocationOn(LocalDate asOf) {
        return (root, query, cb) -> {
            Subquery<Integer> sq = query.subquery(Integer.class);
            Root<Allocation> alloc = sq.from(Allocation.class);
            sq.select(cb.literal(1));
            sq.where(
                    cb.equal(alloc.get("employee"), root),
                    cb.isNull(alloc.get("deletedAt")),
                    cb.lessThanOrEqualTo(alloc.get("startDate"), asOf),
                    cb.or(cb.isNull(alloc.get("endDate")), cb.greaterThanOrEqualTo(alloc.get("endDate"), asOf)));
            return cb.exists(sq);
        };
    }
}
