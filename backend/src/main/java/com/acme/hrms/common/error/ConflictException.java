package com.acme.hrms.common.error;

/**
 * Thrown when a request would violate a business invariant or unique
 * constraint (e.g. duplicate employee email, overlapping salary effective
 * dates, attempt to delete a project with active allocations).
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
