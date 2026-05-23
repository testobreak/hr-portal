package com.acme.hrms.support;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.acme.hrms.common.security.Roles;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

/**
 * Helpers for issuing test bearer tokens with specific subject + roles.
 *
 * <p>Centralised so individual tests don't repeat the JWT-builder dance.
 * Always passes the same claims a real Keycloak token would carry:
 * {@code sub}, {@code preferred_username}, {@code email}, and the
 * {@code realm_access.roles} array.
 */
public final class JwtTestSupport {

    private JwtTestSupport() {
    }

    public static RequestPostProcessor asUser(UUID subject, String username, String... roles) {
        List<String> roleList = Arrays.asList(roles);
        SimpleGrantedAuthority[] authorities = roleList.stream()
                .map(r -> new SimpleGrantedAuthority(Roles.AUTHORITY_PREFIX + r))
                .toArray(SimpleGrantedAuthority[]::new);
        return jwt()
                .jwt(builder -> applyClaims(builder, subject, username, roleList))
                .authorities(authorities);
    }

    private static void applyClaims(Jwt.Builder builder, UUID subject, String username, List<String> roles) {
        builder.subject(subject.toString())
                .claim("preferred_username", username)
                .claim("email", username)
                .claim("realm_access", Map.of("roles", roles));
    }
}
