package com.application.enterprisebackenddesign.api.order;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(
        Long id,
        Long customerId,
        String status,
        String currency,
        BigDecimal totalAmount,
        List<OrderLineResponse> lines
) {}
