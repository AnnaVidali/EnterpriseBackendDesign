package com.application.enterprisebackenddesign.infrastructure.events;

import com.application.enterprisebackenddesign.application.shared.DomainEventPublisher;
import com.application.enterprisebackenddesign.domain.shared.DomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Adapter that publishes domain events through Spring's ApplicationEventPublisher.
 *
 * Interview context: This is the "adapter" for the DomainEventPublisher "port".
 * The domain defines the interface, the infrastructure implements it.
 *
 * By using Spring's built-in event mechanism, we get:
 * 1. Synchronous in-process delivery — the event is published in the same
 *    transaction, so if a @Transactional event handler fails, the entire
 *    transaction rolls back (including the aggregate save).
 * 2. @Async support — handlers can be annotated @Async for fire-and-forget
 *    side effects (email, analytics) that shouldn't block the response.
 * 3. No external dependencies — unlike a message broker, this works in tests
 *    without additional setup.
 *
 * For cross-service communication, we could extend this to also publish to
 * Kafka without changing anything in the domain or application layers —
 * that's the benefit of the port/adapter abstraction.
 */
@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringDomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(DomainEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
