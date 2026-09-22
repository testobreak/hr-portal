package com.acme.hrms.common.web;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Utility for sanitizing incoming HTTP pagination and sorting parameters.
 * Prevents PropertyReferenceException and unexpected 500 errors when clients
 * (such as Postman or frontend query builders) pass invalid or placeholder sort properties.
 */
public final class PageableUtils {

    private PageableUtils() {}

    public static Pageable sanitize(Pageable pageable, Class<?> entityClass) {
        if (pageable == null || pageable.getSort().isUnsorted()) {
            return pageable;
        }

        Set<String> validFields = getEntityFields(entityClass);
        List<Sort.Order> safeOrders = new ArrayList<>();

        for (Sort.Order order : pageable.getSort()) {
            if (validFields.contains(order.getProperty())) {
                safeOrders.add(order);
            }
        }

        if (safeOrders.isEmpty()) {
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.unsorted());
        }

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(safeOrders));
    }

    private static Set<String> getEntityFields(Class<?> clazz) {
        Set<String> fields = new HashSet<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                fields.add(field.getName());
            }
            current = current.getSuperclass();
        }
        return fields;
    }
}
