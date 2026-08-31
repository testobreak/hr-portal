package com.acme.hrms;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
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

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18"))
                    .withDatabaseName("hrms")
                    .withUsername("hrms")
                    .withPassword("hrms_test");

    static {
        POSTGRES.start();
    }

    @BeforeEach
    public void cleanDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE workflow_definition CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE announcement_acknowledgment CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE announcement_delivery CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE announcement_audience_rule CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE announcement CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE document_category CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE leave_accrual_run_item CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE leave_accrual_run CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE leave_request_day CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE leave_request CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE leave_ledger_entry CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE employee_work_schedule CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE work_schedule_day CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE work_schedule CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE holiday CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE holiday_calendar CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE leave_policy_assignment CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE leave_policy_version CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE leave_policy CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE leave_type CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE manager_delegation CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE manager_hierarchy_projection CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE employee_certification CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE employee_skill CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE employee_experience CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE employee_education CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE employee_dependent CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE employee_emergency_contact CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE profile_change_request CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE profile_field_definition CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE allocation CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE project CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE client CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE salary_history CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE employee_document CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE employee_assignment_history CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE approval_request CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE outbox_event CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE employee CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE department CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE designation CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE location CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE legal_entity CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE audit_log CASCADE");
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

