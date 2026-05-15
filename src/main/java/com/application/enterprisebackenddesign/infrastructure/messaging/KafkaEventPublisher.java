package com.application.enterprisebackenddesign.infrastructure.messaging;

import com.application.enterprisebackenddesign.config.KafkaConfig;
import com.application.enterprisebackenddesign.domain.shared.DomainEvent;
import com.application.enterprisebackenddesign.domain.shared.DomainEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

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
