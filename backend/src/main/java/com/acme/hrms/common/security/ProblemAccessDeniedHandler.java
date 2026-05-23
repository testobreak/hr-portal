package com.acme.hrms.common.security;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.acme.hrms.common.error.ApiError;
import com.acme.hrms.common.error.ErrorCode;
import com.acme.hrms.common.web.RequestIdFilter;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Produces a uniform problem+json body for 403 responses raised from the
 * Spring Security filter chain (e.g. missing role on a {@code @PreAuthorize}
 * check). Domain-level forbidden-by-row errors travel through
 * {@code GlobalExceptionHandler} via {@code ForbiddenAccessException}.
 */
@Component
public class ProblemAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper mapper;

    public ProblemAccessDeniedHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        ApiError body = ApiError.of(ErrorCode.FORBIDDEN)
                .detail("Insufficient privileges for this operation")
                .instance(request.getRequestURI())
                .traceId(MDC.get(RequestIdFilter.MDC_KEY))
                .build();
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/problem+json");
        response.setCharacterEncoding("UTF-8");
        mapper.writeValue(response.getOutputStream(), body);
    }
}
