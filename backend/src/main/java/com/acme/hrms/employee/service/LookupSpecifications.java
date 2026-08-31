package com.acme.hrms.employee.service;

import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import com.acme.hrms.employee.entity.Department;
import com.acme.hrms.employee.entity.Designation;
import com.acme.hrms.employee.entity.Location;

public final class LookupSpecifications {

    private LookupSpecifications() {
    }

    public static Specification<Department> departmentSearch(String queryText) {
        if (queryText == null || queryText.isBlank()) {
            return all();
        }
        String needle = "%" + queryText.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("code")), needle),
                cb.like(cb.lower(root.get("name")), needle),
                cb.like(cb.lower(root.get("description")), needle));
    }

    public static Specification<Designation> designationSearch(String queryText) {
        if (queryText == null || queryText.isBlank()) {
            return all();
        }
        String needle = "%" + queryText.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), needle),
                cb.like(cb.lower(root.get("level")), needle),
                cb.like(cb.lower(root.get("description")), needle));
    }

    public static Specification<Location> locationSearch(String queryText) {
        if (queryText == null || queryText.isBlank()) {
            return all();
        }
        String needle = "%" + queryText.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("code")), needle),
                cb.like(cb.lower(root.get("name")), needle),
                cb.like(cb.lower(root.get("city")), needle),
                cb.like(cb.lower(root.get("country")), needle));
    }

    public static Specification<com.acme.hrms.employee.entity.LegalEntity> legalEntitySearch(String queryText) {
        if (queryText == null || queryText.isBlank()) {
            return all();
        }
        String needle = "%" + queryText.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("code")), needle),
                cb.like(cb.lower(root.get("name")), needle));
    }

    private static <T> Specification<T> all() {
        return (root, query, cb) -> cb.conjunction();
    }
}
