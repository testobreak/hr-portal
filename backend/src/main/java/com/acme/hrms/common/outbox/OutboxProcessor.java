package com.acme.hrms.common.outbox;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Background worker that relays pending transactional outbox events
 * to application consumers and maintains table hygiene.
 */
@Component
public class OutboxProcessor {

    private static final Logger log = LoggerFactory.getLogger(OutboxProcessor.class);

    private final OutboxEventRepository repository;
    private final ApplicationEventPublisher publisher;

    public OutboxProcessor(OutboxEventRepository repository, ApplicationEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processPendingEvents() {
        List<OutboxEvent> pending = repository.findTop50ByStatusOrderByCreatedAtAsc("PENDING");
        if (pending.isEmpty()) {
            return;
        }

        log.debug("Processing {} pending outbox event(s)", pending.size());
        for (OutboxEvent event : pending) {
            try {
                // Dispatch event internally to any registered listeners (notifications, analytics, audit)
                publisher.publishEvent(new OutboxDispatchedEvent(
                        event.getId(),
                        event.getTenantId(),
                        event.getEventType(),
                        event.getPayload(),
                        event.getCreatedAt()
                ));
                event.setStatus("PROCESSED");
            } catch (Exception ex) {
                log.error("Failed to dispatch outbox event {}: {}", event.getId(), ex.getMessage(), ex);
                event.setStatus("FAILED");
            }
        }
        repository.saveAll(pending);
    }

    /**
     * Purges successfully processed outbox records older than 7 days to prevent table bloat.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void purgeOldProcessedEvents() {
        Instant cutoff = Instant.now().minus(7, ChronoUnit.DAYS);
        int deleted = repository.deleteProcessedOlderThan("PROCESSED", cutoff);
        if (deleted > 0) {
            log.info("Purged {} processed outbox events older than 7 days", deleted);
        }
    }

    public record OutboxDispatchedEvent(
            java.util.UUID id,
            java.util.UUID tenantId,
            String eventType,
            String payload,
            Instant createdAt
    ) {}
}
