package com.application.enterprisebackenddesign.domain.shared;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency EUR = Currency.getInstance("EUR");

    @Test
    void shouldCreateMoneyWithValidAmountAndCurrency() throws DomainException {
        Money money = new Money(new BigDecimal("10.50"), USD);
        assertThat(money.getAmount()).isEqualByComparingTo(new BigDecimal("10.50"));
        assertThat(money.getCurrency()).isEqualTo(USD);
    }

    @Test
    void shouldScaleAmountToTwoDecimals() throws DomainException {
        Money money = new Money(new BigDecimal("10.555"), USD);
        assertThat(money.getAmount()).isEqualByComparingTo(new BigDecimal("10.56"));
    }

    @Test
    void shouldCreateZeroMoney() throws DomainException {
        Money zero = Money.zero(USD);
        assertThat(zero.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(zero.getCurrency()).isEqualTo(USD);
        assertThat(zero.isZero()).isTrue();
    }

    @Test
    void shouldRejectNullAmount() {
        assertThatThrownBy(() -> new Money(null, USD))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Amount cannot be null or negative");
    }

    @Test
    void shouldRejectNegativeAmount() {
        assertThatThrownBy(() -> new Money(new BigDecimal("-5.00"), USD))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Amount cannot be null or negative");
    }

    @Test
    void shouldRejectNullCurrency() {
        assertThatThrownBy(() -> new Money(BigDecimal.TEN, null))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Currency cannot be null");
    }

    @Test
    void shouldRejectNullCurrencyForZero() {
        assertThatThrownBy(() -> Money.zero(null))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Currency cannot be null");
    }

    @Test
    void shouldMultiplyByQuantity() throws DomainException {
        Money money = new Money(new BigDecimal("10.00"), USD);
        Money result = money.multiply(3);
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("30.00"));
        assertThat(result.getCurrency()).isEqualTo(USD);
    }

    @Test
    void shouldRejectMultiplyByNegativeQuantity() throws DomainException {
        Money money = new Money(BigDecimal.TEN, USD);
        assertThatThrownBy(() -> money.multiply(-1))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Quantity cannot be negative");
    }

    @Test
    void shouldMultiplyByZero() throws DomainException {
        Money money = new Money(new BigDecimal("10.00"), USD);
        Money result = money.multiply(0);
        assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldAddMoneyWithSameCurrency() throws DomainException {
        Money a = new Money(new BigDecimal("10.00"), USD);
        Money b = new Money(new BigDecimal("5.50"), USD);
        Money result = a.add(b);
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("15.50"));
        assertThat(result.getCurrency()).isEqualTo(USD);
    }

    @Test
    void shouldRejectAddWithDifferentCurrency() throws DomainException {
        Money a = new Money(BigDecimal.TEN, USD);
        Money b = new Money(BigDecimal.TEN, EUR);
        assertThatThrownBy(() -> a.add(b))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Currencies do not match");
    }

    @Test
    void shouldRejectAddNull() throws DomainException {
        Money a = new Money(BigDecimal.TEN, USD);
        assertThatThrownBy(() -> a.add(null))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Cannot add null value");
    }

    @Test
    void shouldCompareMoneyWithSameCurrency() throws DomainException {
        Money small = new Money(new BigDecimal("5.00"), USD);
        Money large = new Money(new BigDecimal("10.00"), USD);
        assertThat(small.compareTo(large)).isNegative();
        assertThat(large.compareTo(small)).isPositive();
        assertThat(small.compareTo(small)).isZero();
    }

    @Test
    void shouldThrowOnCompareDifferentCurrencies() throws DomainException {
        Money usd = new Money(BigDecimal.TEN, USD);
        Money eur = new Money(BigDecimal.TEN, EUR);
        assertThatThrownBy(() -> usd.compareTo(eur))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Currencies do not match");
    }

    @Test
    void shouldCheckEqualityByAmountAndCurrency() throws DomainException {
        Money a = new Money(new BigDecimal("10.00"), USD);
        Money b = new Money(new BigDecimal("10.00"), USD);
        Money c = new Money(new BigDecimal("10.00"), EUR);
        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
    }

    @Test
    void shouldHaveConsistentHashCode() throws DomainException {
        Money a = new Money(new BigDecimal("10.00"), USD);
        Money b = new Money(new BigDecimal("10.00"), USD);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void shouldReturnTrueForIsZero() throws DomainException {
        Money zero = Money.zero(USD);
        assertThat(zero.isZero()).isTrue();
    }

    @Test
    void shouldReturnFalseForIsZeroWhenNonZero() throws DomainException {
        Money money = new Money(BigDecimal.TEN, USD);
        assertThat(money.isZero()).isFalse();
    }
}
