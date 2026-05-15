package com.application.enterprisebackenddesign.application.order;

import com.application.enterprisebackenddesign.application.shared.DomainEventPublisher;
import com.application.enterprisebackenddesign.domain.order.Order;
import com.application.enterprisebackenddesign.domain.order.OrderRepository;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

/**
 * Canonical use case: cancels an order and publishes domain events.
 *
 * Application Service (DDD): Same pattern as ConfirmOrderUseCase.
 * The use case skeleton is deliberately consistent across all operations:
 * find → mutate → save → publish. This consistency is what makes the
 * architecture predictable and testable.
 *
 * Note: Cancellation is allowed from PENDING or CONFIRMED status.
 * The business rule is enforced inside Order.cancelOrder(), not here.
 * The use case should never contain if/else on order status — that
 * is domain logic.
 */
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
