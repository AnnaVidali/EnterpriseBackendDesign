package com.application.enterprisebackenddesign.domain.shared;

import lombok.Getter;

@Getter
public class CustomerDeletedEvent extends DomainEvent {

    private final Long customerId;

    public CustomerDeletedEvent(Long customerId) {
        super(DomainEventType.CUSTOMER_DELETED);
        this.customerId = customerId;
    }
}
