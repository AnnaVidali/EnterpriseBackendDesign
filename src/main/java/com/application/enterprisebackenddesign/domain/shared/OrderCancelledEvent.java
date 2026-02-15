package com.application.enterprisebackenddesign.domain.shared;

import lombok.Getter;

@Getter
public class OrderCancelledEvent extends DomainEvent{

    private final Long orderId;
    private final Long customerId;

    public OrderCancelledEvent(Long orderId, Long customerId) {
        this.orderId = orderId;
        this.customerId = customerId;
    }
}
