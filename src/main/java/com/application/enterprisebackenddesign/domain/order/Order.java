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

    public Order(Long id, Long customerId, List<OrderLine> orderLines) {
        this.id = id;
        this.customerId = customerId;
        this.status = OrderStatus.CREATED;
        this.orderLines = orderLines != null ? new ArrayList<>(orderLines) : new ArrayList<>();
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
}
