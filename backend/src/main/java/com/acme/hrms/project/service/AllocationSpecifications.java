package com.acme.hrms.project.service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.acme.hrms.project.entity.Allocation;

final class AllocationSpecifications {

    private AllocationSpecifications() {
    }

    static Specification<Allocation> alwaysTrue() {
        return (root, query, cb) -> cb.conjunction();
    }

    static Specification<Allocation> alwaysFalse() {
        return (root, query, cb) -> cb.disjunction();
    }

    static Specification<Allocation> idEquals(UUID id) {
        return (root, query, cb) -> cb.equal(root.get("id"), id);
    }

    static Specification<Allocation> textSearch(String queryText) {
        if (queryText == null || queryText.isBlank()) {
            return alwaysTrue();
        }
        String needle = "%" + queryText.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("project").get("projectCode")), needle),
                cb.like(cb.lower(root.get("project").get("name")), needle),
                cb.like(cb.lower(root.get("employee").get("firstName")), needle),
                cb.like(cb.lower(root.get("employee").get("lastName")), needle),
                cb.like(cb.lower(root.get("roleTitle")), needle));
    }

    static Specification<Allocation> employeeIdIn(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return alwaysFalse();
        }
        return (root, query, cb) -> root.get("employee").get("id").in(ids);
    }

    static Specification<Allocation> ownProjects(UUID subjectUuid) {
        if (subjectUuid == null) {
            return alwaysFalse();
        }
        return (root, query, cb) ->
                cb.equal(root.get("project").get("projectManager").get("keycloakUserId"), subjectUuid);
    }

    static Specification<Allocation> self(UUID subjectUuid) {
        if (subjectUuid == null) {
            return alwaysFalse();
        }
        return (root, query, cb) ->
                cb.equal(root.get("employee").get("keycloakUserId"), subjectUuid);
    }
}
