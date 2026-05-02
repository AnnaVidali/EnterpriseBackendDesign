package com.application.enterprisebackenddesign.application.order;

import com.application.enterprisebackenddesign.application.shared.DomainEventPublisher;
import com.application.enterprisebackenddesign.domain.order.Order;
import com.application.enterprisebackenddesign.domain.order.OrderLine;
import com.application.enterprisebackenddesign.domain.order.OrderRepository;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import com.application.enterprisebackenddesign.domain.shared.Money;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class AddOrderLineUseCase {

    private final OrderRepository orderRepository;
    private final DomainEventPublisher eventPublisher;

    public AddOrderLineUseCase(OrderRepository orderRepository, DomainEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    public Order execute(Long orderId, Long orderLineId, Long productId, Money price, int quantity) throws DomainException {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new DomainException.ResourceNotFoundException("Order not found"));

        OrderLine orderLine = new OrderLine(orderLineId, productId, quantity, price);
        order.addLine(orderLine);

        Order savedOrder = orderRepository.save(order);

        savedOrder.pullEvents(true).forEach(eventPublisher::publish);

        return savedOrder;
    }
}
