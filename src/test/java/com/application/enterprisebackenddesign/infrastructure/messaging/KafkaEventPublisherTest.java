package com.application.enterprisebackenddesign.infrastructure.messaging;

import com.application.enterprisebackenddesign.config.KafkaConfig;
import com.application.enterprisebackenddesign.domain.shared.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaEventPublisherTest {

    @Mock
    private KafkaTemplate<String, DomainEvent> kafkaTemplate;

    @Captor
    private ArgumentCaptor<DomainEvent> eventCaptor;

    private KafkaEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new KafkaEventPublisher(kafkaTemplate);
    }

    @Test
    void shouldPublishOrderCreatedEventToOrderTopic() {
        DomainEvent event = new OrderCreatedEvent(1L, 10L, 3);
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(CompletableFuture.completedFuture(null));

        publisher.onEvent(event);

        verify(kafkaTemplate).send(eq(KafkaConfig.TOPIC_ORDER), eq(DomainEventType.ORDER_CREATED.name()), eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOf(OrderCreatedEvent.class);
    }

    @Test
    void shouldPublishOrderConfirmedEventToOrderTopic() {
        DomainEvent event = new OrderConfirmedEvent(1L, 10L);
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(CompletableFuture.completedFuture(null));

        publisher.onEvent(event);

        verify(kafkaTemplate).send(eq(KafkaConfig.TOPIC_ORDER), eq(DomainEventType.ORDER_CONFIRMED.name()), any());
    }

    @Test
    void shouldPublishInvoiceIssuedEventToInvoiceTopic() throws DomainException.BusinessRuleViolationException {
        Money money = new Money(BigDecimal.valueOf(150), Currency.getInstance("EUR"));
        DomainEvent event = new InvoiceIssuedEvent(1L, 2L, 10L, money);
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(CompletableFuture.completedFuture(null));

        publisher.onEvent(event);

        verify(kafkaTemplate).send(eq(KafkaConfig.TOPIC_INVOICE), eq(DomainEventType.INVOICE_ISSUED.name()), any());
    }

    @Test
    void shouldPublishPaymentCompletedEventToPaymentTopic() throws DomainException.BusinessRuleViolationException {
        Money money = new Money(BigDecimal.valueOf(150), Currency.getInstance("EUR"));
        DomainEvent event = new PaymentCompletedEvent(1L, 2L, 3L, 10L, money);
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(CompletableFuture.completedFuture(null));

        publisher.onEvent(event);

        verify(kafkaTemplate).send(eq(KafkaConfig.TOPIC_PAYMENT), eq(DomainEventType.PAYMENT_COMPLETED.name()), any());
    }

    @Test
    void shouldPublishCustomerCreatedEventToCustomerTopic() {
        DomainEvent event = new CustomerCreatedEvent(1L, "John", "Doe", "john@example.com");
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(CompletableFuture.completedFuture(null));

        publisher.onEvent(event);

        verify(kafkaTemplate).send(eq(KafkaConfig.TOPIC_CUSTOMER), eq(DomainEventType.CUSTOMER_CREATED.name()), any());
    }

    @Test
    void shouldPublishProductCreatedEventToProductTopic() throws DomainException.BusinessRuleViolationException {
        Money money = new Money(BigDecimal.valueOf(29.99), Currency.getInstance("USD"));
        DomainEvent event = new ProductCreatedEvent(1L, "Widget", money, "WID-001");
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(CompletableFuture.completedFuture(null));

        publisher.onEvent(event);

        verify(kafkaTemplate).send(eq(KafkaConfig.TOPIC_PRODUCT), eq(DomainEventType.PRODUCT_CREATED.name()), any());
    }

    @Test
    void shouldHandleKafkaFailureGracefully() {
        DomainEvent event = new OrderCreatedEvent(1L, 10L, 3);
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(CompletableFuture.failedFuture(new RuntimeException("Kafka down")));

        publisher.onEvent(event);

        verify(kafkaTemplate).send(any(), any(), any());
    }

    @Test
    void resolveTopicShouldReturnCorrectTopicForEachEventType() {
        assertThat(publisher.resolveTopic(DomainEventType.ORDER_CREATED)).isEqualTo(KafkaConfig.TOPIC_ORDER);
        assertThat(publisher.resolveTopic(DomainEventType.ORDER_CONFIRMED)).isEqualTo(KafkaConfig.TOPIC_ORDER);
        assertThat(publisher.resolveTopic(DomainEventType.ORDER_CANCELLED)).isEqualTo(KafkaConfig.TOPIC_ORDER);
        assertThat(publisher.resolveTopic(DomainEventType.ORDER_BILLED)).isEqualTo(KafkaConfig.TOPIC_ORDER);
        assertThat(publisher.resolveTopic(DomainEventType.ORDER_UPDATED)).isEqualTo(KafkaConfig.TOPIC_ORDER);

        assertThat(publisher.resolveTopic(DomainEventType.INVOICE_CREATED)).isEqualTo(KafkaConfig.TOPIC_INVOICE);
        assertThat(publisher.resolveTopic(DomainEventType.INVOICE_ISSUED)).isEqualTo(KafkaConfig.TOPIC_INVOICE);

        assertThat(publisher.resolveTopic(DomainEventType.PAYMENT_PENDING)).isEqualTo(KafkaConfig.TOPIC_PAYMENT);
        assertThat(publisher.resolveTopic(DomainEventType.PAYMENT_COMPLETED)).isEqualTo(KafkaConfig.TOPIC_PAYMENT);
        assertThat(publisher.resolveTopic(DomainEventType.PAYMENT_FAILED)).isEqualTo(KafkaConfig.TOPIC_PAYMENT);

        assertThat(publisher.resolveTopic(DomainEventType.CUSTOMER_CREATED)).isEqualTo(KafkaConfig.TOPIC_CUSTOMER);
        assertThat(publisher.resolveTopic(DomainEventType.CUSTOMER_UPDATED)).isEqualTo(KafkaConfig.TOPIC_CUSTOMER);
        assertThat(publisher.resolveTopic(DomainEventType.CUSTOMER_DELETED)).isEqualTo(KafkaConfig.TOPIC_CUSTOMER);

        assertThat(publisher.resolveTopic(DomainEventType.PRODUCT_CREATED)).isEqualTo(KafkaConfig.TOPIC_PRODUCT);
        assertThat(publisher.resolveTopic(DomainEventType.PRODUCT_UPDATED)).isEqualTo(KafkaConfig.TOPIC_PRODUCT);
    }
}
