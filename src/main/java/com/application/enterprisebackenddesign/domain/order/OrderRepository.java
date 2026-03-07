package com.application.enterprisebackenddesign.domain.order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(Long orderId);

    void deleteById(Long orderId);

    List<Order> findByStatus(OrderStatus status);

    boolean existsById(Long orderId);

    List<Order> findAll();

    List<Order> findByCustomerId(Long customerId);

    List<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status);
}
