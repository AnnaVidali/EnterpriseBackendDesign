package com.application.enterprisebackenddesign.domain.shared;

import lombok.Getter;

@Getter
public class ProductCreatedEvent extends DomainEvent {

    private final Long id;
    private final String name;
    private final Money price;
    private final String sku;


    public ProductCreatedEvent(Long id, String name, Money price, String sku) {
        super(DomainEventType.PRODUCT_CREATED);
        this.id = id;
        this.name = name;
        this.price = price;
        this.sku = sku;
    }
}
