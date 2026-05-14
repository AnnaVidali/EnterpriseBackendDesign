package com.application.enterprisebackenddesign.domain.shared;

import com.application.enterprisebackenddesign.domain.invoice.InvoiceStatus;
import lombok.Getter;

@Getter
public class InvoiceCreatedEvent extends DomainEvent {

    private final Long id;
    private final Long customerId;
    private final Long orderId;
    private final Money amount;
    private final InvoiceStatus status;

    public InvoiceCreatedEvent(Long id, Long customerId, Long orderId, Money amount, InvoiceStatus status) {
        super(DomainEventType.INVOICE_CREATED);
        this.id = id;
        this.customerId = customerId;
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
    }
}
