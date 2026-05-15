package com.application.enterprisebackenddesign.infrastructure.persistence.shared;

import com.application.enterprisebackenddesign.domain.shared.DomainException;
import com.application.enterprisebackenddesign.domain.shared.Money;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * JPA embeddable mapping for the Money value object.
 *
 * Interview context: @Embeddable maps the Money's fields (amount, currency)
 * as columns in the parent table rather than a separate table. For example,
 * ProductEntity has an embedded "price" field that maps to two columns:
 * price_amount (DECIMAL) and price_currency (VARCHAR).
 *
 * This is a design tradeoff:
 * PRO: Simple, no JOINs, easy to query ("products where price_amount > 100").
 * CON: Can't share the same Money instance across multiple entities at the DB
 * level (but in practice, each product has its own price anyway).
 *
 * The fromDomain/toDomain methods handle the conversion. toDomain re-runs
 * Money's validation, so corrupted DB data (negative amount, invalid currency)
 * is caught and mapped to an IllegalStateException in the repository mapper.
 */
@Embeddable
public class MoneyEmbeddable {

    private BigDecimal amount;
    private String currency;

    public static MoneyEmbeddable fromDomain(Money money) {
        MoneyEmbeddable moneyEmbeddable = new MoneyEmbeddable();
        moneyEmbeddable.amount = money.getAmount();
        moneyEmbeddable.currency = money.getCurrency().getCurrencyCode();

        return moneyEmbeddable;
    }

    public Money toDomain() throws DomainException.BusinessRuleViolationException {
        return new Money(amount, Currency.getInstance(currency));
    }

}
