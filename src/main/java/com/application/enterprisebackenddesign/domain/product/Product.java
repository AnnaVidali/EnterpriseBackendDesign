package com.application.enterprisebackenddesign.domain.product;

import com.application.enterprisebackenddesign.domain.shared.DomainException;
import com.application.enterprisebackenddesign.domain.shared.Money;
import lombok.Getter;

@Getter
public class Product {

    private final Long id;
    private String name;
    private Money price;
    private String sku;

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
    }

    public Money updatePrice(Money newPrice){
        Money oldPrice = this.price;
        this.price = newPrice;
        return oldPrice;
    }

    public String updateName(String name){
        String oldName = this.name;
        this.name = name;
        return oldName;
    }

    public String updateSku(String sku){
        String oldSku = this.sku;
        this.sku = sku;
        return oldSku;
    }
}
