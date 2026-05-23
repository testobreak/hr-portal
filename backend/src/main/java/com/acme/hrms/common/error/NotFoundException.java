package com.acme.hrms.common.error;

/**
 * Thrown by services when a requested resource does not exist (or is not
 * visible to the caller — we deliberately collapse "not found" and "no
 * permission to know about it" to avoid information leakage).
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException of(String entity, Object id) {
        return new NotFoundException(entity + " " + id + " not found");
    }
}
