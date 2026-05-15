package com.application.enterprisebackenddesign.application.order;

import com.application.enterprisebackenddesign.application.shared.DomainEventPublisher;
import com.application.enterprisebackenddesign.domain.order.Order;
import com.application.enterprisebackenddesign.domain.order.OrderRepository;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

/**
 * Canonical use case: confirms an order and publishes domain events.
 *
 * Application Service (DDD): This class follows the standard use case skeleton:
 * 1. Load the aggregate from the repository
 * 2. Call a domain method that encapsulates the business logic + records events
 * 3. Save the aggregate
 * 4. Publish all recorded domain events
 *
 * The @Transactional annotation ensures that the save and any side effects
 * within the same transaction boundary are atomic. The domain events are
 * published AFTER the save completes successfully.
 *
 * This use case is intentionally simple — it delegates all business logic
 * to Order.confirmOrder(). The application layer coordinates; the domain
 * layer decides.
 */
@Service
@Transactional
public class ConfirmOrderUseCase {

    private final OrderRepository orderRepository;
    private final DomainEventPublisher eventPublisher;


    public ConfirmOrderUseCase(OrderRepository orderRepository, DomainEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    public Order execute (Long orderId) throws DomainException {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new DomainException.ResourceNotFoundException("Order not found"));

        order.confirmOrder();

        Order savedOrder = orderRepository.save(order);

        order.pullEvents(true).forEach(eventPublisher::publish);

        return savedOrder;
    }
}
