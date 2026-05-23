package com.acme.hrms.common.error;

/**
 * Thrown by services performing row-level authorisation when the caller is
 * authenticated but lacks permission to act on the specific row (e.g. a
 * MANAGER trying to read an employee outside their reports tree).
 *
 * <p>Distinct from Spring Security's {@code AccessDeniedException}, which
 * is raised by URL/method-level checks. Both end up as 403 in
 * {@code GlobalExceptionHandler}; the named class makes service code clearer.
 */
public class ForbiddenAccessException extends RuntimeException {

    public ForbiddenAccessException(String message) {
        super(message);
    }
}
