package com.application.enterprisebackenddesign.domain.shared;

import lombok.Getter;

import java.time.Instant;

/**
 * Base class for all domain events.
 *
 * Interview context: Domain events are a core DDD tactical pattern. They capture
 * state changes within aggregates, enabling event-driven communication between
 * bounded contexts. Here's why we use them:
 *
 * 1. Decoupling: Aggregates raise events without knowing who consumes them.
 *    The Order aggregate raises OrderCreatedEvent — it doesn't care whether
 *    the analytics service, inventory system, or email service reacts to it.
 *
 * 2. Audit trail: Events record what happened and when (occurredAt timestamp).
 *
 * 3. Side-effect management: Instead of embedding email, CRM, and inventory logic
 *    inside the domain model, we raise events and handle them asynchronously
 *    in the infrastructure layer (see infrastructure/events/handlers/).
 *
 * 4. Event sourcing ready: The event structure (type + timestamp + payload)
 *    is compatible with event sourcing patterns if needed later.
 *
 * Events are published via Spring's ApplicationEventPublisher (synchronous
 * in-process) and consumed by @EventListener handlers marked @Async for
 * non-blocking side effects. For cross-service events, we could bridge to
 * Kafka. The abstraction layer (DomainEventPublisher interface) makes this
 * swappable.
 */
@Getter
public abstract class DomainEvent {

    private final Instant occurredAt = Instant.now();
    private final DomainEventType eventType;

    protected DomainEvent(DomainEventType eventType) {
        this.eventType = eventType;
    }

}
