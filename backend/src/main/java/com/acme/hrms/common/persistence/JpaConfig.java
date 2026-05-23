package com.acme.hrms.common.persistence;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Turns on Spring Data JPA auditing. The named bean {@code auditorAware}
 * is provided by {@link SecurityContextAuditorAware}.
 *
 * <p>Living in a dedicated config class (rather than on
 * {@code HrmsApplication}) keeps the entrypoint terse and lets test slices
 * choose to exclude this configuration when they don't need auditing.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaConfig {
}
