package com.application.enterprisebackenddesign.infrastructure.persistence.order;

import com.application.enterprisebackenddesign.domain.order.Order;
import com.application.enterprisebackenddesign.domain.order.OrderRepository;
import com.application.enterprisebackenddesign.domain.order.OrderStatus;
import com.application.enterprisebackenddesign.infrastructure.persistence.order.entity.OrderEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class OrderRepositoryImpl implements OrderRepository {

    private final SpringDataOrderRepository repository;
    private final OrderMapper orderMapper;

    public OrderRepositoryImpl(SpringDataOrderRepository repository, OrderMapper orderMapper) {
        this.repository = repository;
        this.orderMapper = orderMapper;
    }

    @Override
    public Order save(Order order) {
        OrderEntity entity = orderMapper.toEntity(order);
        OrderEntity saved = repository.save(entity);
        return orderMapper.toDomain(saved);
    }

    @Override
    public Optional<Order> findById(Long orderId) {
        return repository.findById(orderId)
                .map(orderMapper::toDomain);
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return repository.findByStatus(status)
                .stream()
                .map(orderMapper::toDomain)
                .toList();
    }

    @Override
    public List<Order> findAll() {
        return repository.findAll()
                .stream()
                .map(orderMapper::toDomain)
                .toList();
    }

    @Override
    public List<Order> findByCustomerId(Long customerId) {
        return repository.findByCustomerId(customerId)
                .stream()
                .map(orderMapper::toDomain)
                .toList();
    }

    @Override
    public List<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status) {
        return repository.findByCustomerIdAndStatus(customerId, status)
                .stream()
                .map(orderMapper::toDomain)
                .toList();
    }
}
