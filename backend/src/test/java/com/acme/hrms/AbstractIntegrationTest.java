package com.acme.hrms;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Base class for full-stack integration tests.
 *
 * <p>Spins up a real PostgreSQL 18 container per test JVM (singleton — the
 * static initializer keeps the same instance alive across all subclasses)
 * and points Spring's datasource + Flyway at it. JWTs are stubbed via
 * {@code spring-security-test}'s {@code SecurityMockMvcRequestPostProcessors.jwt()}
 * so we don't need a live Keycloak.
 *
 * <p>Requires Docker on the host running the build. CI must have Docker
 * available too — non-negotiable, since PG-specific features
 * ({@code uuidv7()}, partitioning, {@code citext}, {@code jsonb}) cannot be
 * faithfully simulated by H2.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public abstract class AbstractIntegrationTest {

    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18"))
                    .withDatabaseName("hrms")
                    .withUsername("hrms")
                    .withPassword("hrms_test");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.flyway.url", POSTGRES::getJdbcUrl);
        registry.add("spring.flyway.user", POSTGRES::getUsername);
        registry.add("spring.flyway.password", POSTGRES::getPassword);
    }
}
