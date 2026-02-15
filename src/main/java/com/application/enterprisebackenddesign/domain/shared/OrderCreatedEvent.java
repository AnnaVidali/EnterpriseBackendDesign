package com.application.enterprisebackenddesign.domain.shared;

import lombok.Getter;

@Getter
public class OrderCreatedEvent extends DomainEvent{

    private final Long orderId;
    private final Long customerId;
    private final int lineCount;

    public OrderCreatedEvent(Long orderId, Long customerId, int lineCount) {
        super(DomainEventType.ORDER_CREATED);
        this.orderId = orderId;
        this.customerId = customerId;
        this.lineCount = lineCount;
    }
}
