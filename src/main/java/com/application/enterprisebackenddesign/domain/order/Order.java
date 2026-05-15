package com.application.enterprisebackenddesign.domain.order;

import com.application.enterprisebackenddesign.domain.shared.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

/**
 * Aggregate root representing a customer order.
 * Manages order lines, status transitions (CREATED, CONFIRMED, CANCELLED),
 * total amount calculation, and raises domain events on all state changes.
 *
 * Interview context: Order is the richest aggregate in the system. Some key
 * design decisions:
 *
 * 1. Aggregate boundary: Order owns OrderLine. All access to order lines
 *    goes through Order methods (addLine, removeLine, updateOrderLines).
 *    This enforces invariants like "no line changes after CONFIRMED".
 *
 * 2. Two constructors: The 4-param constructor creates a NEW order (raises
 *    OrderCreatedEvent). The 5-param constructor is for RECONSTITUTION from
 *    the database — it preserves whatever status was persisted and does NOT
 *    raise a creation event. This pattern ("command constructor" vs
 *    "reconstitution constructor") avoids replaying events on load.
 *
 * 3. Status machine: Order enforces a finite state machine internally.
 *    CREATED → CONFIRMED → BILLED (via invoice), with CANCELLED allowed
 *    from CREATED or CONFIRMED. The domain model prevents illegal transitions
 *    via exceptions, keeping this logic visible and testable.
 *
 * 4. Domain events for every change: Each state-changing method raises a
 *    typed domain event. Events are collected in-memory and pulled by the
 *    use case after persistence, preventing duplicate publication on errors.
 *
 * 5. Total recalculation: totalAmount is always derived from order lines,
 *    never set directly. This prevents inconsistency bugs.
 */
@Getter
public class Order {
    private final Long id;
    private final Long customerId;
    private OrderStatus status;
    private final List<OrderLine> orderLines;
    private Money totalAmount;
    private final Currency currency;
    private final List<DomainEvent> events = new ArrayList<>();

    /**
     * Convenience constructor that creates an order with CREATED status.
     *
     * @param id          unique identifier, must not be null
     * @param customerId  owning customer, must not be null
     * @param orderLines  initial line items, must not be null
     * @param currency    order currency, must not be null
     * @throws DomainException.BusinessRuleViolationException if any validation fails
     */
    public Order(Long id, Long customerId, List<OrderLine> orderLines, Currency currency) throws DomainException.BusinessRuleViolationException {
        this(id, customerId, orderLines, currency, OrderStatus.CREATED);
        events.add(new OrderCreatedEvent(this.id, this.customerId, this.orderLines.size()));
    }

    /**
     * Full constructor that allows specifying the initial order status.
     *
     * @param id          unique identifier, must not be null
     * @param customerId  owning customer, must not be null
     * @param orderLines  initial line items, must not be null
     * @param currency    order currency, must not be null
     * @param status      initial order status
     * @throws DomainException.BusinessRuleViolationException if any validation fails
     */
    public Order(Long id, Long customerId, List<OrderLine> orderLines, Currency currency, OrderStatus status) throws DomainException.BusinessRuleViolationException {
        if(id == null){
            throw new DomainException.BusinessRuleViolationException("Id cannot be null.");
        }
        this.id = id;
        if(customerId == null){
            throw new DomainException.BusinessRuleViolationException("Customer id cannot be null.");
        }
        this.customerId = customerId;
        if(orderLines == null){
            throw new DomainException.BusinessRuleViolationException("Order line id cannot be null.");
        }
        if(currency == null){
            throw new DomainException.BusinessRuleViolationException("Currency cannot be null.");
        }
        this.status = status;
        this.orderLines = new ArrayList<>(orderLines);
        this.currency = currency;
        this.totalAmount = calculateTotal();
    }

    /**
     * Adds an order line to the order. Only allowed when status is CREATED.
     *
     * @param orderLine the line item to add, must not be null and must match the order currency
     * @throws DomainException.BusinessRuleViolationException if the order line is invalid or the order cannot be modified
     */
    public void addLine(OrderLine orderLine) throws DomainException.BusinessRuleViolationException {

        if(orderLine == null){
            throw new DomainException.BusinessRuleViolationException("Order line cannot be null.");
        }

        if(orderLines.contains(orderLine)){
            throw new DomainException.BusinessRuleViolationException("Order line already exists.");
        }

        if(!orderLine.getPrice().getCurrency().equals(currency)){
            throw new DomainException.BusinessRuleViolationException("Order line price does not match currency.");
        }

        if(status == OrderStatus.CREATED) {
            orderLines.add(orderLine);
            totalAmount = calculateTotal();
            events.add(new OrderLineUpdatedEvent(DomainEventType.ORDER_UPDATED, this.id, this.customerId, orderLine.getId(), 0, orderLine.getQuantity()));
        } else {
            throw new DomainException.BusinessRuleViolationException("Cannot add line to order with status: " + status);
        }
    }

    /**
     * Removes an order line from the order. Only allowed when status is CREATED.
     *
     * @param orderLine the line item to remove, must already exist in the order
     * @throws DomainException.BusinessRuleViolationException if the order line is not found or the order cannot be modified
     */
    public void removeLine(OrderLine orderLine) throws DomainException.BusinessRuleViolationException {

        if(orderLine == null){
            throw new DomainException.BusinessRuleViolationException("Order line cannot be null.");
        }

        if(!orderLines.contains(orderLine)){
            throw new DomainException.BusinessRuleViolationException("Order line does not exist.");
        }

        if(status == OrderStatus.CREATED) {
            orderLines.remove(orderLine);
            totalAmount = calculateTotal();
            events.add(new OrderLineUpdatedEvent(DomainEventType.ORDER_UPDATED, this.id, this.customerId, orderLine.getId(), orderLine.getQuantity(), 0));
        } else {
            throw new DomainException.BusinessRuleViolationException("Cannot remove line from order with status: " + status);
        }
    }

    /**
     * Transitions the order from CREATED to CONFIRMED. Requires at least one order line.
     *
     * @throws DomainException.BusinessRuleViolationException if the order cannot be confirmed
     */
    public void confirmOrder() throws DomainException.BusinessRuleViolationException {

        if(status == OrderStatus.CREATED && !orderLines.isEmpty()){
            status = OrderStatus.CONFIRMED;
            totalAmount = calculateTotal();
            events.add(new OrderConfirmedEvent(id, customerId));
        } else {
            throw new DomainException.BusinessRuleViolationException("Cannot confirm order. Current status: " + status + ", lines count: " + orderLines.size());
        }
    }

    /**
     * Cancels the order. Only allowed when status is CREATED or CONFIRMED.
     *
     * @throws DomainException.BusinessRuleViolationException if the order cannot be cancelled
     */
    public void cancelOrder() throws DomainException.BusinessRuleViolationException {

        if(status == OrderStatus.CREATED || status == OrderStatus.CONFIRMED){
            status = OrderStatus.CANCELLED;
            totalAmount = calculateTotal();
            events.add(new OrderCancelledEvent(id, customerId));
        } else {
            throw new DomainException.BusinessRuleViolationException("Cannot cancel order. Current status: " + status);
        }
    }

    public List<DomainEvent> pullEvents(boolean clear) {

        List<DomainEvent> copiedEvents = new ArrayList<>(events);
        if(clear) {
            events.clear();
        }
        return copiedEvents;
    }

    private Money calculateTotal() throws DomainException.BusinessRuleViolationException {

        Money total = Money.zero(currency);

        for(OrderLine orderLine : orderLines){
            if (!orderLine.getPrice().getCurrency().equals(currency)) {
                throw new DomainException.BusinessRuleViolationException(
                        "Order line price currency (" + orderLine.getPrice().getCurrency() +
                                ") does not match order currency (" + currency + ")."
                );
            }
            total = total.add(orderLine.getSubtotal());
        }
        return total;
    }

    /**
     * Updates the quantity of an existing order line. If the new quantity is zero the line is removed.
     * Only allowed when status is CREATED.
     *
     * @param orderLine  the line item to update, must already exist in the order
     * @param newQuantity the new quantity for the line item, must not be negative
     * @throws DomainException.BusinessRuleViolationException if the order line is not found or the order cannot be modified
     */
    public void updateOrderLines(OrderLine orderLine, int newQuantity) throws DomainException.BusinessRuleViolationException {

        if(orderLine == null){
            throw new DomainException.BusinessRuleViolationException("Order line cannot be null.");
        }
        if(!orderLine.getPrice().getCurrency().equals(currency)){
            throw new DomainException.BusinessRuleViolationException("Order line price does not match currency.");
        }
        if(status == OrderStatus.CREATED){
            if(!orderLines.contains(orderLine)){
                throw new DomainException.BusinessRuleViolationException("Order line does not exist.");
            }
            if(newQuantity < 0){
                throw new DomainException.BusinessRuleViolationException("New quantity cannot be negative.");
            }
            int index = orderLines.indexOf(orderLine);
            int oldQuantity = orderLine.getQuantity();
            if(newQuantity == 0) {
                orderLines.remove(index);
            } else {
                OrderLine updatedOrderLine = orderLine.withQuantity(newQuantity);
                orderLines.set(index, updatedOrderLine);
            }
            totalAmount = calculateTotal();
            events.add(new OrderLineUpdatedEvent(DomainEventType.ORDER_UPDATED, this.id, this.customerId, orderLine.getId(), oldQuantity, newQuantity));
        } else {
            throw new DomainException.BusinessRuleViolationException("Cannot update orderLines. Current status: " + status);
        }
    }

    public boolean isEmpty() {
        return orderLines.isEmpty();
    }

    @Override
    public String toString() {
        return "Order {\n" +
                "\tid = " + id +
                ",\n\tcustomerId = " + customerId +
                ",\n\tstatus = " + status +
                ",\n\tcurrency = " + currency +
                ",\n\ttotalAmount = " + totalAmount +
                ",\n\torderLines = " + orderLines +
                "\n}";
    }
}
