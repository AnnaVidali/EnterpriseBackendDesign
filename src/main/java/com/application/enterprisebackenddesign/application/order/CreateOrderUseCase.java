package com.application.enterprisebackenddesign.application.order;

import com.application.enterprisebackenddesign.application.shared.DomainEventPublisher;
import com.application.enterprisebackenddesign.domain.order.Order;
import com.application.enterprisebackenddesign.domain.order.OrderLine;
import com.application.enterprisebackenddesign.domain.order.OrderRepository;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Currency;
import java.util.List;

@Service
@Transactional
public class CreateOrderUseCase {
    private final OrderRepository orderRepository;
    private final DomainEventPublisher eventPublisher;


    public CreateOrderUseCase(OrderRepository orderRepository, DomainEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    public Order execute(Long id, Long customerId, List<OrderLine> orderLines, Currency currency) throws DomainException.BusinessRuleViolationException {
        Order order = new Order(id, customerId, orderLines, currency);

        Order savedOrder = orderRepository.save(order);

        savedOrder.pullEvents(true).forEach(eventPublisher::publish);

        return savedOrder;
    }
}
