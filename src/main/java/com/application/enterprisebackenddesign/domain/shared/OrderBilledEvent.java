package com.application.enterprisebackenddesign.domain.shared;

import lombok.Getter;

@Getter
public class OrderBilledEvent extends DomainEvent{

    private final Long orderId;
    private final Long customerId;
    private final Money billedAmount;


    public OrderBilledEvent(Long orderId, Long customerId, Money billedAmount) {
        super(DomainEventType.ORDER_BILLED);
        this.orderId = orderId;
        this.customerId = customerId;
        this.billedAmount = billedAmount;
    }
}
