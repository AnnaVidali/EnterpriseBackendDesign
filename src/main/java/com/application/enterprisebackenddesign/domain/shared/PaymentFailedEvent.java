package com.application.enterprisebackenddesign.domain.shared;

public class PaymentFailedEvent extends DomainEvent {

    private final Long paymentId;
    private final Long invoiceId;
    private final String reason;

    public PaymentFailedEvent(Long paymentId, Long invoiceId, String reason) {
        super(DomainEventType.PAYMENT_FAILED);
        this.paymentId = paymentId;
        this.invoiceId = invoiceId;
        this.reason = reason;
    }
}
