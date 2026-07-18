package com.application.enterprisebackenddesign.infrastructure.persistence.order.entity;

import com.application.enterprisebackenddesign.domain.order.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity for the orders table.
 *
 * Interview context: OrderEntity has a bidirectional @OneToMany mapping to
 * OrderLineEntity. This is the ONLY entity with a JPA relationship — because
 * OrderLine is a child entity within the Order aggregate, not a separate
 * aggregate. Key mapping choices:
 * 1. CascadeType.ALL — persisting OrderEntity also persists/updates/deletes
 *    its OrderLineEntities. This ensures aggregate consistency at the DB level.
 * 2. orphanRemoval = true — if an OrderLine is removed from the list, it's
 *    automatically deleted from the database. No manual cleanup needed.
 * 3. FetchType.LAZY — order lines are loaded on-demand. The @Transactional
 *    annotation on use cases ensures the session stays open during lazy loading.
 * 4. The bidirectional mapping uses "mappedBy" on the @OneToMany side, meaning
 *    OrderLineEntity owns the foreign key (order_id column). This is the
 *    standard JPA pattern for one-to-many with join columns.
 */
@Setter
@Getter
@Entity
@Table(name = "orders")
@EntityListeners(AuditingEntityListener.class)
public class OrderEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime lastModifiedDate;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<OrderLineEntity> orderLines = new ArrayList<>();

}
