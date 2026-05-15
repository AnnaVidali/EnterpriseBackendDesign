package com.application.enterprisebackenddesign.domain.order;

import com.application.enterprisebackenddesign.domain.shared.DomainException;
import com.application.enterprisebackenddesign.domain.shared.Money;
import lombok.Getter;

/**
 * Value object representing a single line item within an order.
 * Identified by a unique line id and composed of a product reference,
 * quantity, and unit price. Two order lines with the same id are
 * considered equal.
 *
 * Interview context: OrderLine is part of the Order aggregate root,
 * not a separate aggregate. It has no independent repository — it's
 * loaded and saved through the Order. All modifications must go
 * through Order methods which enforce aggregate invariants.
 *
 * Key decisions:
 * 1. withQuantity() returns a NEW OrderLine — immutability for safety.
 * 2. equals/hashCode based on ID — allows Order to use indexOf/contains.
 * 3. No @Version here — optimistic locking is on the Order aggregate root.
 *    If two users modify different lines simultaneously, the Order's version
 *    will fail on the second save (optimistic lock exception), preventing
 *    concurrent modification even though lines are stored separately.
 */
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

    public Money getSubtotal() throws DomainException.BusinessRuleViolationException {
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
