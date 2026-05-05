package com.application.enterprisebackenddesign.domain.shared;

import lombok.Getter;

@Getter
public class CustomerCreatedEvent extends DomainEvent {

    private final Long id;
    private final String name;
    private final String lastName;
    private final String email;

    public CustomerCreatedEvent(Long id, String name, String lastName, String email) {
        super(DomainEventType.CUSTOMER_CREATED);
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.email = email;
    }
}
