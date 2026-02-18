package com.application.enterprisebackenddesign.domain.shared;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

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

    public Money subtract(Money subtotal) throws DomainException.BusinessRuleViolationException {
        if (subtotal == null) {
            throw new DomainException.BusinessRuleViolationException("Cannot subtract null value.");
        }
        assertSameCurrency(subtotal);
        if (amount.subtract(subtotal.amount).compareTo(BigDecimal.ZERO) < 0){
            throw new DomainException.BusinessRuleViolationException("Result after subtraction cannot be negative.");
        }
        return new Money(amount.subtract(subtotal.amount), currency);
    }

    public boolean isZero(){
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    private void assertSameCurrency(Money other) throws DomainException.BusinessRuleViolationException {
        if (!other.getCurrency().equals(currency)) {
            throw new DomainException.BusinessRuleViolationException("Currencies do not match.");
        }
    }

    public int compareTo(Money other) {
        try {
            assertSameCurrency(other);
        } catch (DomainException.BusinessRuleViolationException e) {
            throw new IllegalArgumentException("Currencies do not match.", e);
        }
        return amount.compareTo(other.amount);
    }

    public static Money of(double amount, String currencyCode) throws DomainException.BusinessRuleViolationException {
        return new Money(BigDecimal.valueOf(amount), Currency.getInstance(currencyCode));
    }

    @Override
    public boolean equals(Object o) {
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
