package com.application.enterprisebackenddesign.domain.order;

import com.application.enterprisebackenddesign.domain.shared.DomainException;
import com.application.enterprisebackenddesign.domain.shared.Money;
import lombok.Getter;

@Getter
public class OrderLine {

    private final Long id;
    private final Long productId;
    private final int quantity;
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

    public OrderLine withQuantity(int newQuantity) throws DomainException.BusinessRuleViolationException {
        if (newQuantity <= 0) {
            throw new DomainException.BusinessRuleViolationException("Quantity must be greater than zero.");
        }
        return new OrderLine(this.id, this.productId, newQuantity, this.price);
    }

    public Money getSubtotal() {
        return price.multiply(quantity);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderLine that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "OrderLine {\n" +
                "\tid = " + id +
                ",\n\tproductId = " + productId +
                ",\n\tquantity = " + quantity +
                ",\n\tprice = " + price +
                "\n}";
    }
}
