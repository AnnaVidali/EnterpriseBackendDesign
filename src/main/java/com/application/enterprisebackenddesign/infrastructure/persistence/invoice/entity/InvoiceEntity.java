package com.application.enterprisebackenddesign.infrastructure.persistence.invoice.entity;

import com.application.enterprisebackenddesign.domain.invoice.InvoiceStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "Invoices")
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
}
