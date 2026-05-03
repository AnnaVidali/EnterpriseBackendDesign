package com.application.enterprisebackenddesign.api.order;

import java.math.BigDecimal;

public record OrderLineResponse(
        Long id,
        Long productId,
        int quantity,
        BigDecimal price
) {}
