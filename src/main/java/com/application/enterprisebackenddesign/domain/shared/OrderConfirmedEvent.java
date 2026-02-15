package com.application.enterprisebackenddesign.domain.shared;

import lombok.Getter;

@Getter
public class OrderConfirmedEvent extends DomainEvent{

    private final Long orderId;
    private final Long customerId;

    public OrderConfirmedEvent(Long orderId, Long customerId) {
        super(DomainEventType.ORDER_CONFIRMED);
        this.orderId = orderId;
        this.customerId = customerId;
    }
}
