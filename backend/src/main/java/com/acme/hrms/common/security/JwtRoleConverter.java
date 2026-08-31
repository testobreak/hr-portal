package com.acme.hrms.common.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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
 * Translates a custom backend access token into a Spring {@link AbstractAuthenticationToken}
 * with the right authorities for our RBAC matrix.
 */
public final class JwtRoleConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>(scopeConverter.convert(jwt));
        authorities.addAll(extractRealmRoles(jwt));
        String principalName = resolvePrincipalName(jwt);
        return new JwtAuthenticationToken(jwt, authorities, principalName);
    }

    private Set<GrantedAuthority> extractRealmRoles(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(s -> new SimpleGrantedAuthority(Roles.AUTHORITY_PREFIX + s))
                .collect(Collectors.toSet());
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
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }
}
