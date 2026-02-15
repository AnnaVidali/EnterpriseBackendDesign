package com.application.enterprisebackenddesign.domain.shared;

import lombok.Getter;

import java.time.Instant;

@Getter
public abstract class DomainEvent {

    // Timestamp for events
    private final Instant occurredAt = Instant.now();

}
