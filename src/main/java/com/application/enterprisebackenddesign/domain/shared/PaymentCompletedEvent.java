package com.application.enterprisebackenddesign.domain.shared;

public class PaymentCompletedEvent extends DomainEvent {

    private final Long paymentId;
    private final Long invoiceId;
    private final Long orderId;
    private final Long customerId;
    private final Money amount;

    public PaymentCompletedEvent(Long paymentId, Long invoiceId, Long orderId, Long customerId, Money amount) {
        super(DomainEventType.PAYMENT_COMPLETED);
        this.paymentId = paymentId;
        this.invoiceId = invoiceId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
    }
}
