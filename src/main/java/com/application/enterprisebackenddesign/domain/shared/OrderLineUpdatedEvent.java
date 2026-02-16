package com.application.enterprisebackenddesign.domain.shared;

import lombok.Getter;

@Getter
public class OrderLineUpdatedEvent extends DomainEvent {

    private final Long orderId;
    private final Long customerId;
    private final Long orderLineId;
    private final int oldQuantity;
    private final int newQuantity;

    public OrderLineUpdatedEvent(DomainEventType eventType, Long orderId, Long customerId, Long orderLineId, int oldQuantity, int newQuantity) {
        super(eventType);
        this.orderId = orderId;
        this.customerId = customerId;
        this.orderLineId = orderLineId;
        this.oldQuantity = oldQuantity;
        this.newQuantity = newQuantity;
    }
}
