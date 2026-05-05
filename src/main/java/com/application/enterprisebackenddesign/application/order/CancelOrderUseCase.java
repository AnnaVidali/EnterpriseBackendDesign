package com.application.enterprisebackenddesign.application.order;

import com.application.enterprisebackenddesign.application.shared.DomainEventPublisher;
import com.application.enterprisebackenddesign.domain.order.Order;
import com.application.enterprisebackenddesign.domain.order.OrderRepository;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class CancelOrderUseCase {

    private final OrderRepository orderRepository;
    private final DomainEventPublisher eventPublisher;

    public CancelOrderUseCase(OrderRepository orderRepository, DomainEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    public Order execute (Long orderId) throws DomainException {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new DomainException.ResourceNotFoundException("Order not found"));

        order.cancelOrder();

        Order savedOrder = orderRepository.save(order);

        order.pullEvents(true).forEach(eventPublisher::publish);

        return savedOrder;
    }
}
