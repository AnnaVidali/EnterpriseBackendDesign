package com.application.enterprisebackenddesign.domain.shared;

import lombok.Getter;

@Getter
public class CustomerUpdatedEvent extends DomainEvent {

    private final Long id;
    private final String fieldName;
    private final String oldValue;
    private final String newValue;

    public CustomerUpdatedEvent(Long id, String fieldName, String oldValue, String newValue) {
        super(DomainEventType.CUSTOMER_UPDATED);
        this.id = id;
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
}
