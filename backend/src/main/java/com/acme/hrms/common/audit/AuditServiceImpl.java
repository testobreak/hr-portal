package com.acme.hrms.common.audit;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.acme.hrms.common.security.CurrentUser;
import com.acme.hrms.common.web.RequestIdFilter;

/**
 * Default {@link AuditService} implementation.
 *
 * <p>Each call resolves the actor from the security context and the request
 * id / IP from the current servlet request, and writes a single row into
 * {@code audit_log}. The write happens in a fresh
 * {@link Propagation#REQUIRES_NEW} transaction so that an audit insert
 * never piggy-backs on a business transaction that might roll back.
 *
 * <p>Failures are logged but never thrown — losing an audit row is bad,
 * losing the originating business response (or worse, leaking a 500 to the
 * caller because of an audit hiccup) is worse.
 */
@Service
public class AuditServiceImpl implements AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditServiceImpl.class);

    private final AuditLogRepository repository;
    private final Clock clock;

    public AuditServiceImpl(AuditLogRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(AuditEvent event) {
        if (event == null || event.action() == null || event.entity() == null) {
            log.warn("audit.record called with incomplete event: {}", event);
            return;
        }
        try {
            Optional<CurrentUser> actor = CurrentUser.fromSecurityContext();
            UUID tenantId = com.acme.hrms.common.tenant.TenantContext.getTenantId();
            if (tenantId == null) {
                tenantId = UUID.fromString("00000000-0000-0000-0000-000000000000");
            }
            AuditLog row = AuditLog.builder()
                    .at(Instant.now(clock))
                    .actorId(actor.map(CurrentUser::subjectUuid).orElse(null))
                    .actorLabel(actor.map(this::labelFor).orElse(null))
                    .action(event.action())
                    .entity(event.entity())
                    .entityId(event.entityId())
                    .requestId(MDC.get(RequestIdFilter.MDC_KEY))
                    .ip(currentIp())
                    .beforeJson(event.beforeJson())
                    .afterJson(event.afterJson())
                    .detail(event.detail())
                    .tenantId(tenantId)
                    .build();
            repository.save(row);
        } catch (Exception ex) {
            log.error("Failed to persist audit row for action={} entity={} id={}",
                    event.action(), event.entity(), event.entityId(), ex);
        }
    }

    private String labelFor(CurrentUser u) {
        if (u.email() != null && !u.email().isBlank()) {
            return u.email();
        }
        if (u.username() != null && !u.username().isBlank()) {
            return u.username();
        }
        return u.subjectUuid() != null ? u.subjectUuid().toString() : null;
    }

    private static String currentIp() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return null;
            }
            HttpServletRequest req = attrs.getRequest();
            // Prefer X-Forwarded-For when running behind a known reverse proxy;
            // for now we trust the socket peer because compose deploys the API
            // directly on the host.
            String fwd = req.getHeader("X-Forwarded-For");
            if (fwd != null && !fwd.isBlank()) {
                return fwd.split(",")[0].trim();
            }
            return req.getRemoteAddr();
        } catch (Exception ex) {
            return null;
        }
    }
}
