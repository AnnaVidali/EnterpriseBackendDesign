package com.application.enterprisebackenddesign.domain.payment;

import com.application.enterprisebackenddesign.domain.shared.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentTest {

    private static final Currency USD = Currency.getInstance("USD");

    private Money amount(double value) throws DomainException {
        return new Money(BigDecimal.valueOf(value), USD);
    }

    @Test
    void shouldCreatePendingPayment() throws DomainException {
        Payment payment = new Payment(1L, 10L, 100L, 1L, amount(50.00), PaymentStatus.PENDING);
        assertThat(payment.getId()).isEqualTo(1L);
        assertThat(payment.getInvoiceId()).isEqualTo(10L);
        assertThat(payment.getOrderId()).isEqualTo(100L);
        assertThat(payment.getCustomerId()).isEqualTo(1L);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(payment.getPaymentDate()).isNull();
    }

    @Test
    void shouldRejectNullId() {
        assertThatThrownBy(() -> new Payment(null, 10L, 100L, 1L, amount(50.00), PaymentStatus.PENDING))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Id cannot be null");
    }

    @Test
    void shouldRejectNullInvoiceId() {
        assertThatThrownBy(() -> new Payment(1L, null, 100L, 1L, amount(50.00), PaymentStatus.PENDING))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Invoice id cannot be null");
    }

    @Test
    void shouldRejectNullOrderId() {
        assertThatThrownBy(() -> new Payment(1L, 10L, null, 1L, amount(50.00), PaymentStatus.PENDING))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Order id cannot be null");
    }

    @Test
    void shouldRejectNullCustomerId() {
        assertThatThrownBy(() -> new Payment(1L, 10L, 100L, null, amount(50.00), PaymentStatus.PENDING))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Customer id cannot be null");
    }

    @Test
    void shouldRejectNullAmount() {
        assertThatThrownBy(() -> new Payment(1L, 10L, 100L, 1L, null, PaymentStatus.PENDING))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Amount must be greater than zero");
    }

    @Test
    void shouldRejectZeroAmount() {
        assertThatThrownBy(() -> new Payment(1L, 10L, 100L, 1L, Money.zero(USD), PaymentStatus.PENDING))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Amount must be greater than zero");
    }

    @Test
    void shouldRejectNullStatus() {
        assertThatThrownBy(() -> new Payment(1L, 10L, 100L, 1L, amount(50.00), null))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Status cannot be null");
    }

    @Test
    void shouldEmitPaymentPendingEvent() throws DomainException {
        Payment payment = new Payment(1L, 10L, 100L, 1L, amount(50.00), PaymentStatus.PENDING);
        List<DomainEvent> events = payment.pullEvents(false);
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(PaymentPendingEvent.class);
        PaymentPendingEvent event = (PaymentPendingEvent) events.get(0);
        assertThat(event.getPaymentId()).isEqualTo(1L);
        assertThat(event.getInvoiceId()).isEqualTo(10L);
    }

    @Test
    void shouldCompletePendingPayment() throws DomainException {
        Payment payment = new Payment(1L, 10L, 100L, 1L, amount(50.00), PaymentStatus.PENDING);
        payment.pullEvents(true);
        payment.complete();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(payment.getPaymentDate()).isNotNull();
    }

    @Test
    void shouldEmitPaymentCompletedEvent() throws DomainException {
        Payment payment = new Payment(1L, 10L, 100L, 1L, amount(50.00), PaymentStatus.PENDING);
        payment.pullEvents(true);
        payment.complete();
        List<DomainEvent> events = payment.pullEvents(false);
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(PaymentCompletedEvent.class);
        PaymentCompletedEvent event = (PaymentCompletedEvent) events.get(0);
        assertThat(event.getPaymentId()).isEqualTo(1L);
    }

    @Test
    void shouldRejectCompleteAlreadyCompletedPayment() throws DomainException {
        Payment payment = new Payment(1L, 10L, 100L, 1L, amount(50.00), PaymentStatus.PENDING);
        payment.complete();
        assertThatThrownBy(payment::complete)
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("already COMPLETED");
    }

    @Test
    void shouldRejectCompleteFailedPayment() throws DomainException {
        Payment payment = new Payment(1L, 10L, 100L, 1L, amount(50.00), PaymentStatus.PENDING);
        payment.fail("Insufficient funds");
        assertThatThrownBy(payment::complete)
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Status must be PENDING");
    }

    @Test
    void shouldFailPendingPayment() throws DomainException {
        Payment payment = new Payment(1L, 10L, 100L, 1L, amount(50.00), PaymentStatus.PENDING);
        payment.pullEvents(true);
        payment.fail("Insufficient funds");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void shouldEmitPaymentFailedEvent() throws DomainException {
        Payment payment = new Payment(1L, 10L, 100L, 1L, amount(50.00), PaymentStatus.PENDING);
        payment.pullEvents(true);
        payment.fail("Card declined");
        List<DomainEvent> events = payment.pullEvents(false);
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(PaymentFailedEvent.class);
        PaymentFailedEvent event = (PaymentFailedEvent) events.get(0);
        assertThat(event.getReason()).isEqualTo("Card declined");
    }

    @Test
    void shouldRejectFailNonPendingPayment() throws DomainException {
        Payment payment = new Payment(1L, 10L, 100L, 1L, amount(50.00), PaymentStatus.PENDING);
        payment.complete();
        assertThatThrownBy(() -> payment.fail("reason"))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Status must be PENDING");
    }

    @Test
    void shouldMatchInvoiceWithSameAmountAndCurrency() throws DomainException {
        Payment payment = new Payment(1L, 10L, 100L, 1L, amount(50.00), PaymentStatus.PENDING);
        assertThat(payment.matchesInvoice(amount(50.00))).isTrue();
    }

    @Test
    void shouldNotMatchInvoiceWithDifferentAmount() throws DomainException {
        Payment payment = new Payment(1L, 10L, 100L, 1L, amount(50.00), PaymentStatus.PENDING);
        assertThat(payment.matchesInvoice(amount(30.00))).isFalse();
    }

    @Test
    void shouldNotMatchInvoiceWithDifferentCurrency() throws DomainException {
        Currency EUR = Currency.getInstance("EUR");
        Payment payment = new Payment(1L, 10L, 100L, 1L, amount(50.00), PaymentStatus.PENDING);
        Money eurAmount = new Money(new BigDecimal("50.00"), EUR);
        assertThat(payment.matchesInvoice(eurAmount)).isFalse();
    }

    @Test
    void shouldClearEvents() throws DomainException {
        Payment payment = new Payment(1L, 10L, 100L, 1L, amount(50.00), PaymentStatus.PENDING);
        assertThat(payment.pullEvents(false)).isNotEmpty();
        payment.clearEvents();
        assertThat(payment.pullEvents(false)).isEmpty();
    }
}
