package com.acme.hrms.common.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import com.acme.hrms.common.security.CurrentUser;

/**
 * Provides the actor UUID for {@code @CreatedBy} / {@code @LastModifiedBy}.
 *
 * <p>For requests with a JWT, returns the Keycloak subject UUID from the
 * security context. For unauthenticated paths or background jobs (no
 * security context yet, but coming in later phases) returns
 * {@link Optional#empty()} — the column is nullable on purpose so we can
 * recognise rows written outside an HTTP context.
 */
@Component("auditorAware")
public class SecurityContextAuditorAware implements AuditorAware<UUID> {

    @Override
    public Optional<UUID> getCurrentAuditor() {
        return CurrentUser.fromSecurityContext()
                .map(CurrentUser::subjectUuid)
                .filter(uuid -> uuid != null);
    }
}
