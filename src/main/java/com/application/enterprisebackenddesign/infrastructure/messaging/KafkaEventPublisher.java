package com.application.enterprisebackenddesign.infrastructure.messaging;

import com.application.enterprisebackenddesign.config.KafkaConfig;
import com.application.enterprisebackenddesign.domain.shared.DomainEvent;
import com.application.enterprisebackenddesign.domain.shared.DomainEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * External event publisher — fan-out from in-process events to Kafka topics.
 *
 * This class is a second event publishing channel alongside Spring's internal
 * ApplicationEventPublisher. The flow is:
 * 1. Use cases publish domain events via DomainEventPublisher interface
 * 2. SpringDomainEventPublisher fires them as ApplicationEvents
 * 3. This listener catches all DomainEvent subtypes and publishes to Kafka
 *
 * The resolveTopic() method maps event types to Kafka topics using an
 * aggregate-based partitioning scheme. This achieves:
 * - Temporal decoupling: The use case transaction completes before Kafka send
 * - At-least-once delivery: Kafka provides persistence and replay
 * - Consumer autonomy: Each microservice subscribes only to its relevant topics
 *
 * Tradeoff: The @EventListener on a generic DomainEvent parameter catches
 * ALL domain events. This is intentional — it's a centralized fan-out point.
 * Individual event handlers use @Async for fire-and-forget side effects.
 */
@Component
public class KafkaEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);

    private final KafkaTemplate<String, DomainEvent> kafkaTemplate;

    public KafkaEventPublisher(KafkaTemplate<String, DomainEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @EventListener
    public void onEvent(DomainEvent event) {
        String topic = resolveTopic(event.getEventType());
        log.info("Publishing event {} to topic {}: {}", event.getEventType(), topic, event);
        kafkaTemplate.send(topic, event.getEventType().name(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish event {} to topic {}", event.getEventType(), topic, ex);
                    } else {
                        log.debug("Successfully published event {} to topic {} at offset {}",
                                event.getEventType(), topic, result.getRecordMetadata().offset());
                    }
                });
    }

    public String resolveTopic(DomainEventType eventType) {
        return switch (eventType) {
            case ORDER_CREATED, ORDER_CONFIRMED, ORDER_CANCELLED, ORDER_BILLED, ORDER_UPDATED ->
                    KafkaConfig.TOPIC_ORDER;
            case INVOICE_CREATED, INVOICE_ISSUED ->
                    KafkaConfig.TOPIC_INVOICE;
            case PAYMENT_PENDING, PAYMENT_COMPLETED, PAYMENT_FAILED ->
                    KafkaConfig.TOPIC_PAYMENT;
            case CUSTOMER_CREATED, CUSTOMER_UPDATED, CUSTOMER_DELETED ->
                    KafkaConfig.TOPIC_CUSTOMER;
            case PRODUCT_CREATED, PRODUCT_UPDATED ->
                    KafkaConfig.TOPIC_PRODUCT;
        };
    }
}
