package com.application.enterprisebackenddesign.infrastructure.persistence.order.entity;

import com.application.enterprisebackenddesign.infrastructure.persistence.shared.MoneyEmbeddable;
import jakarta.persistence.*;
import lombok.Setter;

@Setter
@Entity
@Table(name = "Order_Lines")
public class OrderLineEntity {
    @Id
    private Long id;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private int quantity;

    @Embedded
    private MoneyEmbeddable price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderEntity order;

}
