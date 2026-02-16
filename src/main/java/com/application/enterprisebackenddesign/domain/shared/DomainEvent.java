package com.application.enterprisebackenddesign.domain.shared;

import lombok.Getter;

import java.time.Instant;

@Getter
public abstract class DomainEvent {

    private final Instant occurredAt = Instant.now();
    private final DomainEventType eventType;

    protected DomainEvent(DomainEventType eventType) {
        this.eventType = eventType;
    }

}
