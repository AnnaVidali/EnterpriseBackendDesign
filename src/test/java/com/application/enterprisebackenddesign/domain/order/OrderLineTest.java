package com.application.enterprisebackenddesign.domain.order;

import com.application.enterprisebackenddesign.domain.shared.DomainException;
import com.application.enterprisebackenddesign.domain.shared.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderLineTest {

    private static final Currency USD = Currency.getInstance("USD");

    private Money price() throws DomainException {
        return new Money(new BigDecimal("19.99"), USD);
    }

    @Test
    void shouldCreateOrderLine() throws DomainException {
        OrderLine line = new OrderLine(1L, 10L, 2, price());
        assertThat(line.getId()).isEqualTo(1L);
        assertThat(line.getProductId()).isEqualTo(10L);
        assertThat(line.getQuantity()).isEqualTo(2);
        assertThat(line.getPrice()).isNotNull();
    }

    @Test
    void shouldRejectNullId() {
        assertThatThrownBy(() -> new OrderLine(null, 10L, 2, price()))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Id cannot be null");
    }

    @Test
    void shouldRejectNullProductId() {
        assertThatThrownBy(() -> new OrderLine(1L, null, 2, price()))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Product id cannot be null");
    }

    @Test
    void shouldRejectZeroQuantity() {
        assertThatThrownBy(() -> new OrderLine(1L, 10L, 0, price()))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Quantity must be greater than zero");
    }

    @Test
    void shouldRejectNegativeQuantity() {
        assertThatThrownBy(() -> new OrderLine(1L, 10L, -1, price()))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Quantity must be greater than zero");
    }

    @Test
    void shouldRejectNullPrice() {
        assertThatThrownBy(() -> new OrderLine(1L, 10L, 2, null))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Price cannot be null");
    }

    @Test
    void shouldCreateWithQuantity() throws DomainException {
        OrderLine line = new OrderLine(1L, 10L, 2, price());
        OrderLine updated = line.withQuantity(5);
        assertThat(updated.getQuantity()).isEqualTo(5);
        assertThat(updated.getId()).isEqualTo(line.getId());
        assertThat(updated.getProductId()).isEqualTo(line.getProductId());
    }

    @Test
    void shouldRejectWithQuantityZero() throws DomainException {
        OrderLine line = new OrderLine(1L, 10L, 2, price());
        assertThatThrownBy(() -> line.withQuantity(0))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Quantity must be greater than zero");
    }

    @Test
    void shouldRejectWithQuantityNegative() throws DomainException {
        OrderLine line = new OrderLine(1L, 10L, 2, price());
        assertThatThrownBy(() -> line.withQuantity(-1))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Quantity must be greater than zero");
    }

    @Test
    void shouldCalculateSubtotal() throws DomainException {
        OrderLine line = new OrderLine(1L, 10L, 3, new Money(new BigDecimal("10.00"), USD));
        Money subtotal = line.getSubtotal();
        assertThat(subtotal.getAmount()).isEqualByComparingTo(new BigDecimal("30.00"));
    }

    @Test
    void shouldBeEqualById() throws DomainException {
        OrderLine a = new OrderLine(1L, 10L, 2, price());
        OrderLine b = new OrderLine(1L, 20L, 5, price());
        assertThat(a).isEqualTo(b);
    }

    @Test
    void shouldHaveSameHashCodeForSameId() throws DomainException {
        OrderLine a = new OrderLine(1L, 10L, 2, price());
        OrderLine b = new OrderLine(1L, 20L, 5, price());
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void shouldNotBeEqualForDifferentIds() throws DomainException {
        OrderLine a = new OrderLine(1L, 10L, 2, price());
        OrderLine b = new OrderLine(2L, 10L, 2, price());
        assertThat(a).isNotEqualTo(b);
    }
}
