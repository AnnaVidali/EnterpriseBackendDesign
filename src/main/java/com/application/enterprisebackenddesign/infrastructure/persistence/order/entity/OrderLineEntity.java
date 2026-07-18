package com.application.enterprisebackenddesign.infrastructure.persistence.order.entity;

import com.application.enterprisebackenddesign.infrastructure.persistence.shared.MoneyEmbeddable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * JPA entity for the order_lines table.
 *
 * Interview context: OrderLineEntity is the child side of the
 * Order → OrderLine bidirectional @OneToMany mapping. It owns the
 * foreign key (order_id column) because in JPA, the @ManyToOne
 * side is always the "owner" of the relationship.
 *
 * Note that OrderLineEntity does NOT have @Version, @CreatedDate, or
 * @LastModifiedDate. This is intentional — optimistic locking is on the
 * AGGREGATE ROOT (OrderEntity), not on individual lines. If two users
 * modify different lines simultaneously, the Order's version check will
 * fail on the second save, preventing concurrent modification.
 *
 * The "order" field creates the bidirectional link needed for JPA
 * cascading. When OrderEntity is saved with CascadeType.ALL, Hibernate
 * traverses this link to persist/update/delete child lines.
 *
 * MoneyEmbeddable is reused here (same as ProductEntity.price),
 * demonstrating the value of @Embeddable for shared value objects.
 */
@Setter
@Getter
@Entity
@Table(name = "order_lines")
public class OrderLineEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private int quantity;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "price_amount", nullable = false, precision = 19, scale = 2)),
            @AttributeOverride(name = "currency", column = @Column(name = "price_currency", nullable = false, length = 3))
    })
    private MoneyEmbeddable price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

}
