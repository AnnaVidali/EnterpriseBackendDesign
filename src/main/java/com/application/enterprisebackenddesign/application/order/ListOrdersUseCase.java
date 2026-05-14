package com.application.enterprisebackenddesign.application.order;

import com.application.enterprisebackenddesign.domain.order.Order;
import com.application.enterprisebackenddesign.domain.order.OrderRepository;
import com.application.enterprisebackenddesign.domain.order.OrderStatus;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class ListOrdersUseCase {

    private final OrderRepository orderRepository;

    public ListOrdersUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<Order> listAll() {
        return orderRepository.findAll();
    }

    public List<Order> listByCustomerId(Long id) {
        return orderRepository.findByCustomerId(id);
    }

    public List<Order> listByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public List<Order> listByCustomerIdAndStatus(Long customerId, OrderStatus status) {
        return orderRepository.findByCustomerIdAndStatus(customerId, status);
    }
}
