package com.acme.hrms.common.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

/**
 * Translates a Keycloak access token into a Spring {@link AbstractAuthenticationToken}
 * with the right authorities for our RBAC matrix.
 *
 * <p>Keycloak places realm roles inside {@code realm_access.roles}. Spring
 * Security's default converter only picks up {@code scope}/{@code scp}, so
 * we extend it manually.
 *
 * <p>Authorities produced look like:
 * <ul>
 *   <li>{@code ROLE_HR_ADMIN}      (from realm role {@code HR_ADMIN})</li>
 *   <li>{@code SCOPE_profile}      (from OAuth2 scope, kept for diagnostics)</li>
 * </ul>
 *
 * <p>The principal name is the {@code preferred_username} claim if present,
 * else {@code email}, else {@code sub}.
 */
public final class JwtRoleConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String CLAIM_REALM_ACCESS = "realm_access";
    private static final String CLAIM_ROLES = "roles";

    private final JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>(scopeConverter.convert(jwt));
        authorities.addAll(extractRealmRoles(jwt));
        String principalName = resolvePrincipalName(jwt);
        return new JwtAuthenticationToken(jwt, authorities, principalName);
    }

    private Set<GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap(CLAIM_REALM_ACCESS);
        if (realmAccess == null) {
            return Set.of();
        }
        Object rolesObj = realmAccess.get(CLAIM_ROLES);
        if (!(rolesObj instanceof List<?> rawRoles)) {
            return Set.of();
        }
        Set<GrantedAuthority> result = new HashSet<>();
        for (Object role : rawRoles) {
            if (role instanceof String s && !s.isBlank()) {
                result.add(new SimpleGrantedAuthority(Roles.AUTHORITY_PREFIX + s));
            }
        }
        return result;
    }

    private static String resolvePrincipalName(Jwt jwt) {
        String preferred = jwt.getClaimAsString("preferred_username");
        if (preferred != null && !preferred.isBlank()) {
            return preferred;
        }
        String email = jwt.getClaimAsString("email");
        if (email != null && !email.isBlank()) {
            return email;
        }
        return jwt.getSubject();
    }

    /**
     * Convenience: return only the role names (without the {@code ROLE_}
     * prefix) for a given JWT. Useful from {@link CurrentUser}.
     */
    public static Set<String> rolesOf(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap(CLAIM_REALM_ACCESS);
        if (realmAccess == null) {
            return Set.of();
        }
        Object rolesObj = realmAccess.get(CLAIM_ROLES);
        if (!(rolesObj instanceof List<?> rawRoles)) {
            return Set.of();
        }
        return rawRoles.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }
}
