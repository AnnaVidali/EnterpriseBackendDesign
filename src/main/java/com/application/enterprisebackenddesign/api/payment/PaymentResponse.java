package com.application.enterprisebackenddesign.api.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long invoiceId,
        Long orderId,
        Long customerId,
        BigDecimal amount,
        String currency,
        String status,
        LocalDateTime paymentDate
) {}
