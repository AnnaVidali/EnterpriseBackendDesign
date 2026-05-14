package com.application.enterprisebackenddesign.domain.shared;

import lombok.Getter;

@Getter
public class PaymentPendingEvent extends DomainEvent{

    private final Long paymentId;
    private final Long invoiceId;
    private final Money amount;

    public PaymentPendingEvent(Long paymentId, Long invoiceId, Money amount) {
        super(DomainEventType.PAYMENT_PENDING);
        this.paymentId = paymentId;
        this.invoiceId = invoiceId;
        this.amount = amount;
    }
}
