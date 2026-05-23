package com.acme.hrms.employee.service;

import java.util.UUID;

import com.acme.hrms.common.security.CurrentUser;
import com.acme.hrms.common.security.Roles;

/**
 * Resolves the row-level scope a caller has on the {@code employee} table.
 *
 * <p>The mapping mirrors the RBAC matrix §1 (Employee). Roles are unioned;
 * the most permissive scope wins.
 */
public enum EmployeeScope {

    /** No restriction: SUPER_ADMIN, HR_ADMIN, FINANCE_ADMIN, LEADERSHIP. */
    ALL,

    /** Caller's employee row plus every descendant in the manager tree (MANAGER). */
    REPORTS,

    /** Project members of projects the caller manages (PROJECT_MANAGER). */
    PROJECT_MEMBERS,

    /** Caller's own employee row only (EMPLOYEE). */
    SELF,

    /** Authenticated but no permission to read employee rows (placeholder). */
    NONE;

    public static EmployeeScope resolve(CurrentUser user) {
        if (user == null) {
            return NONE;
        }
        if (user.hasAnyRole(Roles.SUPER_ADMIN, Roles.HR_ADMIN, Roles.FINANCE_ADMIN, Roles.LEADERSHIP)) {
            return ALL;
        }
        if (user.hasRole(Roles.MANAGER)) {
            return REPORTS;
        }
        if (user.hasRole(Roles.PROJECT_MANAGER)) {
            return PROJECT_MEMBERS;
        }
        if (user.hasRole(Roles.EMPLOYEE)) {
            return SELF;
        }
        return NONE;
    }

    /**
     * "Sentinel" UUID used in IN-list predicates that must match no rows.
     * uuidv7 starts with the time portion, so a fixed sentinel like the
     * nil UUID is safe — uuidv7() never returns 00000000-0000-0000-0000-000000000000.
     */
    public static final UUID NEVER_MATCH_ID = new UUID(0L, 0L);
}
