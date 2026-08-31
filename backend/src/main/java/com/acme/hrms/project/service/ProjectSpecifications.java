package com.acme.hrms.project.service;

import java.util.UUID;
import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import com.acme.hrms.project.entity.Allocation;
import com.acme.hrms.project.entity.Project;

final class ProjectSpecifications {

    private ProjectSpecifications() {
    }

    static Specification<Project> alwaysTrue() {
        return (root, query, cb) -> cb.conjunction();
    }

    static Specification<Project> alwaysFalse() {
        return (root, query, cb) -> cb.disjunction();
    }

    static Specification<Project> idEquals(UUID id) {
        return (root, query, cb) -> cb.equal(root.get("id"), id);
    }

    static Specification<Project> textSearch(String queryText) {
        if (queryText == null || queryText.isBlank()) {
            return alwaysTrue();
        }
        String needle = "%" + queryText.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("projectCode")), needle),
                cb.like(cb.lower(root.get("name")), needle),
                cb.like(cb.lower(root.get("description")), needle),
                cb.like(cb.lower(root.get("client").get("name")), needle));
    }

    static Specification<Project> ownedOrMemberOf(UUID subjectUuid) {
        if (subjectUuid == null) {
            return alwaysFalse();
        }
        return (root, query, cb) -> {
            var memberSubquery = query.subquery(UUID.class);
            var allocation = memberSubquery.from(Allocation.class);
            memberSubquery.select(allocation.get("project").get("id"))
                    .where(cb.equal(allocation.get("employee").get("id"), subjectUuid));
            return cb.or(
                    cb.equal(root.get("projectManager").get("id"), subjectUuid),
                    root.get("id").in(memberSubquery));
        };
    }

    static Specification<Project> memberOf(UUID subjectUuid) {
        if (subjectUuid == null) {
            return alwaysFalse();
        }
        return (root, query, cb) -> {
            var subquery = query.subquery(UUID.class);
            var allocation = subquery.from(Allocation.class);
            subquery.select(allocation.get("project").get("id"))
                    .where(cb.equal(allocation.get("employee").get("id"), subjectUuid));
            return root.get("id").in(subquery);
        };
    }
}
