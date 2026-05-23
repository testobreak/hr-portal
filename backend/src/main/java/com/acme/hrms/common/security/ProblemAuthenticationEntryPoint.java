package com.acme.hrms.common.security;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.acme.hrms.common.error.ApiError;
import com.acme.hrms.common.error.ErrorCode;
import com.acme.hrms.common.web.RequestIdFilter;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Produces a uniform problem+json body for 401 responses raised from the
 * Spring Security filter chain (missing token, invalid token, expired token).
 *
 * <p>Without this, Spring Boot's default error pipeline returns a stock
 * {@code application/json} body that doesn't match {@link ApiError}, which
 * frontend has to handle as a special case.
 */
@Component
public class ProblemAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper mapper;

    public ProblemAuthenticationEntryPoint(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        ApiError body = ApiError.of(ErrorCode.UNAUTHORIZED)
                .detail("Authentication is required")
                .instance(request.getRequestURI())
                .traceId(MDC.get(RequestIdFilter.MDC_KEY))
                .build();
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/problem+json");
        response.setCharacterEncoding("UTF-8");
        // Prompt the client; honour OAuth2 spec.
        response.setHeader("WWW-Authenticate", "Bearer realm=\"hrms\", error=\"invalid_token\"");
        mapper.writeValue(response.getOutputStream(), body);
    }
}
