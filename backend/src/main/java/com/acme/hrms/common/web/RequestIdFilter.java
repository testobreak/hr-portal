package com.acme.hrms.common.web;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Tags every request with a stable request id, propagates it into
 * {@link MDC} so logs can be correlated, and echoes it back to the caller
 * in the {@code X-Request-Id} response header.
 *
 * <p>If the client supplies an {@code X-Request-Id} header we honour it,
 * provided it looks reasonable (length and charset). Otherwise we mint a
 * UUIDv4. Trusting an unbounded inbound id is a classic log-poisoning hole.
 *
 * <p>The same id surfaces in {@code ApiError.traceId} for problem+json bodies.
 */
@Component
public class RequestIdFilter extends OncePerRequestFilter implements Ordered {

    public static final String HEADER = "X-Request-Id";
    public static final String MDC_KEY = "requestId";
    private static final int MAX_HEADER_LENGTH = 128;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestId = sanitise(request.getHeader(HEADER));
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
        }
        MDC.put(MDC_KEY, requestId);
        response.setHeader(HEADER, requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }

    private static String sanitise(String candidate) {
        if (candidate == null) {
            return null;
        }
        String trimmed = candidate.trim();
        if (trimmed.isEmpty() || trimmed.length() > MAX_HEADER_LENGTH) {
            return null;
        }
        // Allow only ASCII alphanumerics, dashes and underscores. Anything
        // else (newlines, control chars, semicolons) gets dropped.
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            boolean ok = (c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z')
                    || (c >= '0' && c <= '9')
                    || c == '-' || c == '_';
            if (!ok) {
                return null;
            }
        }
        return trimmed;
    }

    @Override
    public int getOrder() {
        // Run before Spring Security so MDC is populated even on 401/403.
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
