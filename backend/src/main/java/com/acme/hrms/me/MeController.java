package com.acme.hrms.me;

import java.util.List;
import java.util.TreeSet;

import org.slf4j.MDC;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.acme.hrms.common.audit.AuditAction;
import com.acme.hrms.common.audit.AuditEvent;
import com.acme.hrms.common.audit.AuditService;
import com.acme.hrms.common.security.CurrentUser;
import com.acme.hrms.common.web.RequestIdFilter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Identity probe.
 *
 * <p>Used by the SPA right after login to render the current user's name +
 * roles in the navbar without needing to decode the token client-side.
 * Also doubles as a smoke-test endpoint: if {@code /api/me} returns 200
 * with the right roles, auth wiring works.
 *
 * <p>Each successful call writes one {@link AuditAction#LOGIN} row, which
 * is enough to demonstrate the audit pipeline end-to-end without lighting
 * up audit on every request (we'll do that explicitly per feature later).
 */
@RestController
@RequestMapping("/api/me")
@Tag(name = "Me", description = "Current user identity")
public class MeController {

    private final AuditService auditService;

    public MeController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    @Operation(summary = "Return the identity and realm roles of the caller")
    public MeResponse me(@AuthenticationPrincipal Jwt jwt) {
        CurrentUser user = CurrentUser.from(jwt);
        List<String> sortedRoles = List.copyOf(new TreeSet<>(user.roles()));

        auditService.record(
                AuditEvent.of(AuditAction.LOGIN, "session")
                        .withDetail("GET /api/me"));

        return new MeResponse(
                user.subjectUuid(),
                user.username(),
                user.email(),
                sortedRoles,
                MDC.get(RequestIdFilter.MDC_KEY));
    }
}
