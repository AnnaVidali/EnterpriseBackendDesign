package com.application.enterprisebackenddesign.infrastructure.persistence.shared;

import com.application.enterprisebackenddesign.domain.shared.DomainException;
import com.application.enterprisebackenddesign.domain.shared.Money;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.util.Currency;

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
