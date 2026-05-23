package com.acme.hrms.common.error;

import org.springframework.http.HttpStatus;

/**
 * Stable machine-readable error codes. The HTTP status is derived; never the
 * other way around. Adding a code is a breaking-ish API change — coordinate
 * with frontend.
 */
public enum ErrorCode {

    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "validation"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "not-found"),
    CONFLICT(HttpStatus.CONFLICT, "conflict"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "unauthorized"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "forbidden"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "method-not-allowed"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "unsupported-media-type"),
    PAYLOAD_TOO_LARGE(HttpStatus.CONTENT_TOO_LARGE, "payload-too-large"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "bad-request"),
    INTERNAL(HttpStatus.INTERNAL_SERVER_ERROR, "internal");

    private static final String TYPE_BASE = "https://hrms.acme/errors/";

    private final HttpStatus status;
    private final String slug;

    ErrorCode(HttpStatus status, String slug) {
        this.status = status;
        this.slug = slug;
    }

    public HttpStatus status() {
        return status;
    }

    public String type() {
        return TYPE_BASE + slug;
    }
}
