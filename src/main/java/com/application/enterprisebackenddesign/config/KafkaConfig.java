package com.application.enterprisebackenddesign.config;

import com.application.enterprisebackenddesign.domain.shared.DomainEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.List;
import java.util.Map;

/**
 * Kafka messaging infrastructure configuration.
 *
 * Hexagonal Architecture (infrastructure layer): This config sets up the
 * external messaging channel for domain events. Each aggregate type gets
 * its own topic (order-events, invoice-events, etc.), allowing downstream
 * consumers to subscribe only to what they need.
 *
 * The topics, partitions, and replication factors are configurable via
 * application properties (app.kafka.topics.*). In production, the
 * replication factor should match the Kafka cluster size for fault tolerance.
 *
 * Integration with the port/adapter pattern: The KafkaTemplate bean created
 * here is used by KafkaEventPublisher, which implements the "publish events
 * to external systems" concern. The domain events flow: use case →
 * DomainEventPublisher → SpringApplicationEvent → KafkaEventPublisher → Kafka.
 */
@Configuration
public class KafkaConfig {

    public static final String TOPIC_ORDER = "order-events";
    public static final String TOPIC_INVOICE = "invoice-events";
    public static final String TOPIC_PAYMENT = "payment-events";
    public static final String TOPIC_CUSTOMER = "customer-events";
    public static final String TOPIC_PRODUCT = "product-events";

    @Value("${app.kafka.topics.partitions:1}")
    private int partitions;

    @Value("${app.kafka.topics.replication-factor:1}")
    private short replicationFactor;

    @Bean
    public ProducerFactory<String, DomainEvent> producerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = kafkaProperties.buildProducerProperties();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, DomainEvent> kafkaTemplate(ProducerFactory<String, DomainEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public List<NewTopic> domainEventTopics() {
        return List.of(
                TopicBuilder.name(TOPIC_ORDER).partitions(partitions).replicas(replicationFactor).build(),
                TopicBuilder.name(TOPIC_INVOICE).partitions(partitions).replicas(replicationFactor).build(),
                TopicBuilder.name(TOPIC_PAYMENT).partitions(partitions).replicas(replicationFactor).build(),
                TopicBuilder.name(TOPIC_CUSTOMER).partitions(partitions).replicas(replicationFactor).build(),
                TopicBuilder.name(TOPIC_PRODUCT).partitions(partitions).replicas(replicationFactor).build()
        );
    }
}
