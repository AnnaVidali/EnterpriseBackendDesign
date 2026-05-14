package com.application.enterprisebackenddesign.domain.order;

import com.application.enterprisebackenddesign.domain.shared.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

@Getter
public class Order {
    private final Long id;
    private final Long customerId;
    private OrderStatus status;
    private final List<OrderLine> orderLines;
    private Money totalAmount;
    private final Currency currency;
    private final List<DomainEvent> events = new ArrayList<>();

    public Order(Long id, Long customerId, List<OrderLine> orderLines, Currency currency) throws DomainException.BusinessRuleViolationException {

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
        this.status = OrderStatus.CREATED;
        this.orderLines = new ArrayList<>(orderLines);
        this.currency = currency;
        this.totalAmount = calculateTotal();
        events.add(new OrderCreatedEvent(this.id, this.customerId, this.orderLines.size()));
    }

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

    public void confirmOrder() throws DomainException.BusinessRuleViolationException {

        if(status == OrderStatus.CREATED && !orderLines.isEmpty()){
            status = OrderStatus.CONFIRMED;
            totalAmount = calculateTotal();
            events.add(new OrderConfirmedEvent(id, customerId));
        } else {
            throw new DomainException.BusinessRuleViolationException("Cannot confirm order. Current status: " + status + ", lines count: " + orderLines.size());
        }
    }

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
