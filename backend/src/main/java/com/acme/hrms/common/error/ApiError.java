package com.acme.hrms.common.error;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * RFC 7807 problem+json body.
 *
 * <p>Field meanings:
 * <ul>
 *   <li>{@code type}     URI identifying the problem class (stable, dereferenceable
 *                        only by convention).</li>
 *   <li>{@code title}    Short, human-readable summary, same for all instances of
 *                        a given {@code type}.</li>
 *   <li>{@code status}   HTTP status code mirrored into the body for log readability.</li>
 *   <li>{@code code}     Machine-readable error code (see {@link ErrorCode}).</li>
 *   <li>{@code detail}   Caller-facing description specific to this occurrence.
 *                        Must NOT include sensitive data.</li>
 *   <li>{@code instance} Path of the failing request (e.g. {@code /api/employees/123}).</li>
 *   <li>{@code traceId}  Same value as the {@code X-Request-Id} response header.</li>
 *   <li>{@code timestamp} Server-side moment of failure (UTC, ISO 8601).</li>
 *   <li>{@code fieldErrors} Per-field validation issues, when applicable.</li>
 * </ul>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        String type,
        String title,
        int status,
        String code,
        String detail,
        String instance,
        String traceId,
        Instant timestamp,
        List<FieldError> fieldErrors
) {

    public static Builder of(ErrorCode code) {
        return new Builder(code);
    }

    public static final class Builder {
        private final ErrorCode code;
        private String title;
        private String detail;
        private String instance;
        private String traceId;
        private List<FieldError> fieldErrors;

        private Builder(ErrorCode code) {
            this.code = code;
            this.title = defaultTitle(code);
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder detail(String detail) {
            this.detail = detail;
            return this;
        }

        public Builder instance(String instance) {
            this.instance = instance;
            return this;
        }

        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public Builder fieldErrors(List<FieldError> errors) {
            this.fieldErrors = (errors == null || errors.isEmpty()) ? null : List.copyOf(errors);
            return this;
        }

        public ApiError build() {
            return new ApiError(
                    code.type(),
                    title,
                    code.status().value(),
                    code.name(),
                    detail,
                    instance,
                    traceId,
                    Instant.now(),
                    fieldErrors);
        }

        private static String defaultTitle(ErrorCode code) {
            return switch (code) {
                case VALIDATION_ERROR -> "Validation failed";
                case NOT_FOUND -> "Resource not found";
                case CONFLICT -> "Conflict";
                case UNAUTHORIZED -> "Authentication required";
                case FORBIDDEN -> "Forbidden";
                case METHOD_NOT_ALLOWED -> "Method not allowed";
                case UNSUPPORTED_MEDIA_TYPE -> "Unsupported media type";
                case PAYLOAD_TOO_LARGE -> "Payload too large";
                case BAD_REQUEST -> "Bad request";
                case INTERNAL -> "Internal server error";
            };
        }
    }
}
