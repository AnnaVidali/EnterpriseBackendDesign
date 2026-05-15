package com.application.enterprisebackenddesign.domain.order;

import com.application.enterprisebackenddesign.domain.shared.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private static final Currency USD = Currency.getInstance("USD");

    private Money price(double amount) throws DomainException {
        return new Money(BigDecimal.valueOf(amount), USD);
    }

    private OrderLine line(Long id, int qty, double price) throws DomainException {
        return new OrderLine(id, 100L + id, qty, price(price));
    }

    @Test
    void shouldCreateOrderWithPendingStatus() throws DomainException {
        List<OrderLine> lines = List.of(line(1L, 2, 10.00));
        Order order = new Order(1L, 1L, lines, USD);
        assertThat(order.getId()).isEqualTo(1L);
        assertThat(order.getCustomerId()).isEqualTo(1L);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getCurrency()).isEqualTo(USD);
        assertThat(order.getOrderLines()).hasSize(1);
    }

    @Test
    void shouldCalculateTotalOnCreation() throws DomainException {
        List<OrderLine> lines = List.of(line(1L, 2, 10.00), line(2L, 3, 5.00));
        Order order = new Order(1L, 1L, lines, USD);
        assertThat(order.getTotalAmount().getAmount()).isEqualByComparingTo(new BigDecimal("35.00"));
    }

    @Test
    void shouldRejectNullId() {
        assertThatThrownBy(() -> new Order(null, 1L, List.of(line(1L, 1, 10.00)), USD))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Id cannot be null");
    }

    @Test
    void shouldRejectNullCustomerId() {
        assertThatThrownBy(() -> new Order(1L, null, List.of(line(1L, 1, 10.00)), USD))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Customer id cannot be null");
    }

    @Test
    void shouldRejectNullOrderLines() {
        assertThatThrownBy(() -> new Order(1L, 1L, null, USD))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Order line id cannot be null");
    }

    @Test
    void shouldRejectNullCurrency() throws DomainException {
        assertThatThrownBy(() -> new Order(1L, 1L, List.of(line(1L, 1, 10.00)), null))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Currency cannot be null");
    }

    @Test
    void shouldEmitOrderCreatedEvent() throws DomainException {
        Order order = new Order(1L, 1L, List.of(line(1L, 2, 10.00)), USD);
        List<DomainEvent> events = order.pullEvents(false);
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(OrderCreatedEvent.class);
        OrderCreatedEvent event = (OrderCreatedEvent) events.get(0);
        assertThat(event.getOrderId()).isEqualTo(1L);
        assertThat(event.getCustomerId()).isEqualTo(1L);
    }

    @Test
    void shouldClearEventsWhenPulledWithClear() throws DomainException {
        Order order = new Order(1L, 1L, List.of(line(1L, 2, 10.00)), USD);
        order.pullEvents(true);
        assertThat(order.pullEvents(false)).isEmpty();
    }

    @Test
    void shouldAddLineToCreatedOrder() throws DomainException {
        Order order = new Order(1L, 1L, List.of(line(1L, 2, 10.00)), USD);
        order.pullEvents(true);
        order.addLine(line(2L, 3, 5.00));
        assertThat(order.getOrderLines()).hasSize(2);
        assertThat(order.getTotalAmount().getAmount()).isEqualByComparingTo(new BigDecimal("35.00"));
    }

    @Test
    void shouldEmitOrderLineUpdatedEventOnAddLine() throws DomainException {
        Order order = new Order(1L, 1L, List.of(line(1L, 2, 10.00)), USD);
        order.pullEvents(true);
        order.addLine(line(2L, 3, 5.00));
        List<DomainEvent> events = order.pullEvents(false);
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(OrderLineUpdatedEvent.class);
    }

    @Test
    void shouldRejectAddNullLine() throws DomainException {
        Order order = new Order(1L, 1L, List.of(line(1L, 2, 10.00)), USD);
        assertThatThrownBy(() -> order.addLine(null))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Order line cannot be null");
    }

    @Test
    void shouldRejectAddDuplicateLine() throws DomainException {
        OrderLine line = line(1L, 2, 10.00);
        Order order = new Order(1L, 1L, List.of(line), USD);
        assertThatThrownBy(() -> order.addLine(line))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Order line already exists");
    }

    @Test
    void shouldRejectAddLineWithDifferentCurrency() throws DomainException {
        Currency EUR = Currency.getInstance("EUR");
        OrderLine eurLine = new OrderLine(2L, 101L, 1, new Money(BigDecimal.TEN, EUR));
        Order order = new Order(1L, 1L, List.of(line(1L, 2, 10.00)), USD);
        assertThatThrownBy(() -> order.addLine(eurLine))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("currency");
    }

    @Test
    void shouldRemoveLineFromCreatedOrder() throws DomainException {
        OrderLine line = line(1L, 2, 10.00);
        Order order = new Order(1L, 1L, List.of(line), USD);
        order.pullEvents(true);
        order.removeLine(line);
        assertThat(order.getOrderLines()).isEmpty();
        assertThat(order.getTotalAmount().getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldRejectRemoveNullLine() throws DomainException {
        Order order = new Order(1L, 1L, List.of(line(1L, 2, 10.00)), USD);
        assertThatThrownBy(() -> order.removeLine(null))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Order line cannot be null");
    }

    @Test
    void shouldRejectRemoveNonExistentLine() throws DomainException {
        Order order = new Order(1L, 1L, List.of(line(1L, 2, 10.00)), USD);
        OrderLine otherLine = line(2L, 3, 5.00);
        assertThatThrownBy(() -> order.removeLine(otherLine))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Order line does not exist");
    }

    @Test
    void shouldConfirmOrder() throws DomainException {
        Order order = new Order(1L, 1L, List.of(line(1L, 2, 10.00)), USD);
        order.pullEvents(true);
        order.confirmOrder();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void shouldEmitOrderConfirmedEvent() throws DomainException {
        Order order = new Order(1L, 1L, List.of(line(1L, 2, 10.00)), USD);
        order.pullEvents(true);
        order.confirmOrder();
        List<DomainEvent> events = order.pullEvents(false);
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(OrderConfirmedEvent.class);
    }

    @Test
    void shouldRejectConfirmEmptyOrder() throws DomainException {
        Order order = new Order(1L, 1L, List.of(), USD);
        assertThatThrownBy(order::confirmOrder)
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Cannot confirm");
    }

    @Test
    void shouldRejectConfirmAlreadyConfirmedOrder() throws DomainException {
        Order order = new Order(1L, 1L, List.of(line(1L, 2, 10.00)), USD);
        order.confirmOrder();
        assertThatThrownBy(order::confirmOrder)
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Cannot confirm");
    }

    @Test
    void shouldCancelCreatedOrder() throws DomainException {
        Order order = new Order(1L, 1L, List.of(line(1L, 2, 10.00)), USD);
        order.pullEvents(true);
        order.cancelOrder();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void shouldCancelConfirmedOrder() throws DomainException {
        Order order = new Order(1L, 1L, List.of(line(1L, 2, 10.00)), USD);
        order.confirmOrder();
        order.pullEvents(true);
        order.cancelOrder();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void shouldEmitOrderCancelledEvent() throws DomainException {
        Order order = new Order(1L, 1L, List.of(line(1L, 2, 10.00)), USD);
        order.pullEvents(true);
        order.cancelOrder();
        List<DomainEvent> events = order.pullEvents(false);
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(OrderCancelledEvent.class);
    }

    @Test
    void shouldRejectCancelCancelledOrder() throws DomainException {
        Order order = new Order(1L, 1L, List.of(line(1L, 2, 10.00)), USD);
        order.cancelOrder();
        assertThatThrownBy(order::cancelOrder)
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Cannot cancel");
    }

    @Test
    void shouldUpdateOrderLineQuantity() throws DomainException {
        OrderLine line = line(1L, 2, 10.00);
        Order order = new Order(1L, 1L, List.of(line), USD);
        order.pullEvents(true);
        order.updateOrderLines(line, 5);
        assertThat(order.getOrderLines().get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    void shouldRemoveLineWhenQuantityIsZero() throws DomainException {
        OrderLine line = line(1L, 2, 10.00);
        Order order = new Order(1L, 1L, List.of(line), USD);
        order.updateOrderLines(line, 0);
        assertThat(order.getOrderLines()).isEmpty();
    }

    @Test
    void shouldRejectUpdateWithNegativeQuantity() throws DomainException {
        OrderLine line = line(1L, 2, 10.00);
        Order order = new Order(1L, 1L, List.of(line), USD);
        assertThatThrownBy(() -> order.updateOrderLines(line, -1))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("New quantity cannot be negative");
    }

    @Test
    void shouldRejectUpdateNonExistentLine() throws DomainException {
        Order order = new Order(1L, 1L, List.of(line(1L, 2, 10.00)), USD);
        OrderLine other = line(2L, 3, 5.00);
        assertThatThrownBy(() -> order.updateOrderLines(other, 5))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("does not exist");
    }

    @Test
    void shouldRejectUpdateLineOnConfirmedOrder() throws DomainException {
        OrderLine line = line(1L, 2, 10.00);
        Order order = new Order(1L, 1L, List.of(line), USD);
        order.confirmOrder();
        assertThatThrownBy(() -> order.updateOrderLines(line, 5))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Cannot update");
    }

    @Test
    void shouldRejectAddLineToConfirmedOrder() throws DomainException {
        Order order = new Order(1L, 1L, List.of(line(1L, 2, 10.00)), USD);
        order.confirmOrder();
        assertThatThrownBy(() -> order.addLine(line(2L, 3, 5.00)))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Cannot add line");
    }

    @Test
    void shouldRejectRemoveLineFromConfirmedOrder() throws DomainException {
        OrderLine line = line(1L, 2, 10.00);
        Order order = new Order(1L, 1L, List.of(line), USD);
        order.confirmOrder();
        assertThatThrownBy(() -> order.removeLine(line))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Cannot remove line");
    }

    @Test
    void shouldCheckIfEmpty() throws DomainException {
        Order empty = new Order(1L, 1L, List.of(), USD);
        assertThat(empty.isEmpty()).isTrue();
        Order notEmpty = new Order(2L, 1L, List.of(line(1L, 1, 10.00)), USD);
        assertThat(notEmpty.isEmpty()).isFalse();
    }

    @Test
    void shouldEmitLineUpdatedEventOnUpdateLine() throws DomainException {
        OrderLine line = line(1L, 2, 10.00);
        Order order = new Order(1L, 1L, List.of(line), USD);
        order.pullEvents(true);
        order.updateOrderLines(line, 5);
        List<DomainEvent> events = order.pullEvents(false);
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(OrderLineUpdatedEvent.class);
        OrderLineUpdatedEvent event = (OrderLineUpdatedEvent) events.get(0);
        assertThat(event.getOldQuantity()).isEqualTo(2);
        assertThat(event.getNewQuantity()).isEqualTo(5);
    }

    @Test
    void shouldCalculateTotalWithMultipleLines() throws DomainException {
        List<OrderLine> lines = List.of(line(1L, 2, 10.00), line(2L, 1, 15.50), line(3L, 3, 5.00));
        Order order = new Order(1L, 1L, lines, USD);
        assertThat(order.getTotalAmount().getAmount()).isEqualByComparingTo(new BigDecimal("50.50"));
    }

    @Test
    void shouldUpdateTotalAfterAddingLine() throws DomainException {
        Order order = new Order(1L, 1L, List.of(line(1L, 2, 10.00)), USD);
        order.addLine(line(2L, 3, 5.00));
        assertThat(order.getTotalAmount().getAmount()).isEqualByComparingTo(new BigDecimal("35.00"));
    }

    @Test
    void shouldUpdateTotalAfterRemovingLine() throws DomainException {
        OrderLine line1 = line(1L, 2, 10.00);
        Order order = new Order(1L, 1L, List.of(line1, line(2L, 3, 5.00)), USD);
        order.removeLine(line1);
        assertThat(order.getTotalAmount().getAmount()).isEqualByComparingTo(new BigDecimal("15.00"));
    }
}
