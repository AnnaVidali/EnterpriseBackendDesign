package com.application.enterprisebackenddesign.infrastructure.persistence.order.entity;

import com.application.enterprisebackenddesign.infrastructure.persistence.shared.MoneyEmbeddable;
import jakarta.persistence.*;

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

    public void setId(Long id) {
        this.id = id;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setPrice(MoneyEmbeddable price) {
        this.price = price;
    }

    public void setOrder(OrderEntity order) {
        this.order = order;
    }
}
