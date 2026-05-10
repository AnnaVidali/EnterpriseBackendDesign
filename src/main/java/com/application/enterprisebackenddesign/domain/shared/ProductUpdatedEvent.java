package com.application.enterprisebackenddesign.domain.shared;

import lombok.Getter;

@Getter
public class ProductUpdatedEvent extends DomainEvent {

    private final Long id;
    private final String fieldName;
    private final Object oldValue;
    private final Object newValue;

    public ProductUpdatedEvent(Long id, String fieldName, Object oldValue, Object newValue) {
        super(DomainEventType.PRODUCT_UPDATED);
        this.id = id;
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
}
