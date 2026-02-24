package com.application.enterprisebackenddesign.infrastructure.persistence.order;

import com.application.enterprisebackenddesign.domain.order.OrderStatus;
import com.application.enterprisebackenddesign.infrastructure.persistence.order.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataOrderRepository extends JpaRepository<OrderEntity, Long> {

    List<OrderEntity> findByStatus(String status);

    List<OrderEntity> findByCustomerId(Long customerId);

    List<OrderEntity> findByCustomerIdAndStatus(Long customerId, String status);
}
