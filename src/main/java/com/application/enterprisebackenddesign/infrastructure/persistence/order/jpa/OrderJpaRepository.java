package com.application.enterprisebackenddesign.infrastructure.persistence.order.jpa;

import com.application.enterprisebackenddesign.infrastructure.persistence.order.entity.OrderLineEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public class OrderJpaRepository extends JpaRepository<OrderLineEntity, Long> {
}
