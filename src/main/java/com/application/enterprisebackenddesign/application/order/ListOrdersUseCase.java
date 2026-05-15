package com.application.enterprisebackenddesign.application.order;

import com.application.enterprisebackenddesign.domain.order.Order;
import com.application.enterprisebackenddesign.domain.order.OrderRepository;
import com.application.enterprisebackenddesign.domain.order.OrderStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public Page<Order> listAll(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    public List<Order> listByCustomerId(Long id) {
        return orderRepository.findByCustomerId(id);
    }

    public Page<Order> listByCustomerId(Long id, Pageable pageable) {
        return orderRepository.findByCustomerId(id, pageable);
    }

    public List<Order> listByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public Page<Order> listByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
    }

    public List<Order> listByCustomerIdAndStatus(Long customerId, OrderStatus status) {
        return orderRepository.findByCustomerIdAndStatus(customerId, status);
    }

    public Page<Order> listByCustomerIdAndStatus(Long customerId, OrderStatus status, Pageable pageable) {
        return orderRepository.findByCustomerIdAndStatus(customerId, status, pageable);
    }
}
