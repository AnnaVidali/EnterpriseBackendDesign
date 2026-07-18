package com.application.enterprisebackenddesign.infrastructure.persistence.invoice.entity;

import com.application.enterprisebackenddesign.domain.invoice.InvoiceStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity for the invoices table.
 *
 * Interview context: Like all entities in this project, InvoiceEntity uses
 * weak ID references (Long customerId, Long orderId) instead of @ManyToOne
 * JPA relationships. This is a deliberate architectural choice:
 *
 * 1. Aggregate boundaries: Invoice, Customer, and Order are separate aggregates.
 *    JPA @ManyToOne would cross aggregate boundaries, mixing concerns.
 * 2. Performance: No lazy loading proxies, no N+1 query problems.
 * 3. Simplicity: The entity is flat — all columns are in one table, no JOINs
 *    needed for basic CRUD.
 *
 * The amount + currency pattern mirrors the domain Money value object but
 * stored as two flat columns (DECIMAL + VARCHAR) rather than an @Embedded.
 * Either approach works — this is just a slightly different mapping choice
 * than ProductEntity (which uses @Embedded MoneyEmbeddable for price).
 */
@Setter
@Getter
@Entity
@Table(name = "invoices")
@EntityListeners(AuditingEntityListener.class)
public class InvoiceEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;

    @Column
    private LocalDateTime invoiceDate;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime lastModifiedDate;

    @Version
    @Column(nullable = false)
    private Long version = 0L;
}
