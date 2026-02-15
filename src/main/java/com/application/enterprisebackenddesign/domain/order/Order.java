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
    private final Long orderLineId;

    public Order(Long id, Long customerId, List<OrderLine> orderLines, Long orderLineId) throws DomainException.BusinessRuleViolationException {

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
        this.orderLineId = orderLineId;
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
            events.add(new OrderLineUpdatedEvent(DomainEventType.ORDER_UPDATED, this.id, this.customerId, this.orderLineId, 0, orderLine.getQuantity()));
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
            events.add(new OrderLineUpdatedEvent(DomainEventType.ORDER_UPDATED, this.id, this.customerId, this.orderLineId, orderLine.getQuantity(), 0));
        } else {
            throw new DomainException.BusinessRuleViolationException("Cannot remove line from order with status: " + status);
        }
    }

    public void confirmOrder() throws DomainException {

        if(status == OrderStatus.CREATED && !orderLines.isEmpty()){
            status = OrderStatus.CONFIRMED;
            events.add(new OrderConfirmedEvent(id, customerId));
        } else {
            throw new DomainException("Cannot confirm order. Current status: " + status + ", lines count: " + orderLines.size());
        }
    }

    public void cancelOrder() throws DomainException {

        if(status == OrderStatus.CONFIRMED){
            status = OrderStatus.CANCELLED;
            events.add(new OrderCancelledEvent(id, customerId));
        } else {
            throw new DomainException("Cannot cancel order. Current status: " + status);
        }
    }

    public List<DomainEvent> pullEvents() {

        List<DomainEvent> copiedEvents = new ArrayList<>(events);
        events.clear();
        return copiedEvents;
    }

    private Money calculateTotal() {

        Money total = Money.zero();

        if(orderLines.isEmpty()){
            return Money.zero();
        }
        for(OrderLine orderLine : orderLines){
            Money subtotal = orderLine.getPrice().multiply(orderLine.getQuantity());
            total = total.add(subtotal);
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
            if(newQuantity == 0) {
                orderLines.remove(index);
            } else {
                OrderLine updatedOrderLine = orderLine.withQuantity(newQuantity);
                orderLines.set(index, updatedOrderLine);
            }
            totalAmount = calculateTotal();
            events.add(new OrderLineUpdatedEvent(DomainEventType.ORDER_UPDATED, this.id, this.customerId, this.orderLineId, orderLine.getQuantity(), newQuantity));
        } else {
            throw new DomainException("Cannot update orderLines. Current status: " + status);
        }
    }
}
