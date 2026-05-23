package com.acme.hrms.common.security;

/**
 * Canonical role names. Mirrors the realm roles in
 * {@code infra/keycloak/realm-hrms.json} and the matrix in
 * {@code docs/rbac-matrix.md}.
 *
 * <p>Use these constants in {@code @PreAuthorize} expressions instead of
 * string literals so that typos become compile errors:
 *
 * <pre>{@code
 * @PreAuthorize("hasRole('" + Roles.HR_ADMIN + "')")
 * }</pre>
 *
 * <p>The bare names live here without the {@code ROLE_} prefix because
 * Spring's {@code hasRole(...)} adds it automatically. {@link JwtRoleConverter}
 * is the only place that adds {@code ROLE_} (when building authorities).
 */
public final class Roles {

    public static final String SUPER_ADMIN = "SUPER_ADMIN";
    public static final String HR_ADMIN = "HR_ADMIN";
    public static final String FINANCE_ADMIN = "FINANCE_ADMIN";
    public static final String LEADERSHIP = "LEADERSHIP";
    public static final String MANAGER = "MANAGER";
    public static final String PROJECT_MANAGER = "PROJECT_MANAGER";
    public static final String EMPLOYEE = "EMPLOYEE";

    public static final String AUTHORITY_PREFIX = "ROLE_";

    private Roles() {
    }
}
