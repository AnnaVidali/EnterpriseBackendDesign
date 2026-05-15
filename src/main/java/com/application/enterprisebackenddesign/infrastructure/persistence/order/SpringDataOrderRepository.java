package com.application.enterprisebackenddesign.infrastructure.persistence.order;

import com.application.enterprisebackenddesign.domain.order.OrderStatus;
import com.application.enterprisebackenddesign.infrastructure.persistence.order.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataOrderRepository extends JpaRepository<OrderEntity, Long> {

    List<OrderEntity> findByStatus(OrderStatus status);

    Page<OrderEntity> findByStatus(OrderStatus status, Pageable pageable);

    List<OrderEntity> findByCustomerId(Long customerId);

    Page<OrderEntity> findByCustomerId(Long customerId, Pageable pageable);

    List<OrderEntity> findByCustomerIdAndStatus(Long customerId, OrderStatus status);

    Page<OrderEntity> findByCustomerIdAndStatus(Long customerId, OrderStatus status, Pageable pageable);
}
