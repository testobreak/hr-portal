package com.acme.hrms.common.tenant;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Extracts tenant_id from the authenticated JWT token claim or the X-Tenant-Id request header,
 * sets it in TenantContext, and clears it on completion.
 */
public class TenantFilter extends OncePerRequestFilter {

    public static final String HEADER_TENANT_ID = "X-Tenant-Id";
    public static final String CLAIM_TENANT_ID = "tenant_id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        UUID tenantId = null;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            String claimVal = jwtAuth.getToken().getClaimAsString(CLAIM_TENANT_ID);
            if (claimVal != null && !claimVal.isBlank()) {
                try {
                    tenantId = UUID.fromString(claimVal.trim());
                } catch (IllegalArgumentException e) {
                    // Ignore malformed UUID
                }
            }
        }

        if (tenantId == null) {
            String headerVal = request.getHeader(HEADER_TENANT_ID);
            if (headerVal != null && !headerVal.isBlank()) {
                try {
                    tenantId = UUID.fromString(headerVal.trim());
                } catch (IllegalArgumentException e) {
                    // Ignore malformed UUID
                }
            }
        }

        if (tenantId != null) {
            TenantContext.setTenantId(tenantId);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
