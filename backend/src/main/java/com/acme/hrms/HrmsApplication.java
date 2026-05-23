package com.acme.hrms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entrypoint.
 *
 * <p>JPA auditing ({@code @EnableJpaAuditing}) and async/scheduling
 * ({@code @EnableScheduling}, {@code @EnableAsync}) are deliberately not
 * turned on yet. They'll be added with their first consumer in later phases
 * (BaseEntity in Phase 2, scheduler jobs in Phase 7+).
 */
@SpringBootApplication
public class HrmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(HrmsApplication.class, args);
    }
}
