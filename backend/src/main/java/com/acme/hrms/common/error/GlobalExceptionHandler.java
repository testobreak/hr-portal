package com.acme.hrms.common.error;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.acme.hrms.common.web.RequestIdFilter;

/**
 * Single source of error responses. Every error path eventually flows here so
 * the body shape ({@link ApiError}) and the {@code Content-Type}
 * ({@code application/problem+json}) are uniform.
 *
 * <p>Conventions:
 * <ul>
 *   <li>{@code detail} is caller-safe text. No stack traces, no SQL, no PII.</li>
 *   <li>5xx logs include the exception at ERROR; 4xx log at WARN without stack trace.</li>
 *   <li>{@code traceId} is the {@code X-Request-Id} from MDC.</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final MediaType PROBLEM_JSON = MediaType.parseMediaType("application/problem+json");

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex,
                                                     HttpServletRequest req) {
        List<FieldError> errors = new ArrayList<>();
        for (org.springframework.validation.FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.add(new FieldError(fe.getField(), fe.getCode(), fe.getDefaultMessage()));
        }
        return build(ErrorCode.VALIDATION_ERROR, "One or more fields are invalid", req, errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex,
                                                              HttpServletRequest req) {
        List<FieldError> errors = new ArrayList<>();
        for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
            String path = v.getPropertyPath() != null ? v.getPropertyPath().toString() : "";
            String code = v.getConstraintDescriptor() != null
                    ? v.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName()
                    : null;
            errors.add(new FieldError(path, code, v.getMessage()));
        }
        return build(ErrorCode.VALIDATION_ERROR, "One or more parameters are invalid", req, errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex,
                                                     HttpServletRequest req) {
        return build(ErrorCode.BAD_REQUEST, "Malformed request body", req, null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParam(MissingServletRequestParameterException ex,
                                                       HttpServletRequest req) {
        List<FieldError> errors = List.of(new FieldError(ex.getParameterName(), "REQUIRED", ex.getMessage()));
        return build(ErrorCode.VALIDATION_ERROR, "Missing required parameter", req, errors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                       HttpServletRequest req) {
        List<FieldError> errors = List.of(new FieldError(ex.getName(), "TYPE_MISMATCH", ex.getMessage()));
        return build(ErrorCode.BAD_REQUEST, "Parameter has the wrong type", req, errors);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiError> handleNoHandler(NoHandlerFoundException ex,
                                                    HttpServletRequest req) {
        return build(ErrorCode.NOT_FOUND, "No handler for " + ex.getHttpMethod() + " " + ex.getRequestURL(), req, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex,
                                                           HttpServletRequest req) {
        return build(ErrorCode.METHOD_NOT_ALLOWED, ex.getMessage(), req, null);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex, HttpServletRequest req) {
        return build(ErrorCode.NOT_FOUND, ex.getMessage(), req, null);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex, HttpServletRequest req) {
        return build(ErrorCode.CONFLICT, ex.getMessage(), req, null);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> handleOptimisticLock(OptimisticLockingFailureException ex,
                                                         HttpServletRequest req) {
        // The classic "someone else updated the row while you were editing".
        // Surface it as 409 so the SPA can re-load and prompt the user.
        return build(ErrorCode.CONFLICT,
                "The record was modified by someone else; reload and try again",
                req, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex,
                                                        HttpServletRequest req) {
        // Most common cause: unique-constraint collision that got past the
        // service-layer pre-check (race). Some FK / CHECK violations also
        // land here. Treat as 409; do NOT echo the SQL message back to the
        // caller — it can leak schema details.
        log.warn("Data integrity violation at {} {}: {}",
                req.getMethod(), req.getRequestURI(),
                rootMessage(ex));
        return build(ErrorCode.CONFLICT,
                "Request violates a uniqueness or integrity constraint",
                req, null);
    }

    @ExceptionHandler(ForbiddenAccessException.class)
    public ResponseEntity<ApiError> handleForbiddenDomain(ForbiddenAccessException ex,
                                                          HttpServletRequest req) {
        return build(ErrorCode.FORBIDDEN, ex.getMessage(), req, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex,
                                                       HttpServletRequest req) {
        return build(ErrorCode.FORBIDDEN, "Insufficient privileges for this operation", req, null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuth(AuthenticationException ex, HttpServletRequest req) {
        return build(ErrorCode.UNAUTHORIZED, "Authentication is required", req, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAny(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception at {} {}", req.getMethod(), req.getRequestURI(), ex);
        return build(ErrorCode.INTERNAL, "An unexpected error occurred", req, null);
    }

    private ResponseEntity<ApiError> build(ErrorCode code,
                                           String detail,
                                           HttpServletRequest req,
                                           List<FieldError> fieldErrors) {
        ApiError body = ApiError.of(code)
                .detail(detail)
                .instance(req != null ? req.getRequestURI() : null)
                .traceId(MDC.get(RequestIdFilter.MDC_KEY))
                .fieldErrors(fieldErrors)
                .build();
        HttpStatus status = code.status();
        if (status.is4xxClientError()) {
            log.warn("{} {} -> {} ({}) {}",
                    req != null ? req.getMethod() : "?",
                    req != null ? req.getRequestURI() : "?",
                    status.value(),
                    code.name(),
                    detail);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(PROBLEM_JSON);
        return new ResponseEntity<>(body, headers, status);
    }

    private static String rootMessage(Throwable ex) {
        Throwable cur = ex;
        while (cur.getCause() != null && cur.getCause() != cur) {
            cur = cur.getCause();
        }
        return cur.getMessage();
    }
}
