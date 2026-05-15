package com.application.enterprisebackenddesign.domain.invoice;

import com.application.enterprisebackenddesign.domain.shared.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvoiceTest {

    private static final Currency USD = Currency.getInstance("USD");

    private Money amount(double value) throws DomainException {
        return new Money(BigDecimal.valueOf(value), USD);
    }

    @Test
    void shouldCreateDraftInvoice() throws DomainException {
        Invoice invoice = new Invoice(1L, 1L, 10L, amount(100.00), InvoiceStatus.DRAFT, null);
        assertThat(invoice.getId()).isEqualTo(1L);
        assertThat(invoice.getCustomerId()).isEqualTo(1L);
        assertThat(invoice.getOrderId()).isEqualTo(10L);
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.DRAFT);
        assertThat(invoice.getInvoiceDate()).isNull();
    }

    @Test
    void shouldCreateIssuedInvoiceWithDate() throws DomainException {
        LocalDateTime date = LocalDateTime.now();
        Invoice invoice = new Invoice(1L, 1L, 10L, amount(100.00), InvoiceStatus.ISSUED, date);
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.ISSUED);
        assertThat(invoice.getInvoiceDate()).isEqualTo(date);
    }

    @Test
    void shouldEmitInvoiceCreatedEventOnConstruction() throws DomainException {
        Invoice invoice = new Invoice(1L, 1L, 10L, amount(100.00), InvoiceStatus.DRAFT, null);
        List<DomainEvent> events = invoice.pullEvents(false);
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(InvoiceCreatedEvent.class);
        InvoiceCreatedEvent event = (InvoiceCreatedEvent) events.get(0);
        assertThat(event.getStatus()).isEqualTo(InvoiceStatus.DRAFT);
    }

    @Test
    void shouldRejectNullId() {
        assertThatThrownBy(() -> new Invoice(null, 1L, 10L, amount(100.00), InvoiceStatus.DRAFT, null))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Id cannot be null");
    }

    @Test
    void shouldRejectNullCustomerId() {
        assertThatThrownBy(() -> new Invoice(1L, null, 10L, amount(100.00), InvoiceStatus.DRAFT, null))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Customer id cannot be null");
    }

    @Test
    void shouldRejectNullOrderId() {
        assertThatThrownBy(() -> new Invoice(1L, 1L, null, amount(100.00), InvoiceStatus.DRAFT, null))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Order id cannot be null");
    }

    @Test
    void shouldRejectNullAmount() {
        assertThatThrownBy(() -> new Invoice(1L, 1L, 10L, null, InvoiceStatus.DRAFT, null))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Amount must be greater than zero");
    }

    @Test
    void shouldRejectZeroAmount() {
        assertThatThrownBy(() -> new Invoice(1L, 1L, 10L, Money.zero(USD), InvoiceStatus.DRAFT, null))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Amount must be greater than zero");
    }

    @Test
    void shouldRejectNullStatus() {
        assertThatThrownBy(() -> new Invoice(1L, 1L, 10L, amount(100.00), null, null))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Status cannot be null");
    }

    @Test
    void shouldRejectIssuedStatusWithoutDate() {
        assertThatThrownBy(() -> new Invoice(1L, 1L, 10L, amount(100.00), InvoiceStatus.ISSUED, null))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Issued or paid invoices must have an invoice date");
    }

    @Test
    void shouldRejectPaidStatusWithoutDate() {
        assertThatThrownBy(() -> new Invoice(1L, 1L, 10L, amount(100.00), InvoiceStatus.PAID, null))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Issued or paid invoices must have an invoice date");
    }

    @Test
    void shouldIssueDraftInvoice() throws DomainException {
        Invoice invoice = new Invoice(1L, 1L, 10L, amount(100.00), InvoiceStatus.DRAFT, null);
        invoice.pullEvents(true);
        invoice.issue();
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.ISSUED);
        assertThat(invoice.getInvoiceDate()).isNotNull();
    }

    @Test
    void shouldEmitInvoiceIssuedEvent() throws DomainException {
        Invoice invoice = new Invoice(1L, 1L, 10L, amount(100.00), InvoiceStatus.DRAFT, null);
        invoice.pullEvents(true);
        invoice.issue();
        List<DomainEvent> events = invoice.pullEvents(false);
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(InvoiceIssuedEvent.class);
        InvoiceIssuedEvent event = (InvoiceIssuedEvent) events.get(0);
        assertThat(event.getInvoiceId()).isEqualTo(1L);
    }

    @Test
    void shouldRejectIssueAlreadyIssuedInvoice() throws DomainException {
        Invoice invoice = new Invoice(1L, 1L, 10L, amount(100.00), InvoiceStatus.DRAFT, null);
        invoice.issue();
        assertThatThrownBy(invoice::issue)
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Only DRAFT invoices can be issued");
    }

    @Test
    void shouldRejectIssuePaidInvoice() throws DomainException {
        LocalDateTime date = LocalDateTime.now();
        Invoice invoice = new Invoice(1L, 1L, 10L, amount(100.00), InvoiceStatus.PAID, date);
        assertThatThrownBy(invoice::issue)
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Only DRAFT invoices can be issued");
    }

    @Test
    void shouldMarkIssuedInvoiceAsPaid() throws DomainException {
        Invoice invoice = new Invoice(1L, 1L, 10L, amount(100.00), InvoiceStatus.DRAFT, null);
        invoice.issue();
        invoice.pullEvents(true);
        invoice.markAsPaid();
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
    }

    @Test
    void shouldEmitOrderBilledEventOnMarkAsPaid() throws DomainException {
        Invoice invoice = new Invoice(1L, 1L, 10L, amount(100.00), InvoiceStatus.DRAFT, null);
        invoice.issue();
        invoice.pullEvents(true);
        invoice.markAsPaid();
        List<DomainEvent> events = invoice.pullEvents(false);
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(OrderBilledEvent.class);
    }

    @Test
    void shouldRejectMarkDraftAsPaid() throws DomainException {
        Invoice invoice = new Invoice(1L, 1L, 10L, amount(100.00), InvoiceStatus.DRAFT, null);
        assertThatThrownBy(invoice::markAsPaid)
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Only ISSUED invoices can be marked as PAID");
    }

    @Test
    void shouldCreateFromOrder() throws DomainException {
        Invoice invoice = Invoice.fromOrder(1L, 10L, 1L, amount(200.00));
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.DRAFT);
        assertThat(invoice.getInvoiceDate()).isNull();
        assertThat(invoice.getOrderId()).isEqualTo(10L);
        assertThat(invoice.getCustomerId()).isEqualTo(1L);
    }

    @Test
    void shouldClearEvents() throws DomainException {
        Invoice invoice = new Invoice(1L, 1L, 10L, amount(100.00), InvoiceStatus.DRAFT, null);
        assertThat(invoice.pullEvents(false)).isNotEmpty();
        invoice.clearEvents();
        assertThat(invoice.pullEvents(false)).isEmpty();
    }
}
