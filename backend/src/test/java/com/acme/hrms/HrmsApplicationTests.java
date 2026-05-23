package com.acme.hrms;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Smoke test: the Spring context boots, Flyway runs against PostgreSQL 18,
 * and the audit_log parent partitioned table exists with at least the seed
 * partitions created by V1.
 */
class HrmsApplicationTests extends AbstractIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void contextLoads() {
        assertThat(dataSource).isNotNull();
    }

    @Test
    void flywayCreatesPartitionedAuditLog() {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        Integer parentExists = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM pg_class c
                JOIN pg_namespace n ON n.oid = c.relnamespace
                WHERE c.relname = 'audit_log'
                  AND n.nspname = 'public'
                  AND c.relkind = 'p'
                """,
                Integer.class);
        assertThat(parentExists).as("audit_log should be a partitioned table").isEqualTo(1);

        Integer partitionCount = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM pg_inherits i
                JOIN pg_class parent ON parent.oid = i.inhparent
                WHERE parent.relname = 'audit_log'
                """,
                Integer.class);
        // V1 seeds 14 partitions (last month + current + 12 future).
        assertThat(partitionCount).as("audit_log should have 14 monthly partitions").isEqualTo(14);
    }
}
