package com.application.enterprisebackenddesign.application.order;

import com.application.enterprisebackenddesign.application.shared.DomainEventPublisher;
import com.application.enterprisebackenddesign.domain.order.Order;
import com.application.enterprisebackenddesign.domain.order.OrderLine;
import com.application.enterprisebackenddesign.domain.order.OrderRepository;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class RemoveOrderLineUseCase {

    private final OrderRepository orderRepository;
    private final DomainEventPublisher eventPublisher;

    public RemoveOrderLineUseCase(OrderRepository orderRepository, DomainEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    public Order execute(Long orderId, Long orderLineId) throws DomainException.BusinessRuleViolationException {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new DomainException.BusinessRuleViolationException("Order not found"));

        OrderLine line = order.getOrderLines().stream()
                .filter(l -> l.getId().equals(orderLineId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("OrderLine not found"));

        order.removeLine(line);

        Order savedOrder = orderRepository.save(order);

        savedOrder.pullEvents(true).forEach(eventPublisher::publish);

        return savedOrder;
    }
}
