package com.application.enterprisebackenddesign.domain.order;

import com.application.enterprisebackenddesign.domain.shared.DomainException;
import com.application.enterprisebackenddesign.domain.shared.Money;
import lombok.Getter;

@Getter
public class OrderLine {

    private final Long id;
    private final Long productId;
    private int quantity;
    private final Money price;

    public OrderLine(Long id, Long productId, int quantity, Money price) throws DomainException.BusinessRuleViolationException {
        if (id == null) {
            throw new DomainException.BusinessRuleViolationException("Id cannot be null.");
        }
        this.id = id;
        if (productId == null) {
            throw new DomainException.BusinessRuleViolationException("Product id cannot be null.");
        }
        this.productId = productId;
        if (quantity <= 0) {
            throw new DomainException.BusinessRuleViolationException("Quantity must be greater than zero.");
        }
        this.quantity = quantity;
        if (price == null) {
            throw new DomainException.BusinessRuleViolationException("Price cannot be null.");
        }
        this.price = price;
    }

    public OrderLine withQuantity(int newQuantity) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        return new OrderLine(this.id, this.productId, newQuantity, this.price);
    }

    public Money getSubtotal() {
        return price.multiply(quantity);
    }
}
