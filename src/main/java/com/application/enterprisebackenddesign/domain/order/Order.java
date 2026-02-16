package com.application.enterprisebackenddesign.domain.order;

import com.application.enterprisebackenddesign.domain.shared.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Order {
    private final Long id;
    private final Long customerId;
    private OrderStatus status;
    private final List<OrderLine> orderLines;
    private Money totalAmount;
    private final List<DomainEvent> events = new ArrayList<>();

    public Order(Long id, Long customerId, List<OrderLine> orderLines) throws DomainException.BusinessRuleViolationException {

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
        this.status = OrderStatus.CREATED;
        this.orderLines = new ArrayList<>(orderLines);
        this.totalAmount = calculateTotal();

        events.add(new OrderCreatedEvent(id, customerId, this.orderLines.size()));
    }

    public void addLine(OrderLine orderLine) throws DomainException.BusinessRuleViolationException {

        if(orderLine == null){
            throw new DomainException.BusinessRuleViolationException("Order line cannot be null.");
        }

        if(orderLines.contains(orderLine)){
            throw new DomainException.BusinessRuleViolationException("Order line already exists.");
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

    public void confirmOrder() throws DomainException {

        if(status == OrderStatus.CREATED && !orderLines.isEmpty()){
            status = OrderStatus.CONFIRMED;
            totalAmount = calculateTotal();
            events.add(new OrderConfirmedEvent(id, customerId));
        } else {
            throw new DomainException("Cannot confirm order. Current status: " + status + ", lines count: " + orderLines.size());
        }
    }

    public void cancelOrder() throws DomainException {

        if(status == OrderStatus.CREATED || status == OrderStatus.CONFIRMED){
            status = OrderStatus.CANCELLED;
            totalAmount = calculateTotal();
            events.add(new OrderCancelledEvent(id, customerId));
        } else {
            throw new DomainException("Cannot cancel order. Current status: " + status);
        }
    }

    public List<DomainEvent> pullEvents(boolean clear) {

        List<DomainEvent> copiedEvents = new ArrayList<>(events);
        if(clear) {
            events.clear();
        }
        return copiedEvents;
    }

    private Money calculateTotal() {

        Money total = Money.zero();

        if(orderLines.isEmpty()){
            return Money.zero();
        }
        for(OrderLine orderLine : orderLines){
            total = total.add(orderLine.getSubtotal());
        }
        return total;
    }

    public void updateOrderLines(OrderLine orderLine, int newQuantity) throws DomainException {

        if(orderLine == null){
            throw new DomainException("Order line cannot be null.");
        }
        if(status == OrderStatus.CREATED){
            if(!orderLines.contains(orderLine)){
                throw new DomainException("Order line does not exist.");
            }
            if(newQuantity < 0){
                throw new DomainException("New quantity cannot be negative.");
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
            throw new DomainException("Cannot update orderLines. Current status: " + status);
        }
    }

    public int getNumberOfOrderLines() {
        return orderLines.size();
    }

    public boolean hasLine(OrderLine orderLine) {
        return orderLines.contains(orderLine);
    }

    public boolean isEmpty() {
        return orderLines.isEmpty();
    }
}
