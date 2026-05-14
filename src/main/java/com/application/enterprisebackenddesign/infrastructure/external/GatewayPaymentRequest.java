package com.application.enterprisebackenddesign.infrastructure.external;

import com.application.enterprisebackenddesign.domain.shared.Money;

public record GatewayPaymentRequest(
        Long invoiceId,
        Money amount
) {}
