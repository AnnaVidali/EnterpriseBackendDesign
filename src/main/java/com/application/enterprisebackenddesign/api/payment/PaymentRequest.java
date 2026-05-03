package com.application.enterprisebackenddesign.api.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PaymentRequest(
        @NotNull(message = "Invoice ID cannot be null") Long invoiceId,
        @NotNull(message = "Amount cannot be null") @Positive(message = "Amount must be positive") BigDecimal amount,
        @NotBlank(message = "Currency cannot be blank") String currency
) {}
