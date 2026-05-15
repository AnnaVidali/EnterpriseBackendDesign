package com.application.enterprisebackenddesign.application.shared;

import com.application.enterprisebackenddesign.domain.shared.DomainEvent;

/**
 * Port interface for publishing domain events.
 *
 * Hexagonal Architecture (Port): This interface defines how the application
 * layer publishes domain events without knowing the publishing mechanism.
 * The implementation (SpringDomainEventPublisher) is an adapter that fires
 * Spring ApplicationEvents, which in turn are caught by:
 * 1. Async event handlers (same JVM, fire-and-forget side effects)
 * 2. KafkaEventPublisher (external messaging, fan-out to microservices)
 *
 * The domain layer records events via Order.pullEvents(), and the use case
 * layer calls this publisher to dispatch them. This separation means the
 * domain model never depends on Spring, Kafka, or any infrastructure.
 */
public interface DomainEventPublisher {

    void publish(DomainEvent event);
}
