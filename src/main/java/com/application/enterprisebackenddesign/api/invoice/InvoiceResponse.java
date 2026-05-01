package com.application.enterprisebackenddesign.api.invoice;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InvoiceResponse(
        Long id,
        Long orderId,
        Long customerId,
        BigDecimal amount,
        String currency,
        String status,
        LocalDateTime invoiceDate
) {}
