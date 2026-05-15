package com.application.enterprisebackenddesign.infrastructure.persistence.product.entity;

import com.application.enterprisebackenddesign.infrastructure.persistence.shared.MoneyEmbeddable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * JPA entity for the products table.
 *
 * Interview context: ProductEntity demonstrates the @Embedded mapping.
 * The "price" field is an @Embedded MoneyEmbeddable, which maps to two
 * columns in the products table: price_amount (DECIMAL) and price_currency
 * (VARCHAR). This avoids a separate "prices" table for a simple attribute.
 *
 * Like all entities, ProductEntity has:
 * - @Version for optimistic locking (prevents concurrent overwrites)
 * - @CreatedDate / @LastModifiedDate for audit trail
 * - Explicit @Id (no @GeneratedValue) — IDs come from IdGenerator
 *
 * The product's SKU has a unique constraint in the database (V1 migration),
 * enforced at the DB level in addition to domain-level validation in
 * the Product domain constructor.
 */
@Getter
@Setter
@Entity
@Table(name = "products")
@EntityListeners(AuditingEntityListener.class)
public class ProductEntity {
    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String sku;

    @Embedded
    private MoneyEmbeddable price;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime lastModifiedDate;

    @Version
    private Long version = 0L;
}
