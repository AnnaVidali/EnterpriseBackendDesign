package com.application.enterprisebackenddesign.infrastructure.persistence.payment.entity;

import com.application.enterprisebackenddesign.domain.payment.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity for the payments table.
 *
 * Interview context: PaymentEntity stores references to invoice, order, and
 * customer — all as simple Long fields (no JPA relationships). This is
 * because Payment is an aggregate root in its own right, and cross-aggregate
 * references should be by ID only, not by JPA object reference.
 *
 * The status column uses @Enumerated(EnumType.STRING), storing the enum
 * NAME rather than ORDINAL. This is critical: string-based storage survives
 * enum reordering, and the column value is human-readable in the database.
 *
 * paymentDate is nullable (null = still pending, non-null = completed),
 * which is semantically clearer than a sentinel date value.
 *
 * See OrderEntity and CustomerEntity for details on @Version and audit
 * field patterns that are identical across all entities.
 */
@Getter
@Setter
@Entity
@Table(name = "payments")
@EntityListeners(AuditingEntityListener.class)
public class PaymentEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private Long invoiceId;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column
    private LocalDateTime paymentDate;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime lastModifiedDate;

    @Version
    private Long version = 0L;
}
