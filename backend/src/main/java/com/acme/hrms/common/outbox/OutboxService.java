package com.acme.hrms.common.outbox;

public interface OutboxService {
    void stageEvent(String eventType, Object payload);
}
