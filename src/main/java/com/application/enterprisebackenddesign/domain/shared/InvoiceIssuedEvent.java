package com.application.enterprisebackenddesign.domain.shared;

import lombok.Getter;

@Getter
public class InvoiceIssuedEvent extends DomainEvent {

    private final Long invoiceId;
    private final Long orderId;
    private final Long customerId;
    private final Money amount;

    public InvoiceIssuedEvent(Long invoiceId, Long orderId, Long customerId, Money amount) {
        super(DomainEventType.INVOICE_ISSUED);
        this.invoiceId = invoiceId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
    }
}
