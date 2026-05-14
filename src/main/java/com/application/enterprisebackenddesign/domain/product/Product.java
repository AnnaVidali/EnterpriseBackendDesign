package com.application.enterprisebackenddesign.domain.product;

import com.application.enterprisebackenddesign.domain.shared.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Product {

    private final Long id;
    private String name;
    private Money price;
    private String sku;
    private final List<DomainEvent> events = new ArrayList<>();

    public Product(Long id, String name, Money price, String sku) throws DomainException.BusinessRuleViolationException {
        if(id == null){
            throw new DomainException.BusinessRuleViolationException("Id cannot be null.");
        }
        this.id = id;
        if (name == null || name.isEmpty()) {
            throw new DomainException.BusinessRuleViolationException("Name cannot be null or empty.");
        }
        this.name = name;
        if (price == null || price.isZero()) {
            throw new DomainException.BusinessRuleViolationException("Price cannot be null or zero.");
        }
        this.price = price;
        if (sku == null || sku.isEmpty()) {
            throw new DomainException.BusinessRuleViolationException("Sku cannot be null or empty.");
        }
        if (sku.startsWith("0") || sku.contains("@") || sku.contains("&") || sku.contains("<") || sku.contains(">")) {
            throw new DomainException.BusinessRuleViolationException("Sku format is invalid.");
        }
        this.sku = sku.toUpperCase();
        events.add(new ProductCreatedEvent(id, name, price, sku));
    }

    public void updatePrice(Money newPrice) throws DomainException {
        if (newPrice == null || newPrice.isZero()) {
            throw new DomainException.BusinessRuleViolationException("New price cannot be null or zero.");
        }
        Money oldPrice = this.price;
        this.price = newPrice;
        events.add(new ProductUpdatedEvent(id, "price", oldPrice, newPrice));
    }

    public void updateName(String newName) throws DomainException {
        if (newName == null || newName.isEmpty()) {
            throw new DomainException.BusinessRuleViolationException("New name cannot be null or empty.");
        }
        String oldName = this.name;
        this.name = newName;
        events.add(new ProductUpdatedEvent(id, "name", oldName, newName));
    }

    public void updateSku(String newSku) throws DomainException {
        if (newSku == null || newSku.isEmpty()) {
            throw new DomainException.BusinessRuleViolationException("New sku cannot be null or empty.");
        }
        if (newSku.startsWith("0") || newSku.contains("@") || newSku.contains("&") || newSku.contains("<") || newSku.contains(">")) {
            throw new DomainException.BusinessRuleViolationException("New sku format is invalid.");
        }
        String oldSku = this.sku;
        this.sku = newSku.toUpperCase();
        events.add(new ProductUpdatedEvent(id, "sku", oldSku, newSku));
    }

    public List<DomainEvent> pullEvents(boolean clear) {
        List<DomainEvent> copiedEvents = new ArrayList<>(events);
        if(clear) {
            events.clear();
        }
        return copiedEvents;
    }
}
