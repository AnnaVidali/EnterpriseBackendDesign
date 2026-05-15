package com.application.enterprisebackenddesign.infrastructure.persistence.order;

import com.application.enterprisebackenddesign.domain.order.Order;
import com.application.enterprisebackenddesign.domain.order.OrderRepository;
import com.application.enterprisebackenddesign.domain.order.OrderStatus;
import com.application.enterprisebackenddesign.infrastructure.persistence.order.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Adapter implementation of the OrderRepository port.
 *
 * Hexagonal Architecture: This class implements the domain-defined
 * OrderRepository interface (the "port") using Spring Data JPA (the "adapter").
 * The domain layer depends only on the interface — it has no knowledge of JPA,
 * Spring Data, or the relational schema.
 *
 * The Order aggregate is the most complex to persist because it contains
 * OrderLine children (bidirectional @OneToMany). Every save() call cascades
 * from OrderEntity to OrderLineEntity. The OrderMapper handles the
 * bidirectional mapping including setting the parent reference on each line.
 *
 * Key pattern: The domain Order and the JPA OrderEntity are separate classes
 * in separate packages. This is intentional — it avoids JPA annotations
 * leaking into the domain model and allows the domain to use its own
 * patterns (collections, value objects, event recording).
 */
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
    public Page<Order> findByStatus(OrderStatus status, Pageable pageable) {
        return repository.findByStatus(status, pageable)
                .map(orderMapper::toDomain);
    }

    @Override
    public List<Order> findAll() {
        return repository.findAll()
                .stream()
                .map(orderMapper::toDomain)
                .toList();
    }

    @Override
    public Page<Order> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(orderMapper::toDomain);
    }

    @Override
    public List<Order> findByCustomerId(Long customerId) {
        return repository.findByCustomerId(customerId)
                .stream()
                .map(orderMapper::toDomain)
                .toList();
    }

    @Override
    public Page<Order> findByCustomerId(Long customerId, Pageable pageable) {
        return repository.findByCustomerId(customerId, pageable)
                .map(orderMapper::toDomain);
    }

    @Override
    public List<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status) {
        return repository.findByCustomerIdAndStatus(customerId, status)
                .stream()
                .map(orderMapper::toDomain)
                .toList();
    }

    @Override
    public Page<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status, Pageable pageable) {
        return repository.findByCustomerIdAndStatus(customerId, status, pageable)
                .map(orderMapper::toDomain);
    }
}
