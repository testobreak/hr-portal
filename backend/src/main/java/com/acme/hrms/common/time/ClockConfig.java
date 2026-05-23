package com.acme.hrms.common.time;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides a system {@link Clock} bean fixed to UTC. All services that need
 * "now" should inject this clock rather than calling {@link java.time.Instant#now()}
 * directly so tests can substitute a {@link Clock#fixed} or {@link Clock#offset}.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
