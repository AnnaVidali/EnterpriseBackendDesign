package com.application.enterprisebackenddesign.domain.shared;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * Value object representing a monetary amount with an associated currency.
 * Provides arithmetic operations (add, multiply) and ensures currency
 * consistency across operations. Amounts are stored with two decimal places.
 *
 * Interview context: Money is a classic DDD value object. We made it a
 * full class instead of using BigDecimal or Double because:
 *
 * 1. Type safety: The compiler enforces that prices, totals, and payments
 *    all use the Money type — you can't accidentally pass a raw BigDecimal.
 *
 * 2. Currency enforcement: Every Money has a currency. Operations (add,
 *    multiply, compare) validate that currencies match, preventing the
 *    "adding dollars to euros" bug at compile-check time.
 *
 * 3. Immutability: All operations return NEW Money instances. This makes
 *    Money safe to share across threads and prevents subtle bugs.
 *
 * 4. Business rules encapsulated: "Amount cannot be negative", "two decimal
 *    places", "currency required" — these rules live in ONE place, the
 *    constructor. Any code that creates a Money gets validation for free.
 *
 * 5. Persistence: We map Money to the JPA @Embeddable MoneyEmbeddable,
 *    storing it as two columns (amount + currency) in the same table.
 *    This avoids a separate "currencies" table for a simple field.
 */
@Getter
public class Money implements Comparable<Money> {

    private final BigDecimal amount;
    private final Currency currency;

    public Money(BigDecimal amount, Currency currency) throws DomainException.BusinessRuleViolationException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException.BusinessRuleViolationException("Amount cannot be null or negative.");
        }
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        if (currency == null) {
            throw new DomainException.BusinessRuleViolationException("Currency cannot be null.");
        }
        this.currency = currency;
    }

    public static Money zero(Currency currency) throws DomainException.BusinessRuleViolationException {
        if (currency == null) {
            throw new DomainException.BusinessRuleViolationException("Currency cannot be null.");
        }
        return new Money(BigDecimal.ZERO, currency);
    }

    public Money multiply(int quantity) throws DomainException.BusinessRuleViolationException {
        if (quantity < 0) {
            throw new DomainException.BusinessRuleViolationException("Quantity cannot be negative.");
        }
        return new Money(amount.multiply(BigDecimal.valueOf(quantity)), currency);
    }

    public Money add(Money subtotal) throws DomainException.BusinessRuleViolationException {
        if (subtotal == null) {
            throw new DomainException.BusinessRuleViolationException("Cannot add null value.");
        }
        assertSameCurrency(subtotal);
        return new Money(amount.add(subtotal.amount), currency);
    }

    public boolean isZero(){
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    private void assertSameCurrency(Money other) throws DomainException.BusinessRuleViolationException {
        if (!other.getCurrency().equals(currency)) {
            throw new DomainException.BusinessRuleViolationException("Currencies do not match.");
        }
    }

    @Override
    public int compareTo(Money other) {
        if (!other.getCurrency().equals(currency)) {
            throw new IllegalArgumentException("Currencies do not match.");
        }
        return amount.compareTo(other.amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return Objects.equals(amount, money.amount) && Objects.equals(currency, money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return "Money {\n" +
                "\tamount = " + amount +
                ",\n\tcurrency = " + currency +
                "\n}";
    }
}
