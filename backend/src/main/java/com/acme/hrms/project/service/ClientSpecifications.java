package com.acme.hrms.project.service;

import java.util.UUID;
import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import com.acme.hrms.project.entity.Client;
import com.acme.hrms.project.entity.Project;

final class ClientSpecifications {

    private ClientSpecifications() {
    }

    static Specification<Client> alwaysTrue() {
        return (root, query, cb) -> cb.conjunction();
    }

    static Specification<Client> alwaysFalse() {
        return (root, query, cb) -> cb.disjunction();
    }

    static Specification<Client> idEquals(UUID id) {
        return (root, query, cb) -> cb.equal(root.get("id"), id);
    }

    static Specification<Client> textSearch(String queryText) {
        if (queryText == null || queryText.isBlank()) {
            return alwaysTrue();
        }
        String needle = "%" + queryText.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("code")), needle),
                cb.like(cb.lower(root.get("name")), needle),
                cb.like(cb.lower(root.get("description")), needle));
    }

    static Specification<Client> managedBy(UUID subjectUuid) {
        if (subjectUuid == null) {
            return alwaysFalse();
        }
        return (root, query, cb) -> {
            var subquery = query.subquery(UUID.class);
            var project = subquery.from(Project.class);
            subquery.select(project.get("client").get("id"))
                    .where(cb.equal(project.get("projectManager").get("id"), subjectUuid));
            return root.get("id").in(subquery);
        };
    }
}
