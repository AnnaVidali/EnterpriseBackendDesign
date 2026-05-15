package com.application.enterprisebackenddesign.domain.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Repository port for the Order aggregate.
 *
 * Hexagonal Architecture (Port): The OrderRepository defines how the domain
 * interacts with persisted orders. The domain calls findById, save, findByStatus,
 * etc. without knowing whether the implementation uses JPA, JDBC, MongoDB,
 * or an in-memory store.
 *
 * The Order aggregate is query-heavy (6 query methods in addition to save/findById)
 * because it serves as the central hub in the order-to-cash flow. The query methods
 * support filtering by customer, status, and combinations — these mirror the
 * read-side requirements of the UI and downstream integrations.
 *
 * DDD note: findById returns Optional<Order>. This forces the caller (use case)
 * to handle the "not found" case explicitly, rather than dealing with null.
 */
public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(Long orderId);

    List<Order> findByStatus(OrderStatus status);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    List<Order> findAll();

    Page<Order> findAll(Pageable pageable);

    List<Order> findByCustomerId(Long customerId);

    Page<Order> findByCustomerId(Long customerId, Pageable pageable);

    List<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status);

    Page<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status, Pageable pageable);
}
