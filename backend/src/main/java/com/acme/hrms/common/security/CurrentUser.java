package com.acme.hrms.common.security;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Snapshot of the caller built from the validated JWT.
 *
 * <p>Pass this through to services that need to make authorisation decisions
 * or write {@code created_by} / {@code updated_by} columns. Never read the
 * {@link SecurityContextHolder} from inside services — take a {@code CurrentUser}
 * argument instead, both for testability and to make the contract explicit.
 *
 * <p>The {@code subjectUuid} field is the Keycloak user id, which we mirror
 * into {@code employee.keycloak_user_id} once the employee module lands.
 */
public record CurrentUser(
        UUID subjectUuid,
        String username,
        String email,
        Set<String> roles,
        UUID tenantId
) {

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public boolean hasAnyRole(String... candidates) {
        for (String r : candidates) {
            if (roles.contains(r)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Resolve the current user from the security context, if any. Returns
     * empty for anonymous / unauthenticated requests.
     */
    public static Optional<CurrentUser> fromSecurityContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof JwtAuthenticationToken token)) {
            return Optional.empty();
        }
        return Optional.of(from(token.getToken()));
    }

    public static CurrentUser from(Jwt jwt) {
        UUID subject = parseSubjectAsUuid(jwt.getSubject());
        String username = jwt.getClaimAsString("preferred_username");
        String email = jwt.getClaimAsString("email");
        Set<String> roles = JwtRoleConverter.rolesOf(jwt);
        String tenantClaim = jwt.getClaimAsString("tenant_id");
        UUID tenantId = null;
        if (tenantClaim != null && !tenantClaim.isBlank()) {
            try {
                tenantId = UUID.fromString(tenantClaim.trim());
            } catch (IllegalArgumentException e) {
                // Ignore malformed UUID
            }
        }
        return new CurrentUser(subject, username, email, roles, tenantId);
    }

    private static UUID parseSubjectAsUuid(String subject) {
        if (subject == null || subject.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException ex) {
            // Keycloak issues UUID subjects, but a non-UUID 'sub' (e.g. for a
            // service account or external IdP) shouldn't blow up auth.
            return null;
        }
    }
}
