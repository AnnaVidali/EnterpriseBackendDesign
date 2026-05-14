package com.application.enterprisebackenddesign.infrastructure.external;

public record PaymentResult(
        boolean success,
        String transactionId,
        String message
) {}
